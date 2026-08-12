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
    public static final Supplier<AttachmentType<Collectibles>> ATTACHMENT_COLLECTIBLE_LAYER =
            ATTACHMENTS
                    .register(
                            "collectible_layer", () -> AttachmentType.serializable(Collectibles::new)
                                    .copyOnDeath()
                                    .build()
                    );
    @NotNull
    private final HashSet<Holder<Item>> UsedCollectibles = new HashSet<>();

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    public static @NotNull String getIdByItem(@NotNull Holder<Item> item) {
        return Objects.requireNonNull(Collectibles.inverse().get(item));
    }

    public static @NotNull String getIdByItem(@NotNull Item item) {
        return Objects.requireNonNull(Collectibles.inverse().get(BuiltInRegistries.ITEM.createIntrusiveHolder(item)));
    }

    public boolean isUsed(@NotNull Holder<Item> item) {
        return UsedCollectibles.contains(item);
    }

    public boolean isUsed(@NotNull Item item) {
        return UsedCollectibles.contains(BuiltInRegistries.ITEM.createIntrusiveHolder(item));
    }

    public void markUsed(@NotNull Holder<Item> item) {
        UsedCollectibles.add(item);
    }

    public void unmarkUsed(@NotNull Holder<Item> item) {
        UsedCollectibles.remove(item);
    }

    public void unmarkUsed(@NotNull Item item) {
        UsedCollectibles.remove(BuiltInRegistries.ITEM.createIntrusiveHolder(item));
    }

    public void markUsed(@NotNull Item item) {
        UsedCollectibles.add(BuiltInRegistries.ITEM.createIntrusiveHolder(item));
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

        public HashBiMap<String, Integer> getLayer() {
            return Layer;
        }

        public int getLayer(@NotNull Holder<Item> item) {
            return Objects.requireNonNull(Layer.get(getIdByItem(item)));
        }

        public int getLayer(@NotNull Item item) {
            return Objects.requireNonNull(Layer.get(getIdByItem(item)));
        }

        private int getLayer(@NotNull String id) {
            return Objects.requireNonNull(Layer.get(id));
        }

        public void setLayer(@NotNull Holder<Item> item, @NotNull Integer layer) {
            Layer.put(getIdByItem(item), layer);
        }

        public void setLayer(@NotNull Item item, @NotNull Integer layer) {
            Layer.put(getIdByItem(item), layer);
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
}
