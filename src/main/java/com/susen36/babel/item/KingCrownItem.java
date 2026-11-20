package com.susen36.babel.item;

import com.susen36.babel.block.KingPedestalBlockEntity;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.init.BabelItemTags;
import com.susen36.babel.util.BlockUtils;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

//King's Crown
public class KingCrownItem extends CollectibleItem.CustomCollectibleItem {
	public KingCrownItem() {
		super(new Item.Properties().stacksTo(1).fireResistant(), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.TOTEM_USE, 2F, 1F)
						.particle(ParticleTypes.ENCHANTED_HIT, 72)
						.showOverlay(true)
						.build());
		addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.5, AttributeModifier.Operation.MULTIPLY_BASE,
				(player, self) -> LifePointUtils.getLifePoint(player) <= 1
						? (countUsedRelics(player, BabelItemTags.KING_RELICS) < 3 ? 1 : 3) : 0);
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		return BlockUtils.setBlockByUse(level,player,hand,BabelBlocks.KING_PEDESTAL);
	}
}