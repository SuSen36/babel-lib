package com.susen36.babel.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.susen36.babel.BabelConfig;
import com.susen36.babel.api.event.CollectibleDropEvent;
import com.susen36.babel.collectible.CollectibleLike;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelEntityTypeTags;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CollectibleLootModifier extends LootModifier {
    private static final int MAX_DROPS = 3;
    // 实体击杀掉落概率：普通 0.1%，babel:elite 精英 2%，forge:bosses 首领必掉
    private static final float CHANCE_NORMAL = 0.001F;
    private static final float CHANCE_ELITE = 0.02F;
    private static final float CHANCE_GUARANTEED = 1.0F;

    public static final Supplier<Codec<CollectibleLootModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.create(instance -> codecStart(instance)
                    .apply(instance, CollectibleLootModifier::new)));

    public CollectibleLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        List<RegistryObject<Item>> pool = BabelCollectible.BABEL_COLLECTIBLES;
        String path = context.getQueriedLootTableId().getPath();
        if (path.startsWith("chests/")) {
            // chance 上限 0.99 防止配置为 1.0 时几何分布退化
            float chance = (float) Math.min(BabelConfig.collectibleLootChance, 0.99);
            if (chance > 0.0F) {
                for (int i = 0; i < MAX_DROPS; i++) {
                    if (context.getRandom().nextFloat() < chance) {
                        addWeightedDrop(generatedLoot, pool, context);
                    }
                }
            }
        } else if (path.startsWith("entities/")) {
            // 仅作用于实体死亡结算，避免箱子等其他来源重复掉落
            Entity victim = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (victim != null) {
                boolean boss = victim.getType().is(BabelEntityTypeTags.FORGE_BOSSES);
                float chance = boss ? CHANCE_GUARANTEED
                        : victim.getType().is(BabelEntityTypeTags.ELITE) ? CHANCE_ELITE : CHANCE_NORMAL;
                chance = CollectibleDropEvent.fire(context, boss, chance);
                if (context.getRandom().nextFloat() < chance) {
                    if (boss) {
                        List<RegistryObject<Item>> advanced = new ArrayList<>();
                        for (RegistryObject<Item> holder : pool) {
                            Item item = holder.get();
                            if (item instanceof CollectibleLike collectible
                                    && collectible.getTier() == CollectibleTiers.ADVANCED) {
                                advanced.add(holder);
                            }
                        }
                        if (!advanced.isEmpty()) {
                            generatedLoot.add(new ItemStack(advanced.get(
                                    context.getRandom().nextInt(advanced.size())).get()));
                        } else {
                            // 高级藏品池为空时退回普通加权池，保证 boss 必有掉落
                            addWeightedDrop(generatedLoot, pool, context);
                        }
                    } else {
                        addWeightedDrop(generatedLoot, pool, context);
                    }
                }
            }
        }
        return generatedLoot;
    }

    private static void addWeightedDrop(ObjectArrayList<ItemStack> loot, List<RegistryObject<Item>> pool, LootContext context) {
        int totalWeight = 0;
        for (RegistryObject<Item> holder : pool) {
            totalWeight += weightOf(holder.get());
        }
        if (totalWeight > 0) {
            int roll = context.getRandom().nextInt(totalWeight);
            for (RegistryObject<Item> holder : pool) {
                roll -= weightOf(holder.get());
                if (roll < 0) {
                    loot.add(new ItemStack(holder.get()));
                    break;
                }
            }
        }
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