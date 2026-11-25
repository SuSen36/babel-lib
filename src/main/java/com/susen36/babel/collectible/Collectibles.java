package com.susen36.babel.collectible;

import com.google.common.base.Suppliers;
import com.google.common.math.LongMath;
import com.mojang.logging.LogUtils;
import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * 玩家级收藏品状态：已使用、层数与冷却，持久化为 NBT 列表。
 * <p>
 * 服务端权威；写入统一使用带 {@code Sync} 后缀的静态方法，由它们负责向客户端同步。
 */
public class Collectibles implements INBTSerializable<ListTag> {
    public static final Capability<Collectibles> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    private static final ResourceLocation CAPABILITY_KEY = new ResourceLocation(BabelMod.MODID, "collectible_cap");

    // 首次查询须在物品注册完成后；索引保留Builder声明顺序，共享池沿用Forge注册表顺序。
    private static final Supplier<CollectibleDirectory> DIRECTORY = Suppliers.memoize(() -> {
        Map<ResourceLocation, RegistryObject<Item>> registry = new LinkedHashMap<>();
        List<RegistryObject<Item>> collectibleItems = new ArrayList<>();
        for (Map.Entry<ResourceLocation, RegistryObject<Item>> entry : CollectibleBuilder.getDeclaredCollectibles().entrySet()) {
            RegistryObject<Item> collectible = entry.getValue();
            if (collectible.isPresent() && collectible.get() instanceof CollectibleLike) {
                registry.put(entry.getKey(), collectible);
            }
        }
        for (Item item : ForgeRegistries.ITEMS) {
            if (item instanceof CollectibleLike) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id != null) {
                    RegistryObject<Item> collectible = registry.computeIfAbsent(id,
                            key -> RegistryObject.create(key, ForgeRegistries.ITEMS));
                    collectibleItems.add(collectible);
                }
            }
        }
        return new CollectibleDirectory(Collections.unmodifiableMap(registry), List.copyOf(collectibleItems));
    });

    private static final String ID = "id";
    private static final String USED = "used";
    private static final String USED_UP = "used_up";
    private static final String LAYER = "layer";
    private static final String COOLDOWN = "cooldown";
    private static final String DURATION = "duration";
    private static final String CONFIGURED_DURATION = "configured_duration";

    // 激活、层数与冷却必须同步生命周期，同源存储避免平行数据不同步
    private final HashMap<Item, CollectibleState> states = new HashMap<>();
    final Layer layer;
    final Cooldown cooldown;

    public static class CollectibleState {
        boolean used;
        boolean usedUp;
        int layer;
        long cooldownEndTick;
        long cooldownDuration;
        long configuredDuration;

        CollectibleState(int layer) {
            this.layer = layer;
        }

        boolean applyConfiguredDuration(long duration, long currentTick) {
            boolean changed = configuredDuration != duration;
            if (changed) {
                if (duration == Cooldown.PERMANENT) {
                    cooldownEndTick = Cooldown.PERMANENT;
                    cooldownDuration = Cooldown.PERMANENT;
                } else if (cooldownEndTick == Cooldown.PERMANENT) {
                    cooldownEndTick = Cooldown.finiteEndTick(currentTick, duration);
                    cooldownDuration = duration;
                } else if (cooldownEndTick != 0L || cooldownDuration != 0L) {
                    // 平移截止时间，保留已消耗的时间和额外续时。
                    long delta = duration - configuredDuration;
                    cooldownDuration = Math.max(0L, LongMath.saturatedAdd(cooldownDuration, delta));
                    cooldownEndTick = Math.max(1L, Cooldown.finiteEndTick(cooldownEndTick, delta));
                }
                configuredDuration = duration;
            }
            return changed;
        }
    }

    Collectibles(@Nullable ServerPlayer player) {
        this.layer = new Layer(this);
        this.cooldown = new Cooldown(this, player);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(CollectiblesEventsAdapter::onRegisterCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CollectiblesEventsAdapter::onAttachCapabilities);
        MinecraftForge.EVENT_BUS.addListener(CollectiblesEventsAdapter::onPlayerClone);
    }

    public static @NotNull Map<ResourceLocation, RegistryObject<Item>> getRegistry() {
        return DIRECTORY.get().registry();
    }

    public static @NotNull List<RegistryObject<Item>> getCollectibleItems() {
        return DIRECTORY.get().items();
    }

    private record CollectibleDirectory(Map<ResourceLocation, RegistryObject<Item>> registry,
                                        List<RegistryObject<Item>> items) {
    }

    private static class CollectiblesEventsAdapter {
        private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            doRegisterCapabilities(event);
        }

        private static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            doAttachCapabilities(event);
        }

        private static void onPlayerClone(PlayerEvent.Clone event) {
            doPlayerClone(event);
        }
    }

    private static void doRegisterCapabilities(@NotNull RegisterCapabilitiesEvent event) {
        event.register(Collectibles.class);
    }

    private static void doAttachCapabilities(@NotNull AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            CollectibleCapability cap = new CollectibleCapability(player);
            event.addCapability(CAPABILITY_KEY, cap);
        }
    }

    private static void doPlayerClone(@NotNull PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        // 死亡后 capability 已被 invalidate，先恢复才能读取旧数据；复制完成后再失效。
        original.reviveCaps();
        Collectibles oldData = original.getCapability(CAPABILITY).orElse(null);
        Collectibles newData = event.getEntity().getCapability(CAPABILITY).orElse(null);
        if (oldData != null && newData != null) {
            newData.deserializeNBT(oldData.serializeNBT());
        }
        original.invalidateCaps();
    }

    /** 收藏品效果总闸：命中即全部停效（自身属性、范围光环、事件钩子、效果 tick）； */
    public static boolean isEffectSuppressed(@NotNull Player player) {
        return player.isSpectator();
    }

    /**
     * 获取玩家收藏品数据，Capability 缺失时返回 {@code null}。
     */
    @Nullable
    public static Collectibles getData(@NotNull Player player) {
        return player.getCapability(CAPABILITY).orElse(null);
    }

    public static boolean isUsed(@NotNull Player player, @NotNull RegistryObject<? extends Item> collectible) {
        Collectibles data = getData(player);
        return data != null && data.isUsed(collectible.get());
    }

    public static boolean isUsed(@NotNull Player player, @NotNull Item item) {
        Collectibles data = getData(player);
        return data != null && data.isUsed(item);
    }

    public static boolean isUsedUp(@NotNull Player player, @NotNull RegistryObject<? extends Item> collectible) {
        Collectibles data = getData(player);
        return data != null && data.isUsedUp(collectible.get());
    }

    public static boolean isUsedUp(@NotNull Player player, @NotNull Item item) {
        Collectibles data = getData(player);
        return data != null && data.isUsedUp(item);
    }

    // capability 失效（死亡克隆等瞬态）时回退到空数据实例：读取到默认值，写入不落到真实存档
    public static @NotNull Collectibles.Layer getLayerData(@NotNull Player player) {
        Collectibles data = getData(player);
        return data != null ? data.layer : new Collectibles(null).layer;
    }

    public static @NotNull Collectibles.Cooldown getCooldownData(@NotNull Player player) {
        Collectibles data = getData(player);
        return data != null ? data.cooldown : new Collectibles(null).cooldown;
    }

    public static Tag serializeStates(@NotNull Player player) {
        Collectibles data = getData(player);
        return data != null ? data.serializeNBT() : new ListTag();
    }

    public static void deserializeStates(@NotNull Player player, @NotNull ListTag tag) {
        Collectibles data = getData(player);
        if (data != null) {
            data.deserializeNBT(tag);
        }
    }

    /**
     * 层数增加与同步
     * 在服务端调整藏品层数，并立即同步到客户端。
     *
     * @param delta 层数增量，可为负值
     */
    public static void addLayerAndSync(@NotNull Player player, @NotNull Item item, int delta) {
        if (!player.level().isClientSide) {
            getLayerData(player).addLayer(item, delta);
            BabelNetwork.syncCollectibles(player);
        }
    }

    /**
     * 指令等场景的激活入口，复用完整激活流程但不消耗物品；层数沿用当前值（未设置时为默认层数）。
     */
    public static void activateAndSync(@NotNull Player player, @NotNull Item item) {
        if (item instanceof CollectibleItem collectible) {
            collectible.activate(player.level(), player, new ItemStack(collectible), collectible);
        }
    }

    /**
     * 主动技能结算后的“已使用”开关：藏品仍保留在生效列表中，冷却到期时随状态一起清理。
     */
    public static void markUsedUpAndSync(@NotNull Player player, @NotNull Item item) {
        Collectibles data = getData(player);
        if (!player.level().isClientSide && data != null) {
            data.markUsedUp(item);
            BabelNetwork.syncCollectibles(player);
        }
    }

    /**
     * 整条移除收藏品状态；unmarkUsed 仅取消激活，层与冷却数据保留。
     */
    public static void removeAndSync(@NotNull Player player, @NotNull Item item) {
        Collectibles data = getData(player);
        if (!player.level().isClientSide && data != null) {
            data.remove(item);
            BabelNetwork.syncCollectibles(player);
        }
    }

    public static void expireUsedCollectibles(@NotNull Player player) {
        Collectibles data = getData(player);
        if (!player.level().isClientSide && data != null) {

            long currentTick = player.level().getServer().overworld().getGameTime();
            boolean changed = false;
            long duration = BabelConfig.collectibleDurationTicks;
            Iterator<CollectibleState> states = data.states.values().iterator();
            while (states.hasNext()) {
                CollectibleState state = states.next();
                if (state.used) {
                    changed |= state.applyConfiguredDuration(duration, currentTick);
                    if (state.cooldownEndTick > 0 && currentTick > state.cooldownEndTick) {
                        states.remove();
                        changed = true;
                    }
                } else if (state.usedUp) {
                    // 已取消激活的残留 usedUp 标记不再有意义，随过期检查一并清理。
                    state.usedUp = false;
                    changed = true;
                }
            }
            if (changed) {
                BabelNetwork.syncCollectibles(player);
            }
        }
    }

    public boolean isUsed(@NotNull Holder<Item> item) {
        return isUsed(item.value());
    }

    public boolean isUsed(@NotNull Item item) {
        CollectibleState state = states.get(item);
        return state != null && state.used;
    }

    public void markUsed(@NotNull Holder<Item> item) {
        markUsed(item.value());
    }

    public void markUsed(@NotNull Item item) {
        ensureState(item).used = true;
    }

    public void unmarkUsed(@NotNull Holder<Item> item) {
        unmarkUsed(item.value());
    }

    public void unmarkUsed(@NotNull Item item) {
        CollectibleState state = states.get(item);
        if (state != null) {
            state.used = false;
            state.usedUp = false;
        }
    }

    public boolean isUsedUp(@NotNull Holder<Item> item) {
        return isUsedUp(item.value());
    }

    public boolean isUsedUp(@NotNull Item item) {
        CollectibleState state = states.get(item);
        return state != null && state.usedUp;
    }

    public void markUsedUp(@NotNull Holder<Item> item) {
        markUsedUp(item.value());
    }

    public void markUsedUp(@NotNull Item item) {
        ensureState(item).usedUp = true;
    }

    public void unmarkUsedUp(@NotNull Holder<Item> item) {
        unmarkUsedUp(item.value());
    }

    public void unmarkUsedUp(@NotNull Item item) {
        CollectibleState state = states.get(item);
        if (state != null) {
            state.usedUp = false;
        }
    }

    public @NotNull Set<Item> getUsedUp() {
        Set<Item> usedUp = new HashSet<>();
        for (Map.Entry<Item, CollectibleState> entry : states.entrySet()) {
            if (entry.getValue().usedUp) {
                usedUp.add(entry.getKey());
            }
        }
        return usedUp;
    }

    public @NotNull Set<Item> getUsed() {
        var used = new HashSet<Item>();
        for (var entry : states.entrySet()) {
            if (entry.getValue().used) {
                used.add(entry.getKey());
            }
        }
        return used;
    }

    public void remove(@NotNull Item item) {
        states.remove(item);
    }

    private @NotNull CollectibleState ensureState(@NotNull Item item) {
        return states.computeIfAbsent(item, key -> {
            if (key instanceof CollectibleLike collectible) {
                return new CollectibleState(collectible.defaultLevel());
            }
            return new CollectibleState(0);
        });
    }

    private static @Nullable ResourceLocation getIdByItem(@NotNull Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        RegistryObject<Item> entry = getRegistry().get(key);
        ResourceLocation id = null;
        if (entry == null) {
            LogUtils.getLogger().error("try to query {} in Collectibles, but not matched.", item);
        } else {
            id = key;
        }
        return id;
    }

    private static @Nullable Item resolveItem(@NotNull ResourceLocation id) {
        RegistryObject<Item> entry = getRegistry().get(id);
        Item item = null;
        if (entry != null && entry.isPresent()) {
            item = entry.get();
        }
        return item;
    }

    @Override
    public ListTag serializeNBT() {
        var list = new ListTag();
        for (var entry : states.entrySet()) {
            ResourceLocation id = getIdByItem(entry.getKey());
            if (id == null) {
                continue;
            }
            var tag = new CompoundTag();
            CollectibleState state = entry.getValue();
            tag.putString(ID, id.toString());
            tag.putBoolean(USED, state.used);
            tag.putBoolean(USED_UP, state.usedUp);
            tag.putInt(LAYER, state.layer);
            tag.putLong(COOLDOWN, state.cooldownEndTick);
            tag.putLong(DURATION, state.cooldownDuration);
            tag.putLong(CONFIGURED_DURATION, state.configuredDuration);
            list.add(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(@NotNull ListTag list) {
        states.clear();
        for (Tag element : list) {
            if (element instanceof CompoundTag tag) {
                Item item = null;
                ResourceLocation id = ResourceLocation.tryParse(tag.getString(ID));
                if (id != null) {
                    item = resolveItem(id);
                }
                if (item == null) {
                    BabelMod.LOGGER.warn("Skipping unknown collectible id in saved data: {}", tag.getString(ID));
                    continue;
                }
                CollectibleState state = new CollectibleState(item instanceof CollectibleLike collectible ? collectible.defaultLevel() : 0);
                state.used = tag.getBoolean(USED);
                state.usedUp = tag.getBoolean(USED_UP);
                int minLevel = 0;
                int maxLevel = 1;
                if (item instanceof CollectibleLike collectible) {
                    minLevel = collectible.minLevel();
                    maxLevel = collectible.maxLevel();
                }
                state.layer = Mth.clamp(tag.getInt(LAYER), minLevel, maxLevel);
                state.cooldownEndTick = tag.getLong(COOLDOWN);
                state.cooldownDuration = tag.getLong(DURATION);
                state.configuredDuration = tag.contains(CONFIGURED_DURATION, Tag.TAG_LONG)
                        ? tag.getLong(CONFIGURED_DURATION) : state.cooldownDuration;
                states.put(item, state);
            }
        }
    }

    public static class Layer {
        private final Collectibles owner;

        Layer(Collectibles owner) {
            this.owner = owner;
        }

        public int getLayer(@NotNull Holder<Item> item) {
            return getLayer(item.value());
        }

        public int getLayer(@NotNull Item item) {
            CollectibleState state = owner.states.get(item);
            if (state != null) {
                return state.layer;
            }
            if (item instanceof CollectibleLike collectible) {
                return collectible.defaultLevel();
            }
            return 0;
        }

        public void setLayer(@NotNull Holder<Item> item, @NotNull Integer layer) {
            setLayer(item.value(), layer);
        }

        public void setLayer(@NotNull Item item, @NotNull Integer layer) {
            int minLevel = 0;
            int maxLevel = 1;
            if (item instanceof CollectibleLike collectible) {
                minLevel = collectible.minLevel();
                maxLevel = collectible.maxLevel();
            }
            owner.ensureState(item).layer = Mth.clamp(layer, minLevel, maxLevel);
        }

        public void addLayer(@NotNull Item item, @NotNull Integer layer) {
            setLayer(item, getLayer(item) + layer);
        }

        public void addLayer(@NotNull Holder<Item> item, @NotNull Integer layer) {
            setLayer(item, getLayer(item) + layer);
        }

        public void removeLayer(@NotNull Item item) {
            CollectibleState state = owner.states.get(item);
            if (state != null) {
                state.layer = item instanceof CollectibleLike collectible ? collectible.defaultLevel() : 0;
            }
        }

        public void removeLayer(@NotNull Holder<Item> item) {
            removeLayer(item.value());
        }
    }

    public static class Cooldown {
        private static final long PERMANENT = -1L;
        private final Collectibles owner;
        private final @Nullable ServerPlayer player;

        Cooldown(Collectibles owner, @Nullable ServerPlayer player) {
            this.owner = owner;
            this.player = player;
        }

        private @Nullable ServerPlayer getPlayer() {
            return this.player;
        }

        private static long finiteEndTick(long startTick, long duration) {
            // 过期条件是严格大于，需给最后一次检查留出一个 tick。
            return Math.min(LongMath.saturatedAdd(startTick, duration), Long.MAX_VALUE - 1L);
        }

        public void setCooldownEndTick(Item item, MinecraftServer server, Long offsetTicks) {
            CollectibleState state = owner.ensureState(item);
            state.cooldownEndTick = offsetTicks == PERMANENT ? PERMANENT : finiteEndTick(server.overworld().getGameTime(), offsetTicks);
            state.cooldownDuration = offsetTicks;
            state.configuredDuration = BabelConfig.collectibleDurationTicks;
            BabelNetwork.syncCollectibles(Objects.requireNonNull(getPlayer()));
        }

        public void setCooldownEndTick(Holder<Item> item, MinecraftServer server, Long offsetTicks) {
            setCooldownEndTick(item.value(), server, offsetTicks);
        }

        public void addCooldownEndTick(Item item, MinecraftServer server, Long addToEndTick) {
            CollectibleState state = owner.ensureState(item);
            if (state.cooldownEndTick != PERMANENT) {
                long startTick = state.cooldownEndTick == 0L ? server.overworld().getGameTime() : state.cooldownEndTick;
                state.cooldownEndTick = finiteEndTick(startTick, addToEndTick);
                state.cooldownDuration = LongMath.saturatedAdd(state.cooldownDuration, addToEndTick);
                // 同步配置基准，避免下次过期检查把配置时长差再次叠加到本次冷却。
                state.configuredDuration = BabelConfig.collectibleDurationTicks;
                BabelNetwork.syncCollectibles(Objects.requireNonNull(getPlayer()));
            }
        }

        public void addCooldownEndTick(Holder<Item> item, MinecraftServer server, Long addToEndTick) {
            addCooldownEndTick(item.value(), server, addToEndTick);
        }

        public long getCooldownEndTick(Item item) {
            CollectibleState state = owner.states.get(item);
            if (state != null) {
                return state.cooldownEndTick;
            }
            return 0L;
        }

        public long getCooldownEndTick(Holder<Item> item) {
            return getCooldownEndTick(item.value());
        }

        public long getCooldownDuration(Item item) {
            CollectibleState state = owner.states.get(item);
            if (state != null) {
                return state.cooldownDuration;
            }
            return 0L;
        }

        public long getCooldownDuration(Holder<Item> item) {
            return getCooldownDuration(item.value());
        }

        public void removeCooldown(Item item) {
            CollectibleState state = owner.states.get(item);
            if (state != null) {
                state.cooldownEndTick = 0L;
                state.cooldownDuration = 0L;
            }
        }

        private long getLastTicks(Item item, Long currentTick) {
            long endTick = getCooldownEndTick(item);
            return endTick == PERMANENT ? 0L : Math.max(currentTick - endTick, 0L);
        }

        private long getLastTicks(Holder<Item> item, Long currentTick) {
            return getLastTicks(item.value(), currentTick);
        }

        public long getLastTicks(Item item, MinecraftServer server) {
            return getLastTicks(item, server.overworld().getGameTime());
        }

        public long getLastTicks(Holder<Item> item, MinecraftServer server) {
            return getLastTicks(item.value(), server);
        }

        private boolean isReady(Item item, long currentTick) {
            long endTick = getCooldownEndTick(item);
            return endTick != PERMANENT && currentTick > endTick;
        }

        private boolean isReady(Holder<Item> item, long currentTick) {
            return isReady(item.value(), currentTick);
        }

        public boolean isReady(Item item, MinecraftServer server) {
            return isReady(item, server.overworld().getGameTime());
        }

        public boolean isReady(Holder<Item> item, MinecraftServer server) {
            return isReady(item.value(), server);
        }
    }
}