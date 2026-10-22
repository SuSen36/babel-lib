package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


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
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
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
			BlockPos pos = blockHit.getBlockPos();
			BlockState blockstate = level.getBlockState(pos);
			if (blockstate.getBlock() == Blocks.DEEPSLATE_BRICK_SLAB) {
				Direction dir = player.getDirection().getOpposite();
				BlockState bs = BabelBlocks.BLOCK_FATE.get().defaultBlockState();
				Property<?> property = bs.getBlock().getStateDefinition().getProperty("facing");
				if (property instanceof DirectionProperty dp && dp.getPossibleValues().contains(dir)) {
					bs = bs.setValue(dp, dir);
				} else {
					property = bs.getBlock().getStateDefinition().getProperty("axis");
					if (property instanceof EnumProperty ap && ap.getPossibleValues().contains(dir.getAxis()))
						bs = bs.setValue(ap, dir.getAxis());
				}
				level.setBlock(pos, bs, 3);
				itemstack.shrink(1);
				return InteractionResultHolder.consume(itemstack);
			}
		}
		return InteractionResultHolder.pass(itemstack);
	}
}
