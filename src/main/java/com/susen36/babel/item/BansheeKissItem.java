package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.difficulty.NDifficulty;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BansheeKissItem extends CollectibleItem.CustomCollectibleItem {
	public BansheeKissItem() {
		super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
						.particle(ParticleTypes.HEART, 72)
						.showOverlay(true)
						.build());
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player) {

		int[] expLevel = {8,9,10,12};
		int expLevelND = NDifficulty.setValue(level,8,expLevel);
		player.giveExperienceLevels(expLevelND);

		if (!level.isClientSide()){
			float[] effectMultiplier = {1.000f,1.125f,1.250f,1.500f,1.500f};
			int effectDuration = NDifficulty.multiplier(level, 100, effectMultiplier);
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, effectDuration, 0));
		}
	}
}