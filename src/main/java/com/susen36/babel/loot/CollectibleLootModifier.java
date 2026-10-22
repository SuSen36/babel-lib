package com.susen36.babel.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.susen36.babel.BabelConfig;
import com.susen36.babel.collectible.CollectibleLike;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelCollectible;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class CollectibleLootModifier extends LootModifier {
    private static final int MAX_DROPS = 3;

    public static final Supplier<Codec<CollectibleLootModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.create(instance -> codecStart(instance)
                    .apply(instance, CollectibleLootModifier::new)));

    public CollectibleLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 仅作用于宝箱战利品表（chests/ 路径）；chance 上限 0.99 防止配置为 1.0 时几何分布退化
        List<RegistryObject<Item>> pool = BabelCollectible.BABEL_COLLECTIBLES;
        float chance = (float) Math.min(BabelConfig.collectibleLootChance, 0.99);
        if (context.getQueriedLootTableId().getPath().startsWith("chests/")
                && chance > 0.0F && !pool.isEmpty()) {
            int totalWeight = 0;
            for (RegistryObject<Item> holder : pool) {
                totalWeight += weightOf(holder.get());
            }
            for (int i = 0; i < MAX_DROPS && totalWeight > 0; i++) {
                if (context.getRandom().nextFloat() < chance) {
                    int roll = context.getRandom().nextInt(totalWeight);
                    for (RegistryObject<Item> holder : pool) {
                        roll -= weightOf(holder.get());
                        if (roll < 0) {
                            generatedLoot.add(new ItemStack(holder.get()));
                            break;
                        }
                    }
                }
            }
        }
        return generatedLoot;
    }

    private static int weightOf(Item item) {
        CollectibleTiers tier = item instanceof CollectibleLike collectible
                ? collectible.getTier() : CollectibleTiers.NORMAL;
        return switch (tier) {
            case CURSED -> 2;
            case RARE -> 4;
            case ADVANCED -> 1;
            default -> 10;
        };
    }

    @Override
    public @NotNull Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}