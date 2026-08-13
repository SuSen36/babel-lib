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

    private CollectibleBuilder(@NotNull String modid) {
        this.ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, modid);
    }

    public static CollectibleBuilder create(@NotNull String modid) {
        return new CollectibleBuilder(modid);
    }

    /**
     * 将收藏品注册表绑定到 mod 事件总线，并同步触发 {@link CollectibleRegisterEvent}。
     * <p>
     * 与 DeferredRegister 的用法一致：builder 与注册字段可在静态初始化期直接创建，
     * 总线绑定与事件触发延后到 {@code init} 阶段统一执行。
     */
    public void register(@NotNull IEventBus bus) {
        ITEMS.register(bus);
        bus.post(new CollectibleRegisterEvent(this));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean consumeSelf, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean consumeSelf, boolean canAlwaysUse, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, canAlwaysUse)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean consumeSelf, int useTicks, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean consumeSelf, int useTicks, boolean canAlwaysUse, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks, canAlwaysUse)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean consumeSelf, boolean canAlwaysUse, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, canAlwaysUse, levels)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean consumeSelf, int useTicks, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks, levels)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean consumeSelf, int useTicks, boolean canAlwaysUse, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks, canAlwaysUse, levels)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, boolean consumeSelf, int useTicks, boolean canAlwaysUse, CollectibleTiers tiers, CollectibleItem.Levels levels, CollectibleActivation activation, CollectibleItem.CollectibleEffect effect) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks, canAlwaysUse, tiers, levels, activation)));
    }

    public <T extends CollectibleItem.CustomCollectibleItem> DeferredHolder<Item, Item> registerCollectible(String name, Supplier<T> item) {
        return save(name, ITEMS.register(name, item));
    }

    private DeferredHolder<Item, Item> save(String name, DeferredHolder<Item, Item> item) {
        Collectibles.Collectibles.put(name, item);
        return item;
    }
}
