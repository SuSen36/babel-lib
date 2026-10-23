package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;


public class OddFluteItem extends CollectibleItem.CustomCollectibleItem {
	public OddFluteItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 40, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(BabelSounds.FLUTESONG.get(), 2F, 1F)
						.particle(ParticleTypes.NOTE, 72)
						.showOverlay(true)
						.build());
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemstack) {
		return UseAnim.BOW;
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
		if (level instanceof ServerLevel serverLevel) {
			double x = player.getX();
			double y = player.getY();
			double z = player.getZ();
			RandomSource random = level.random;
			for (int index0 = 0; index0 < 7; index0++) {
				serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, x + Mth.nextDouble(random, -1, 1), y + Mth.nextDouble(random, 0.6, 0.75), z + Mth.nextDouble(random, -1, 1), 4));
			}
		}
		if (!level.isClientSide())
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 900, 0, false, false));
	}
}