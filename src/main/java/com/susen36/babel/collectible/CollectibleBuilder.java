package com.susen36.babel.collectible;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CollectibleBuilder {
    @NotNull
    private String MODID = "fuck idea, how could this var be null?";
    private final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public CollectibleBuilder(@NotNull String modid) {
        this.MODID = modid;
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf, int useTicks) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks)));
    }

    public <T extends CollectibleItem.CustomCollectibleItem> DeferredHolder<Item, Item> registerCollectible(String name, Supplier<T> item) {
        return save(name, ITEMS.register(name, item));
    }

    private DeferredHolder<Item, Item> save(String name, DeferredHolder<Item, Item> item) {
        Collectibles.Collectibles.put(name, item);
        return item;
    }
}
