package com.susen36.babel.collectible;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CollectibleBuilder {
    @NotNull
    private String MODID = "fuck idea, how could this var be null?";

    public CollectibleBuilder(@NotNull String modid) {
        this.MODID = modid;
    }

    private final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public Supplier<Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf) {
        return ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf));
    }

    public Supplier<Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf, int useTicks) {
        return ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks));
    }

    public <T extends CollectibleItem.CustomCollectibleItem> Supplier<Item> registerCollectible(String name, Supplier<T> item) {
        return ITEMS.register(name, item);
    }
}
