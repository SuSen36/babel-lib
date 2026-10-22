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
import net.minecraft.world.entity.Entity;
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
import net.minecraftforge.event.entity.living.LivingDeathEvent;


public class KingsArmourItem extends CollectibleItem.CustomCollectibleItem {
	public KingsArmourItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.TOTEM_USE, 2F, 1F)
						.particle(ParticleTypes.ENCHANTED_HIT, 72)
						.showOverlay(true)
						.build());
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
		double storedLives = LifePointUtils.getLifePoint(player);
		if (storedLives > 1) {
			LifePointUtils.setLifePoint(player, 1);
		}
		double shieldAfterLifeTransfer = LifePointUtils.getShieldPoint(player) + storedLives;
		LifePointUtils.setShieldPoint(player, (int) shieldAfterLifeTransfer);
		LifePointUtils.setShieldPoint(player, (int) (shieldAfterLifeTransfer + 3));
	}

	@Override
	public void onPlayerKill(LivingDeathEvent event, Player player, Entity victim, int layer) {
		if (Math.random() < 0.08) {
			if (LifePointUtils.getLifePoint(player) > 1) {
				LifePointUtils.setLifePoint(player, LifePointUtils.getLifePoint(player) - 1);
			}
			LifePointUtils.setShieldPoint(player, LifePointUtils.getShieldPoint(player) + 1);
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
				BlockState bs = BabelBlocks.KINGS_ARMOR.get().defaultBlockState();
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
				return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
			}
		}
		return InteractionResultHolder.pass(itemstack);
	}
}
