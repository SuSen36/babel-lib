package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.difficulty.NDifficulty;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ChurchAlmsVoucherItem extends CollectibleItem.CustomCollectibleItem {
    public ChurchAlmsVoucherItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.MAX_HEALTH, 0.01, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> switch (NDifficulty.difficultyLevel(player.level()).tier()) {
                    case EASY -> 6;
                    case NORMAL -> 7;
                    case HARD -> 8;
                    case EXPERT, PERILOUS -> 10;
                });
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide()) {
            float[] effectMultiplier = {6f,7f,8f,10f};
            int effectDuration = NDifficulty.multiplier(level, 50, effectMultiplier);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, effectDuration, 1));
        }
    }
}