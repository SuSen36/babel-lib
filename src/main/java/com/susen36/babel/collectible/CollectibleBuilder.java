package com.susen36.babel.collectible;

import com.susen36.babel.api.event.CollectibleRegisterEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CollectibleBuilder {
    private final DeferredRegister<Item> ITEMS;

    private CollectibleBuilder(@NotNull String modid, @NotNull IEventBus bus) {
        this.ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, modid);
        ITEMS.register(bus);
        bus.post(new CollectibleRegisterEvent(this));
    }

    public static CollectibleBuilder create(@NotNull String modid, @NotNull IEventBus bus) {
        return new CollectibleBuilder(modid, bus);
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean canAlwaysUse, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, canAlwaysUse)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, int useTicks, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, useTicks)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, int useTicks, boolean canAlwaysUse, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, useTicks, canAlwaysUse)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean canAlwaysUse, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, canAlwaysUse, levels)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, int useTicks, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, useTicks, levels)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, int useTicks, boolean canAlwaysUse, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, useTicks, canAlwaysUse, levels)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, int useTicks, boolean canAlwaysUse, CollectibleTiers tiers, CollectibleItem.Levels levels, CollectibleActivation activation, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, useTicks, canAlwaysUse, tiers, levels, activation)));
    }

    public <T extends CollectibleItem.CustomCollectibleItem> DeferredHolder<Item, Item> registerCollectible(String name, Supplier<T> item) {
        return save(name, ITEMS.register(name, item));
    }

    private DeferredHolder<Item, Item> save(String name, DeferredHolder<Item, Item> item) {
        Collectibles.Collectibles.put(name, item);
        return item;
    }
}
