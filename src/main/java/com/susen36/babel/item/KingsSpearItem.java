package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.util.BlockUtils;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

//King's New Lance
public class KingsSpearItem extends CollectibleItem.CustomCollectibleItem {
	public KingsSpearItem() {
		super(new Item.Properties().stacksTo(1).fireResistant(), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.TOTEM_USE, 2F, 1F)
						.particle(ParticleTypes.ENCHANTED_HIT, 72)
						.showOverlay(true)
						.build());
		addConditionalAttributeModifier(Attributes.ATTACK_SPEED, 0.5, AttributeModifier.Operation.ADDITION,
				(player, self) -> LifePointUtils.getLifePoint(player) <= 1 ? 2 : 0);
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		return BlockUtils.setBlockByUse(level,player,hand,BabelBlocks.KING_PEDESTAL);
	}
}