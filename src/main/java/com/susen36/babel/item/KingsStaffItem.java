package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class KingsStaffItem extends CollectibleItem.CustomCollectibleItem {
	public KingsStaffItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.TOTEM_USE, 2F, 1F)
						.particle(ParticleTypes.ENCHANTED_HIT, 72)
						.showOverlay(true)
						.build());
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
		return tickCount % 320 == 0;
	}

	@Override
	public void applyEffectTick(Player player, int layer) {
		if (LifePointUtils.getLifePoint(player) <= 1 && !player.level().isClientSide()) {
			player.getFoodData().eat(1, 0.0F);
		}
	}
}