package com.susen36.babel.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.susen36.babel.api.event.OriginiumIngotDropEvent;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelEntityTypeTags;
import com.susen36.babel.init.BabelItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class OriginiumIngotLootModifier extends LootModifier {
    private static final float CHANCE_NORMAL = 2.5F / 50.0F;
    private static final float CHANCE_ELITE = 3.5F / 50.0F;

    // Boss：100% 掉落，数量按原版险路恶敌 5~7
    private static final int BOSS_MIN_COUNT = 5;
    private static final int BOSS_MAX_COUNT = 7;

    // 箱子：参考普通关卡，概率 2/3，数量 2~3
    private static final float CHEST_CHANCE = 2.0F / 3.0F;
    private static final int CHEST_MIN_COUNT = 2;
    private static final int CHEST_MAX_COUNT = 3;

    public static final Supplier<Codec<OriginiumIngotLootModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.create(instance -> codecStart(instance)
                    .apply(instance, OriginiumIngotLootModifier::new)));

    public OriginiumIngotLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        String path = context.getQueriedLootTableId().getPath();
        if (path.startsWith("chests/")) {
            int count = 0;
            if (context.getRandom().nextFloat() < CHEST_CHANCE) {
                count = CHEST_MIN_COUNT
                        + context.getRandom().nextInt(CHEST_MAX_COUNT - CHEST_MIN_COUNT + 1);
            }
            Entity opener = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (opener instanceof Player player && Collectibles.isUsed(player, BabelCollectible.LUCKY_COIN)) {
                count += 2;
            }
            if (count > 0) {
                generatedLoot.add(new ItemStack(BabelItems.ORIGINIUM_INGOT.get(), count));
            }
        } else if (path.startsWith("entities/")) {
            Entity victim = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (victim != null) {
                boolean boss = victim.getType().is(BabelEntityTypeTags.FORGE_BOSSES);
                if (boss) {
                    // Boss：100% 掉落，数量 5~7
                    int count = BOSS_MIN_COUNT + context.getRandom().nextInt(BOSS_MAX_COUNT - BOSS_MIN_COUNT + 1);
                    generatedLoot.add(new ItemStack(BabelItems.ORIGINIUM_INGOT.get(), count));
                } else {
                    float chance = victim.getType().is(BabelEntityTypeTags.ELITE) ? CHANCE_ELITE : CHANCE_NORMAL;
                    if (context.getRandom().nextFloat() < chance) {
                        float multiplier = OriginiumIngotDropEvent.fire(context, 1.0F);
                        int count = Mth.floor(multiplier);
                        if (context.getRandom().nextFloat() < multiplier - count) {
                            count++;
                        }
                        if (count > 0) {
                            generatedLoot.add(new ItemStack(BabelItems.ORIGINIUM_INGOT.get(), count));
                        }
                    }
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public @NotNull Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}