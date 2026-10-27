package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ProofOfLongevityItem extends CollectibleItem.CustomCollectibleItem {
	public ProofOfLongevityItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.AMBIENT_WARPED_FOREST_MOOD.value(), 3.5F, 1F)
						.particle(ParticleTypes.HAPPY_VILLAGER, 72)
						.showOverlay(true)
						.build());
		addAttributeModifier(BabelAttributes.MAX_LIFE_POINT.get(), 6, AttributeModifier.Operation.ADDITION);
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
		LifePointUtils.setLifePoint(player, LifePointUtils.getLifePoint(player) + 6);
	}
}