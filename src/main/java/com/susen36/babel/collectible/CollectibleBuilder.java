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

    /** 创建收藏品构建器。无需事件总线，可在静态字段初始化阶段安全调用。 */
    public static CollectibleBuilder create(@NotNull String modid) {
        return new CollectibleBuilder(modid);
    }

    /**
     * 将物品注册表绑定到 mod 事件总线，并触发 {@link CollectibleRegisterEvent} 供各方注册收藏品。
     * 应在 mod 构造器中调用。
     */
    public void register(@NotNull IEventBus bus) {
        ITEMS.register(bus);
        bus.post(new CollectibleRegisterEvent(this));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf, int useTicks) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf, int useTicks, CollectibleTiers tier) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks, tier)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf, int useTicks, CollectibleTiers tier, int minLevel, int maxLevel, int defaultLevel) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks, tier, minLevel, maxLevel, defaultLevel)));
    }

    public DeferredHolder<Item, Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect, boolean consumeSelf, int useTicks, CollectibleTiers tier, int minLevel, int maxLevel, int defaultLevel, CollectibleActivation activation) {
        return save(name, ITEMS.register(name, () -> new CollectibleItem(effect, consumeSelf, useTicks, tier, minLevel, maxLevel, defaultLevel, activation)));
    }

    public <T extends CollectibleItem.CustomCollectibleItem> DeferredHolder<Item, Item> registerCollectible(String name, Supplier<T> item) {
        return save(name, ITEMS.register(name, item));
    }

    private DeferredHolder<Item, Item> save(String name, DeferredHolder<Item, Item> item) {
        Collectibles.Collectibles.put(name, item);
        return item;
    }
}
