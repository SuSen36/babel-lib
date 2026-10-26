package com.susen36.babel.item;

import com.susen36.babel.block.KingPedestalBlockEntity;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;


public class RoyalFateItem extends CollectibleItem.CustomCollectibleItem {
	public RoyalFateItem() {
		super(new Item.Properties().stacksTo(2).fireResistant().rarity(Rarity.EPIC), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.WARDEN_DEATH, 2F, 1F)
						.particle(ParticleTypes.END_ROD, 72)
						.showOverlay(true)
						.build());
	}



	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
		if (LifePointUtils.getMaxLifePoint(player) > 1) {
			double lives_left = LifePointUtils.getMaxLifePoint(player);
			LifePointUtils.setMaxLifePoint(player, 1);
			LifePointUtils.setLifePoint(player, 1);
			LifePointUtils.setShieldPoint(player, LifePointUtils.getShieldPoint(player) + (int) lives_left);
			LifePointUtils.setShieldPoint(player, LifePointUtils.getShieldPoint(player) + 3);
		}
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		HitResult hit = player.pick(5.0D, 1.0F, false);
		if (hit instanceof BlockHitResult blockHit) {
			UseOnContext context = new UseOnContext(player, hand, blockHit);
			BlockPlaceContext blockContext = new BlockPlaceContext(context);
			if (blockContext.canPlace()) {
				BlockState blockstate = BabelBlocks.KING_PEDESTAL.get().getStateForPlacement(blockContext);
				if (blockstate != null) {
					level.setBlock(blockContext.getClickedPos(), blockstate, 3);
					if (level.getBlockEntity(blockContext.getClickedPos()) instanceof KingPedestalBlockEntity pedestal) {
						pedestal.setItem(itemstack.copy());
					}
					itemstack.shrink(1);
					return InteractionResultHolder.consume(itemstack);
				}
			}
		}
		return InteractionResultHolder.pass(itemstack);
	}
}
