package com.susen36.babel.api.collectible;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** 向当前回调所属的命名空间声明收藏品。物品在 Forge 注册阶段创建。 */
public interface CollectibleRegistrar {
    @FunctionalInterface
    interface Effect {
        void onEffectStarted(ItemStack stack, Level level, Player player);
    }

    RegistryObject<Item> registerCollectible(String name, CollectibleSpec spec, Effect effect);

    /** 自定义物品必须是 Babel 收藏品基类的实例；在 Forge 注册阶段校验。 */
    RegistryObject<Item> registerCollectible(String name, Supplier<? extends Item> collectible);

    default RegistryObject<Item> registerCollectible(String name, int useTicks, boolean canAlwaysUse, Effect effect) {
        return registerCollectible(name, new CollectibleSpec(useTicks, canAlwaysUse, CollectibleSpec.Tier.NORMAL, 0, 1, 0, null), effect);
    }

    default RegistryObject<Item> registerCollectible(String name, Effect effect) {
        return registerCollectible(name, 25, false, effect);
    }

    default RegistryObject<Item> registerCollectible(String name, int useTicks, Effect effect) {
        return registerCollectible(name, useTicks, false, effect);
    }

    default RegistryObject<Item> registerCollectible(String name, boolean canAlwaysUse, Effect effect) {
        return registerCollectible(name, 25, canAlwaysUse, effect);
    }
}