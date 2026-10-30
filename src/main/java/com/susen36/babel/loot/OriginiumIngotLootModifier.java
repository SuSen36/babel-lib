package com.susen36.babel.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.susen36.babel.api.event.OriginiumIngotDropEvent;
import com.susen36.babel.difficulty.NDifficulty;
import com.susen36.babel.init.BabelItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class OriginiumIngotLootModifier extends LootModifier {
    private static final int N6_LEVEL = 6;
    private static final float CHANCE_BELOW_N6 = 0.05F;
    private static final float CHANCE_N6_AND_ABOVE = 0.08F;

    public static final Supplier<Codec<OriginiumIngotLootModifier>> CODEC = Suppliers.memoize(
            () -> RecordCodecBuilder.create(instance -> codecStart(instance)
                    .apply(instance, OriginiumIngotLootModifier::new)));

    public OriginiumIngotLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // 仅作用于实体战利品表（entities/ 路径），避免箱子等其他来源重复掉落
        if (context.getQueriedLootTableId().getPath().startsWith("entities/")) {
            float chance = NDifficulty.difficultyLevel(context.getLevel()).value() >= N6_LEVEL
                    ? CHANCE_N6_AND_ABOVE : CHANCE_BELOW_N6;
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
        return generatedLoot;
    }

    @Override
    public @NotNull Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
