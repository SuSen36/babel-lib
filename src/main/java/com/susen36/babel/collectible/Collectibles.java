package com.susen36.babel.collectible;

import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import com.susen36.babel.BabelMod;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Collectibles implements INBTSerializable<ListTag> {
    @NotNull
    public static final HashBiMap<String, RegistryObject<Item>> Collectibles = HashBiMap.create();

    private final HashSet<Item> UsedCollectibles = new HashSet<>();

    Collectibles() {
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
            event.addListener(cap::invalidate);
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
        return CollectibleCapability.of(player).getUsed();
    }

    public static @NotNull Collectibles.Layer getLayerData(@NotNull Player player) {
        return CollectibleCapability.of(player).getLayer();
    }

    public static @NotNull Collectibles.Cooldown getCooldownData(@NotNull Player player) {
        return CollectibleCapability.of(player).getCooldown();
    }

    public static void addLayerAndSync(@NotNull Player player, @NotNull Item item, int delta) {
        if (!player.level().isClientSide) {
            getLayerData(player).addLayer(item, delta);
            BabelNetwork.syncCollectibles(player);
        }
    }

    public static Tag serializeUsed(@NotNull Player player) {
        return getData(player).serializeNBT();
    }

    public static void deserializeUsed(@NotNull Player player, @NotNull ListTag tag) {
        getData(player).deserializeNBT(tag);
    }

    public static Tag serializeLayer(@NotNull Player player) {
        return getLayerData(player).serializeNBT();
    }

    public static void deserializeLayer(@NotNull Player player, @NotNull CompoundTag tag) {
        getLayerData(player).deserializeNBT(tag);
    }

    public static Tag serializeCooldown(@NotNull Player player) {
        return getCooldownData(player).serializeNBT();
    }

    public static void deserializeCooldown(@NotNull Player player, @NotNull CompoundTag tag) {
        getCooldownData(player).deserializeNBT(tag);
    }

    public static @NotNull String getIdByItem(@NotNull Holder<Item> item) {
        return getIdByItem(item.value());
    }

    public static @NotNull String getIdByItem(@NotNull Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        RegistryObject<Item> entry = key == null ? null : Collectibles.get(key.getPath());
        if (entry != null && entry.getId().equals(key)) {
            return key.getPath();
        }
        LogUtils.getLogger().error("try to query {} in Collectibles, but not matched.", item);
        return "null";
    }

    public boolean isUsed(@NotNull Holder<Item> item) {
        return isUsed(item.value());
    }

    public boolean isUsed(@NotNull Item item) {
        return UsedCollectibles.contains(item);
    }

    public void markUsed(@NotNull Holder<Item> item) {
        markUsed(item.value());
    }

    public void markUsed(@NotNull Item item) {
        UsedCollectibles.add(item);
    }

    public void unmarkUsed(@NotNull Holder<Item> item) {
        unmarkUsed(item.value());
    }

    public void unmarkUsed(@NotNull Item item) {
        UsedCollectibles.remove(item);
    }

    public @NotNull HashSet<Item> getUsed() {
        return UsedCollectibles;
    }

    public static void expireUsedCollectibles(@NotNull Player player) {
        if (!player.level().isClientSide) {
            var usedCollectibles = getData(player);
            var cooldown = getCooldownData(player);
            var layers = getLayerData(player);
            long currentTick = player.level().getServer().overworld().getGameTime();
            boolean changed = false;
            for (Item item : new ArrayList<>(usedCollectibles.getUsed())) {
                long endTick = cooldown.getCooldownEndTick(item);
                // endTick 为 0 表示冷却附件里没有该物品的记录，这类数据不判定过期
                if (endTick > 0 && currentTick > endTick) {
                    usedCollectibles.unmarkUsed(item);
                    layers.removeLayer(item);
                    changed = true;
                }
            }
            if (changed) {
                BabelNetwork.syncCollectibles(player);
            }
        }
    }

    private @NotNull HashSet<String> getUsedCollectibleIds() {
        var ids = new HashSet<String>();
        for (var item : UsedCollectibles) {
            ids.add(Objects.requireNonNull(getIdByItem(item)));
        }
        return ids;
    }

    private @NotNull HashSet<Item> getUsedCollectibleFormIds(@NotNull HashSet<String> ids) {
        var uc = new HashSet<Item>();
        for (var id : ids) {
            RegistryObject<Item> entry = Collectibles.get(id);
            if (entry != null && entry.get() != null) {
                uc.add(entry.get());
            } else {
                BabelMod.LOGGER.warn("Skipping unknown collectible id in saved data: {}", id);
            }
        }
        return uc;
    }

    @Override
    public ListTag serializeNBT() {
        var list = new ListTag();
        for (String collectiblesId : getUsedCollectibleIds()) {
            list.add(StringTag.valueOf(collectiblesId));
        }
        return list;
    }

    @Override
    public void deserializeNBT(@NotNull ListTag list) {
        var ids = new HashSet<String>();
        for (var id : list) {
            ids.add(id.getAsString());
        }
        UsedCollectibles.clear();
        UsedCollectibles.addAll(getUsedCollectibleFormIds(ids));
    }

    public static class Layer implements INBTSerializable<CompoundTag> {
        private static final String KEY = "key";
        private static final String VALUE = "value";
        public final HashMap<String, Integer> layer = new HashMap<>();

        Layer() {
        }

        public HashMap<String, Integer> getLayer() {
            return layer;
        }

        public int getLayer(@NotNull Holder<Item> item) {
            Integer layer = this.layer.get(getIdByItem(item));
            if (layer != null) {
                return layer;
            }
            if (item.value() instanceof CollectibleLike collectible) {
                return collectible.defaultLevel();
            }
            return 0;
        }

        public int getLayer(@NotNull Item item) {
            Integer layer = this.layer.get(getIdByItem(item));
            if (layer != null) {
                return layer;
            }
            if (item instanceof CollectibleLike collectible) {
                return collectible.defaultLevel();
            }
            return 0;
        }

        public void setLayer(@NotNull Holder<Item> item, @NotNull Integer layer) {
            int minLevel = 0;
            int maxLevel = 1;
            if (item.value() instanceof CollectibleLike collectible) {
                minLevel = collectible.minLevel();
                maxLevel = collectible.maxLevel();
            }
            this.layer.put(getIdByItem(item), Mth.clamp(layer, minLevel, maxLevel));
        }

        public void setLayer(@NotNull Item item, @NotNull Integer layer) {
            int minLevel = 0;
            int maxLevel = 1;
            if (item instanceof CollectibleLike collectible) {
                minLevel = collectible.minLevel();
                maxLevel = collectible.maxLevel();
            }
            this.layer.put(getIdByItem(item), Mth.clamp(layer, minLevel, maxLevel));
        }

        public void addLayer(@NotNull Item item, @NotNull Integer layer) {
            setLayer(item, getLayer(item) + layer);
        }

        public void addLayer(@NotNull Holder<Item> item, @NotNull Integer layer) {
            setLayer(item, getLayer(item) + layer);
        }

        public void removeLayer(@NotNull Item item) {
            this.layer.remove(getIdByItem(item));
        }

        public void removeLayer(@NotNull Holder<Item> item) {
            this.layer.remove(getIdByItem(item));
        }

        @Override
        public CompoundTag serializeNBT() {
            var root = new CompoundTag();
            var keys = new ListTag();
            var values = new IntArrayTag(new ArrayList<>(layer.values()));
            for (var entry : layer.entrySet()) {
                keys.add(StringTag.valueOf(entry.getKey()));
            }
            root.put(KEY, keys);
            root.put(VALUE, values);
            return root;
        }

        @Override
        public void deserializeNBT(@NotNull CompoundTag root) {
            var tempKeys = root.get(KEY);
            var tempValues = root.get(VALUE);
            if (!root.contains(KEY) || !root.contains(VALUE)) {
                BabelMod.LOGGER.error("Missing keys or values in Layer NBT");
            } else if (tempKeys instanceof ListTag keys && tempValues instanceof IntArrayTag values) {
                if (keys.size() == values.size()) {
                    layer.clear();

                    for (int i = 0; i < keys.size(); i++) {
                        String key = keys.getString(i);

                        int value;
                        var tag = values.get(i);
                        if (tag == null) {
                            BabelMod.LOGGER.error("found a null in values{}.", values);
                            continue;
                        }
                        if (tag.getId() == IntTag.TAG_INT) {
                            value = tag.getAsInt();
                        } else {
                            BabelMod.LOGGER.error("Unexpected tag type in Layer values: {}", tag.getClass().getCanonicalName());
                            continue;
                        }

                        if (Collectibles.get(key) == null) {
                            BabelMod.LOGGER.warn("Dropping invalid collectible id in Layer NBT: {}", key);
                            continue;
                        }
                        if (!layer.containsKey(key)) {
                            layer.put(key, value);
                        } else {
                            layer.put(key, value);
                            BabelMod.LOGGER.error("Duplicate keypair found in Layer NBT: ({}, {}), Overrided old.", key, value);
                        }
                    }
                } else
                    BabelMod.LOGGER.error("the size of keys and values are not equals : keys: {}, values: {}", keys.size(), values.size());
            } else {
                BabelMod.LOGGER.error("wanted (ListTag, IntArrayTag), matched: ({}, {})", tempKeys == null ? "null" : tempKeys.getClass().getCanonicalName(), tempValues == null ? "null" : tempValues.getClass().getCanonicalName());
            }
        }
    }

    public static class Cooldown implements INBTSerializable<CompoundTag> {
        private static final String KEY = "key";
        private static final String VALUE = "value";

        private final HashMap<String, Long> cooldown = new HashMap<>();
        private @Nullable ServerPlayer player;

        Cooldown(@Nullable ServerPlayer player) {
            this.player = player;
        }

        private @Nullable ServerPlayer getPlayer() {
            return this.player;
        }

        private void setCooldownEndTick(Item item, Long endTick) {
            cooldown.put(getIdByItem(item), endTick);
        }

        private void setCooldownEndTick(Holder<Item> item, Long endTick) {
            setCooldownEndTick(item.value(), endTick);
        }

        public void setCooldownEndTick(Item item, MinecraftServer server, Long offsetTicks) {
            setCooldownEndTick(item, server.overworld().getGameTime() + offsetTicks);
            ServerPlayer syncTarget = Objects.requireNonNull(getPlayer());
            BabelNetwork.syncCollectibles(syncTarget);
            BabelNetwork.syncCooldown(syncTarget, item, offsetTicks, server.overworld().getGameTime());
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
        }

        private void addCooldownEndTick(Holder<Item> item, Long currentTick, Long addToEndTick) {
            addCooldownEndTick(item.value(), currentTick, addToEndTick);
        }

        public void addCooldownEndTick(Item item, MinecraftServer server, Long addToEndTick) {
            addCooldownEndTick(item, server.overworld().getGameTime(), addToEndTick);
        }

        public void addCooldownEndTick(Holder<Item> item, MinecraftServer server, Long addToEndTick) {
            addCooldownEndTick(item.value(), server, addToEndTick);
        }

        public long getCooldownEndTick(Item item) {
            return cooldown.getOrDefault(getIdByItem(item), 0L);
        }

        public long getCooldownEndTick(Holder<Item> item) {
            return getCooldownEndTick(item.value());
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

        @Override
        public CompoundTag serializeNBT() {
            var root = new CompoundTag();
            var keys = new ListTag();
            var values = new LongArrayTag(new ArrayList<>(cooldown.values()));

            for (var entry : cooldown.entrySet()) {
                keys.add(StringTag.valueOf(entry.getKey()));
            }

            root.put(KEY, keys);
            root.put(VALUE, values);
            return root;
        }

        @Override
        public void deserializeNBT(@NotNull CompoundTag root) {
            var tempKeys = root.get(KEY);
            var tempValues = root.get(VALUE);
            if (!root.contains(KEY) || !root.contains(VALUE)) {
                BabelMod.LOGGER.error("Missing keys or values in Cooldown NBT");
            } else if (tempKeys instanceof ListTag keys && tempValues instanceof LongArrayTag values) {
                if (keys.size() != values.size()) {
                    BabelMod.LOGGER.error("the size of keys and values are not equals : keys: {}, values: {}", keys.size(), values.size());
                } else {
                    cooldown.clear();

                    for (int i = 0; i < keys.size(); i++) {
                        String key = keys.getString(i);

                        long value;
                        Tag tag = values.get(i);
                        if (tag == null) {
                            BabelMod.LOGGER.error("found a null in values{}.", values);
                            continue;
                        }
                        if (tag instanceof LongTag longTag) {
                            value = longTag.getAsLong();
                        } else {
                            BabelMod.LOGGER.error("Unexpected tag type in Cooldown values: {}", tag.getClass().getCanonicalName());
                            continue;
                        }

                        if (Collectibles.get(key) == null) {
                            BabelMod.LOGGER.warn("Dropping invalid collectible id in Cooldown NBT: {}", key);
                            continue;
                        }
                        if (!cooldown.containsKey(key)) {
                            cooldown.put(key, value);
                        } else {
                            cooldown.put(key, value);
                            BabelMod.LOGGER.error("Duplicate keypair found in Cooldown NBT: ({}, {}), Overrided old.", key, value);
                        }
                    }
                }
            } else {
                BabelMod.LOGGER.error("wanted (ListTag, LongArrayTag), matched: ({}, {})", tempKeys == null ? "null" : tempKeys.getClass().getCanonicalName(), tempValues == null ? "null" : tempValues.getClass().getCanonicalName());
            }
        }
    }
}