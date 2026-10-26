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
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class KingsBucklerItem extends CollectibleItem.CustomCollectibleItem {
	public KingsBucklerItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.TOTEM_USE, 2F, 1F)
						.particle(ParticleTypes.ENCHANTED_HIT, 72)
						.showOverlay(true)
						.build());
	}

	@Override
	public void onMobHurt(LivingDamageEvent event, Player player, int layer) {
		if (LifePointUtils.getLifePoint(player) <= 1
				&& event.getSource().getEntity() != null
				&& event.getSource().getDirectEntity() == event.getSource().getEntity()) {
			event.setAmount(event.getAmount() * 0.8F);
		}
	}
}