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
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class CoffeeCandyItem extends CollectibleItem.CustomCollectibleItem {
	public CoffeeCandyItem() {
		super(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder().nutrition(2).saturationMod(1f).build()), 30, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
						.particle(ParticleTypes.WAX_OFF, 72)
						.showOverlay(true)
						.build());
		addConditionalAttributeModifier(Attributes.MOVEMENT_SPEED, 0.01, AttributeModifier.Operation.MULTIPLY_BASE,
				(player, self) -> switch (NDifficulty.difficultyLevel(player.level()).tier()) {
					case EASY -> 2;
					case NORMAL -> 4;
					case HARD -> 6;
					case EXPERT, PERILOUS -> 8;
				});
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player) {
		if (!level.isClientSide()){
			float[] effectMultiplier = {2f,4f,6f,8f};
			int effectDurationND = NDifficulty.multiplier(level,160, effectMultiplier);
			player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, effectDurationND, 1));
		}
	}
}