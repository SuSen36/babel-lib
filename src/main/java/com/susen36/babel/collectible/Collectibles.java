package com.susen36.babel.collectible;

import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import com.susen36.babel.BabelMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static com.susen36.babel.BabelMod.MODID;

public class Collectibles implements INBTSerializable<ListTag> {
    @NotNull
    public static final HashBiMap<String, Holder<Item>> Collectibles = HashBiMap.create();
    @NotNull
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
    @NotNull
    public static final Supplier<AttachmentType<Collectibles>> ATTACHMENT_COLLECTIBLE =
            ATTACHMENTS
                    .register(
                            "used_collectibles", () -> AttachmentType.serializable(Collectibles::new)
                                    .copyOnDeath()
                                    .build()
                    );
    @NotNull
    public static final Supplier<AttachmentType<Layer>> ATTACHMENT_COLLECTIBLE_LAYER =
            ATTACHMENTS
                    .register(
                            "collectible_layer", () -> AttachmentType.serializable(Layer::new)
                                    .copyOnDeath()
                                    .build()
                    );
    @NotNull
    public static final Supplier<AttachmentType<Cooldown>> ATTACHMENT_COLLECTIBLE_COOLDOWN =
            ATTACHMENTS
                    .register(
                            "collectible_cooldown", () -> AttachmentType.serializable(Cooldown::new)
                                    .copyOnDeath()
                                    .build()
                    );
    @NotNull
    private final HashSet<Holder<Item>> UsedCollectibles = new HashSet<>();

    private Collectibles() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    public static @NotNull String getIdByItem(@NotNull Holder<Item> item) {
        return Objects.requireNonNullElseGet(Collectibles.inverse().get(item), () -> {
            LogUtils.getLogger().error("try to query {} in Collectibles, but not matched.", item);
            return "null";
        });
    }

    public static @NotNull String getIdByItem(@NotNull Item item) {
        return Objects.requireNonNullElseGet(Collectibles.inverse().get(BuiltInRegistries.ITEM.wrapAsHolder(item)), () -> {
            LogUtils.getLogger().error("try to query {} in Collectibles, but not matched.", item);
            return "null";
        });
    }

    public boolean isUsed(@NotNull Holder<Item> item) {
        return UsedCollectibles.contains(item);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isUsed(@NotNull Item item) {
        return UsedCollectibles.contains(BuiltInRegistries.ITEM.wrapAsHolder(item));
    }

    public void markUsed(@NotNull Holder<Item> item) {
        UsedCollectibles.add(item);
    }

    public void markUsed(@NotNull Item item) {
        UsedCollectibles.add(BuiltInRegistries.ITEM.wrapAsHolder(item));
    }

    public void unmarkUsed(@NotNull Holder<Item> item) {
        UsedCollectibles.remove(item);
    }

    public void unmarkUsed(@NotNull Item item) {
        UsedCollectibles.remove(BuiltInRegistries.ITEM.wrapAsHolder(item));
    }


    public @NotNull HashSet<Holder<Item>> getUsed() {
        return UsedCollectibles;
    }

    private @NotNull HashSet<String> getUsedCollectibleIds() {
        var ids = new HashSet<String>();
        for (var item : UsedCollectibles) {
            ids.add(Objects.requireNonNull(Collectibles.inverse().get(item)));
        }
        return ids;
    }

    private @NotNull HashSet<Holder<Item>> getUsedCollectibleFormIds(@NotNull HashSet<String> ids) {
        var uc = new HashSet<Holder<Item>>();
        for (var id : ids) {
            uc.add(Objects.requireNonNull(Collectibles.get(id)));
        }
        return uc;
    }

    @Override
    public ListTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        var list = new ListTag();
        for (String collectiblesId : getUsedCollectibleIds()) {
            list.add(StringTag.valueOf(collectiblesId));
        }
        return list;
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider provider, @NotNull ListTag list) {
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
        public final HashBiMap<String, Integer> layer = HashBiMap.create();

        private Layer() {
        }

        private HashBiMap<String, Integer> getLayer() {
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

        @Override
        public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
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
        public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag root) {
            if (!root.contains(KEY) || !root.contains(VALUE)) {
                BabelMod.LOGGER.error("Missing keys or values in Layer NBT");
                return;
            }
            var tempKeys = root.get(KEY);
            var tempValues = root.get(VALUE);
            if (tempKeys instanceof ListTag keys && tempValues instanceof IntArrayTag values) {
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
                        if (tag instanceof IntTag intTag) {
                            value = intTag.getAsInt();
                        } else {
                            BabelMod.LOGGER.error("Unexpected tag type in Layer values: {}", tag.getClass().getCanonicalName());
                            continue;
                        }

                        if (!layer.containsKey(key) && !layer.containsValue(value)) {
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

        public final HashMap<String, Long> cooldown = new HashMap<>();

        private Cooldown() {
        }

        public void setCooldown(@NotNull Item item, long durationSeconds) {
            long lastTick = durationSeconds * 20L;
            cooldown.put(getIdByItem(item), lastTick);
        }

        public void setCooldown(@NotNull Holder<Item> item, long durationSeconds) {
            long lastTick = durationSeconds * 20L;
            cooldown.put(getIdByItem(item), lastTick);
        }

        public long getRemainingTicks(@NotNull Item item) {
            Long lastTick = cooldown.get(getIdByItem(item));
            if (lastTick == null) return 0;
            return lastTick;
        }

        public long getRemainingTicks(@NotNull Holder<Item> item) {
            Long lastTick = cooldown.get(getIdByItem(item));
            if (lastTick == null) return 0;
            return lastTick;
        }

        public Set<net.minecraft.core.Holder<Item>> getCooldowns() {
            var items = new HashSet<Holder<Item>>();
            for (var id : cooldown.keySet()) {
                items.add(Collectibles.get(id));
            }
            return items;
        }

        public int getCooldown(@NotNull Item item) {
            return Math.toIntExact(getRemainingTicks(item) / 20);
        }

        public int getCooldown(@NotNull Holder<Item> item) {
            return Math.toIntExact(getRemainingTicks(item) / 20);
        }

        public void addCooldown(@NotNull Item item, float durationSeconds) {
            long lastTick = getCooldown(item);
            long processd;
            if (durationSeconds < 0) {
                processd = Math.round(Math.floor(durationSeconds));
            } else {
                processd = Math.round(durationSeconds);
            }
            setCooldown(item, lastTick + processd * 20L);
        }

        public void addCooldown(@NotNull Holder<Item> item, float durationSeconds) {
            long lastTick = getCooldown(item);
            long processd;
            if (durationSeconds < 0) {
                processd = Math.round(Math.floor(durationSeconds));
            } else {
                processd = Math.round(durationSeconds);
            }
            setCooldown(item, lastTick + processd * 20L);
        }

        public boolean isReady(@NotNull Item item) {
            return getRemainingTicks(item) <= 0;
        }

        public boolean isReady(@NotNull Holder<Item> item) {
            return getRemainingTicks(item) <= 0;
        }

        public void removeCooldown(@NotNull Item item) {
            cooldown.remove(getIdByItem(item));
        }

        public void removeCooldown(@NotNull Holder<Item> item) {
            cooldown.remove(getIdByItem(item));
        }

        public void updateCooldown(@NotNull Item item) {
            if (getIdByItem(item).equals("null"))
                LogUtils.getLogger().error("try to update a not existly collectible: {}", item);
            cooldown.merge(getIdByItem(item), -1L, Long::sum);
        }

        public void updateCooldown(@NotNull Holder<Item> item) {
            if (getIdByItem(item).equals("null"))
                LogUtils.getLogger().error("try to update a not existly collectible: {}", item);
            cooldown.merge(getIdByItem(item), -1L, Long::sum);
        }

        public void clearIfExpired(@NotNull Item item) {
            if (getRemainingTicks(item) <= 0) {
                removeCooldown(item);
            }
        }

        public void clearIfExpired(@NotNull Holder<Item> item) {
            if (getRemainingTicks(item) <= 0) {
                removeCooldown(item);
            }
        }

        public void clearAllExpired() {
            for (var item : getCooldowns()) {
                clearIfExpired(item);
                updateCooldown(item);
            }
        }

        @Override
        public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
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
        public void deserializeNBT(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag root) {
            if (!root.contains(KEY) || !root.contains(VALUE)) {
                BabelMod.LOGGER.error("Missing keys or values in Cooldown NBT");
                return;
            }

            var tempKeys = root.get(KEY);
            var tempValues = root.get(VALUE);

            if (tempKeys instanceof ListTag keys && tempValues instanceof LongArrayTag values) {
                if (keys.size() != values.size()) {
                    BabelMod.LOGGER.error("the size of keys and values are not equals : keys: {}, values: {}", keys.size(), values.size());
                    return;
                }

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

                    if (!cooldown.containsKey(key) && !cooldown.containsValue(value)) {
                        cooldown.put(key, value);
                    } else {
                        cooldown.put(key, value);
                        BabelMod.LOGGER.error("Duplicate keypair found in Cooldown NBT: ({}, {}), Overrided old.", key, value);
                    }
                }
            } else {
                BabelMod.LOGGER.error("wanted (ListTag, LongArrayTag), matched: ({}, {})", tempKeys == null ? "null" : tempKeys.getClass().getCanonicalName(), tempValues == null ? "null" : tempValues.getClass().getCanonicalName());
            }
        }
    }
}
