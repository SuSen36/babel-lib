package com.susen36.babel.collectible;

import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
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
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Collectibles implements INBTSerializable<ListTag> {
    @NotNull
    public static final HashBiMap<String, RegistryObject<Item>> Collectibles = HashBiMap.create();

    private static final String ID = "id";
    private static final String USED = "used";
    private static final String LAYER = "layer";
    private static final String COOLDOWN = "cooldown";
    private static final String DURATION = "duration";

    // 激活、层数与冷却必须同步生命周期，同源存储避免平行数据不同步
    private final HashMap<Item, CollectibleState> states = new HashMap<>();
    final Layer layer;
    final Cooldown cooldown;

    public static class CollectibleState {
        boolean used;
        int layer;
        long cooldownEndTick;
        long cooldownDuration;

        CollectibleState(int layer) {
            this.layer = layer;
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
        event.register(CollectibleCapability.class);
    }

    private static void doAttachCapabilities(@NotNull AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            CollectibleCapability cap = new CollectibleCapability(player);
            event.addCapability(CollectibleCapability.getKey(), cap);
        }
    }

    private static void doPlayerClone(@NotNull PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        CollectibleCapability oldCap = original.getCapability(CollectibleCapability.CAPABILITY).orElse(null);
        if (oldCap != null) {
            CompoundTag nbt = oldCap.serializeNBT();
            CollectibleCapability.of(event.getEntity()).deserializeNBT(nbt);
        }
        original.invalidateCaps();
    }

    public static @NotNull Collectibles getData(@NotNull Player player) {
        return CollectibleCapability.of(player).getCollectibles();
    }

    public static boolean isUsed(@NotNull Player player, @NotNull RegistryObject<? extends Item> collectible) {
        return getData(player).isUsed(collectible.get());
    }

    public static boolean isUsed(@NotNull Player player, @NotNull Item item) {
        return getData(player).isUsed(item);
    }

    public static @NotNull Collectibles.Layer getLayerData(@NotNull Player player) {
        return CollectibleCapability.of(player).getLayer();
    }

    public static @NotNull Collectibles.Cooldown getCooldownData(@NotNull Player player) {
        return CollectibleCapability.of(player).getCooldown();
    }

    public static Tag serializeStates(@NotNull Player player) {
        return getData(player).serializeNBT();
    }

    public static void deserializeStates(@NotNull Player player, @NotNull ListTag tag) {
        getData(player).deserializeNBT(tag);
    }

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
        } else if (item instanceof CollectibleItem.CustomCollectibleItem collectible) {
            collectible.activate(player.level(), player, new ItemStack(collectible), collectible);
        }
    }

    /**
     * 整条移除收藏品状态；unmarkUsed 仅取消激活，层与冷却数据保留。
     */
    public static void deactivateAndSync(@NotNull Player player, @NotNull Item item) {
        if (!player.level().isClientSide) {
            getData(player).remove(item);
            BabelNetwork.syncCollectibles(player);
        }
    }

    public static void expireUsedCollectibles(@NotNull Player player) {
        if (!player.level().isClientSide) {
            Collectibles data = getData(player);
            long currentTick = player.level().getServer().overworld().getGameTime();
            boolean changed = false;
            for (Item item : new ArrayList<>(data.getUsed())) {
                long endTick = getCooldownData(player).getCooldownEndTick(item);
                // endTick 为 0 表示从未设置冷却，这类数据不判定过期
                if (endTick > 0 && currentTick > endTick) {
                    data.remove(item);
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
        }
    }

    public @NotNull HashSet<Item> getUsed() {
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

    public static @NotNull String getIdByItem(@NotNull Holder<Item> item) {
        return getIdByItem(item.value());
    }

    // 注册表冻结后 item 到 id 的映射不变；Item 为注册期懒创建，无法在注册时直填
    private static final Map<Item, String> ITEM_ID_CACHE = new ConcurrentHashMap<>();

    // 方法引用 Collectibles::resolveIdByItem 会因同名静态字段被解析为表达式，改用 lambda
    public static @NotNull String getIdByItem(@NotNull Item item) {
        return ITEM_ID_CACHE.computeIfAbsent(item, target -> resolveIdByItem(target));
    }

    private static @NotNull String resolveIdByItem(@NotNull Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        RegistryObject<Item> entry = Collectibles.get(key.getPath());
        if (entry != null && entry.getId() != null && entry.getId().equals(key)) {
            return key.getPath();
        }
        LogUtils.getLogger().error("try to query {} in Collectibles, but not matched.", item);
        return "null";
    }

    private static @Nullable Item resolveItem(@NotNull String id) {
        RegistryObject<Item> entry = Collectibles.get(id);
        if (entry != null && entry.get() != null) {
            return entry.get();
        }
        return null;
    }

    @Override
    public ListTag serializeNBT() {
        var list = new ListTag();
        for (var entry : states.entrySet()) {
            var tag = new CompoundTag();
            CollectibleState state = entry.getValue();
            tag.putString(ID, getIdByItem(entry.getKey()));
            tag.putBoolean(USED, state.used);
            tag.putInt(LAYER, state.layer);
            tag.putLong(COOLDOWN, state.cooldownEndTick);
            tag.putLong(DURATION, state.cooldownDuration);
            list.add(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(@NotNull ListTag list) {
        states.clear();
        for (Tag element : list) {
            if (element instanceof CompoundTag tag) {
                Item item = resolveItem(tag.getString(ID));
                if (item == null) {
                    BabelMod.LOGGER.warn("Skipping unknown collectible id in saved data: {}", tag.getString(ID));
                    continue;
                }
                CollectibleState state = new CollectibleState(item instanceof CollectibleLike collectible ? collectible.defaultLevel() : 0);
                state.used = tag.getBoolean(USED);
                int minLevel = 0;
                int maxLevel = 1;
                if (item instanceof CollectibleLike collectible) {
                    minLevel = collectible.minLevel();
                    maxLevel = collectible.maxLevel();
                }
                state.layer = Mth.clamp(tag.getInt(LAYER), minLevel, maxLevel);
                state.cooldownEndTick = tag.getLong(COOLDOWN);
                state.cooldownDuration = tag.getLong(DURATION);
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
        private final Collectibles owner;
        private final @Nullable ServerPlayer player;

        Cooldown(Collectibles owner, @Nullable ServerPlayer player) {
            this.owner = owner;
            this.player = player;
        }

        private @Nullable ServerPlayer getPlayer() {
            return this.player;
        }

        private void setCooldownEndTick(Item item, Long endTick) {
            owner.ensureState(item).cooldownEndTick = endTick;
        }

        private void setCooldownEndTick(Holder<Item> item, Long endTick) {
            setCooldownEndTick(item.value(), endTick);
        }

        public void setCooldownEndTick(Item item, MinecraftServer server, Long offsetTicks) {
            CollectibleState state = owner.ensureState(item);
            state.cooldownEndTick = server.overworld().getGameTime() + offsetTicks;
            state.cooldownDuration = offsetTicks;
            BabelNetwork.syncCollectibles(Objects.requireNonNull(getPlayer()));
        }

        public void setCooldownEndTick(Holder<Item> item, MinecraftServer server, Long offsetTicks) {
            setCooldownEndTick(item.value(), server, offsetTicks);
        }

        private void addCooldownEndTick(Item item, Long currentTick, Long addToEndTick) {
            var lastTick = getCooldownEndTick(item);
            if (lastTick == 0) {
                setCooldownEndTick(item, currentTick + addToEndTick);
            } else {
                setCooldownEndTick(item, lastTick + addToEndTick);
            }
            CollectibleState state = owner.ensureState(item);
            state.cooldownDuration = state.cooldownDuration + addToEndTick;
        }

        private void addCooldownEndTick(Holder<Item> item, Long currentTick, Long addToEndTick) {
            addCooldownEndTick(item.value(), currentTick, addToEndTick);
        }

        public void addCooldownEndTick(Item item, MinecraftServer server, Long addToEndTick) {
            addCooldownEndTick(item, server.overworld().getGameTime(), addToEndTick);
            BabelNetwork.syncCollectibles(Objects.requireNonNull(getPlayer()));
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
            return Math.max(currentTick - getCooldownEndTick(item), 0L);
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
            return currentTick > getCooldownEndTick(item);
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