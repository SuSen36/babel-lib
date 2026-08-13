package com.susen36.babel.collectible;

import com.google.common.collect.HashBiMap;
import com.susen36.babel.BabelMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
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

import java.util.HashSet;
import java.util.Objects;
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

    public static Collectibles getInstance() {
        return new Collectibles();
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    public static @NotNull String getIdByItem(@NotNull Holder<Item> item) {
        return Objects.requireNonNull(Collectibles.inverse().get(item));
    }

    public static @NotNull String getIdByItem(@NotNull Item item) {
        return Objects.requireNonNull(Collectibles.inverse().get(BuiltInRegistries.ITEM.wrapAsHolder(item)));
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
        private static final String VALUE = "vaule";
        public final HashBiMap<String, Integer> Layer = HashBiMap.create();

        private Layer() {
        }

        public static Layer getInstance() {
            return new Layer();
        }

        private HashBiMap<String, Integer> getLayer() {
            return Layer;
        }

        public int getLayer(@NotNull Holder<Item> item) {
            Integer layer = Layer.get(getIdByItem(item));
            if (layer != null) {
                return layer;
            }
            if (item.value() instanceof CollectibleLike collectible) {
                return collectible.defaultLevel();
            }
            return 0;
        }

        public int getLayer(@NotNull Item item) {
            Integer layer = Layer.get(getIdByItem(item));
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
            Layer.put(getIdByItem(item), Mth.clamp(layer, minLevel, maxLevel));
        }

        public void setLayer(@NotNull Item item, @NotNull Integer layer) {
            int minLevel = 0;
            int maxLevel = 1;
            if (item instanceof CollectibleLike collectible) {
                minLevel = collectible.minLevel();
                maxLevel = collectible.maxLevel();
            }
            Layer.put(getIdByItem(item), Mth.clamp(layer, minLevel, maxLevel));
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
            var root = new CompoundTag();
            var keys = new ListTag();
            var values = new ListTag();
            for (var entry : Layer.entrySet()) {
                keys.add(StringTag.valueOf(entry.getKey()));
                values.add(IntTag.valueOf(entry.getValue()));
            }
            root.put(KEY, keys);
            root.put(VALUE, values);
            return root;
        }

        @Override
        public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag root) {
            Layer.clear();
            if (!root.contains(KEY, Tag.TAG_LIST) || !root.contains(VALUE, Tag.TAG_LIST)) {
                BabelMod.LOGGER.error("Missing keys or values in Layer NBT");
                return;
            }
            var tempKeys = root.get(KEY);
            var tempValues = root.get(VALUE);
            if (tempKeys instanceof ListTag keys && tempValues instanceof ListTag values) {
                if (keys.size() == values.size())
                    for (int i = 0; i < keys.size(); i++) {
                        var key = keys.getString(i);
                        var value = values.getInt(i);
                        if (!Layer.containsKey(key) && !Layer.containsValue(value)) {
                            Layer.put(key, value);
                        } else {
                            BabelMod.LOGGER.error("Duplicate keypair found in Layer NBT: {} -> {}", key, value);
                        }
                    }
                else
                    BabelMod.LOGGER.error("the size of keys and values are not equals : keys: {}, values: {}", keys.size(), values.size());
            } else if (tempKeys != null) {
                BabelMod.LOGGER.error("wanted ListTag, matched: {}", tempKeys.getClass().getName());
            } else BabelMod.LOGGER.error("Missing 'keys' or 'values' tag in Layer NBT");
        }
    }

    public static class Cooldown implements INBTSerializable<CompoundTag> {
        private static final String KEY = "key";
        private static final String VALUE = "vaule";
        public final HashBiMap<String, Integer> Cooldown = HashBiMap.create();

        private Cooldown() {
        }

        public static Cooldown getInstance() {
            return new Cooldown();
        }

        private HashBiMap<String, Integer> getCooldown() {
            return Cooldown;
        }

        public int getCooldown(@NotNull Holder<Item> item) {
            Integer layer = Cooldown.get(getIdByItem(item));
            if (layer != null) {
                return layer;
            }
            if (item.value() instanceof CollectibleLike collectible) {
                return collectible.defaultLevel();
            }
            return 0;
        }

        public int getCooldown(@NotNull Item item) {
            Integer layer = Cooldown.get(getIdByItem(item));
            if (layer != null) {
                return layer;
            }
            if (item instanceof CollectibleLike collectible) {
                return collectible.defaultLevel();
            }
            return 0;
        }

        public void setCooldown(@NotNull Holder<Item> item, @NotNull Integer layer) {
            int minLevel = 0;
            int maxLevel = 1;
            if (item.value() instanceof CollectibleLike collectible) {
                minLevel = collectible.minLevel();
                maxLevel = collectible.maxLevel();
            }
            Cooldown.put(getIdByItem(item), Mth.clamp(layer, minLevel, maxLevel));
        }

        public void setCooldown(@NotNull Item item, @NotNull Integer layer) {
            int minLevel = 0;
            int maxLevel = 1;
            if (item instanceof CollectibleLike collectible) {
                minLevel = collectible.minLevel();
                maxLevel = collectible.maxLevel();
            }
            Cooldown.put(getIdByItem(item), Mth.clamp(layer, minLevel, maxLevel));
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
            var root = new CompoundTag();
            var keys = new ListTag();
            var values = new ListTag();
            for (var entry : Cooldown.entrySet()) {
                keys.add(StringTag.valueOf(entry.getKey()));
                values.add(IntTag.valueOf(entry.getValue()));
            }
            root.put(KEY, keys);
            root.put(VALUE, values);
            return root;
        }

        @Override
        public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag root) {
            Cooldown.clear();
            if (!root.contains(KEY, Tag.TAG_LIST) || !root.contains(VALUE, Tag.TAG_LIST)) {
                BabelMod.LOGGER.error("Missing keys or values in Cooldown NBT");
                return;
            }
            var tempKeys = root.get(KEY);
            var tempValues = root.get(VALUE);
            if (tempKeys instanceof ListTag keys && tempValues instanceof ListTag values) {
                if (keys.size() == values.size())
                    for (int i = 0; i < keys.size(); i++) {
                        var key = keys.getString(i);
                        var value = values.getInt(i);
                        if (!Cooldown.containsKey(key) && !Cooldown.containsValue(value)) {
                            Cooldown.put(key, value);
                        } else {
                            BabelMod.LOGGER.error("Duplicate keypair found in Cooldown NBT: {} -> {}", key, value);
                        }
                    }
                else
                    BabelMod.LOGGER.error("the size of keys and values are not equals : keys: {}, values: {}", keys.size(), values.size());
            } else if (tempKeys != null) {
                BabelMod.LOGGER.error("wanted ListTag, matched: {}", tempKeys.getClass().getName());
            } else BabelMod.LOGGER.error("Missing 'keys' or 'values' tag in Cooldown NBT");
        }
    }
}
