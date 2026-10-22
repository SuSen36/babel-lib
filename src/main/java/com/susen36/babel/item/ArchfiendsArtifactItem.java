package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelItemTags;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ArchfiendsArtifactItem extends CollectibleItem.CustomCollectibleItem {
	public ArchfiendsArtifactItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.TOTEM_USE, 2F, 1F)
						.particle(ParticleTypes.DRIPPING_LAVA, 72)
						.showOverlay(true)
						.build());
		addConditionalAttributeModifier(Attributes.MAX_HEALTH, 0.5, AttributeModifier.Operation.MULTIPLY_BASE,
				(player, self) -> LifePointUtils.getLifePoint(player) >= LifePointUtils.getMaxLifePoint(player)
						? (countUsedRelics(player, BabelItemTags.ARCHFIEND_RELICS) < 3 ? 1 : 3) : 0);
	}
}
