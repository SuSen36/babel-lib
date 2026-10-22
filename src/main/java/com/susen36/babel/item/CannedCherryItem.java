package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.items.ItemHandlerHelper;


public class CannedCherryItem extends CollectibleItem.CustomCollectibleItem {
	public CannedCherryItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.1f).alwaysEat().build()), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
						.particle(ParticleTypes.HAPPY_VILLAGER, 72)
						.showOverlay(true)
						.build());
		addAttributeModifier(Attributes.ATTACK_DAMAGE, 0.05, AttributeModifier.Operation.MULTIPLY_BASE);
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
		ItemStack setstack = new ItemStack(Items.GLASS_BOTTLE).copy();
		setstack.setCount(1);
		ItemHandlerHelper.giveItemToPlayer(player, setstack);
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		HitResult hit = player.pick(5.0D, 1.0F, false);
		if (hit instanceof BlockHitResult blockHit) {
			Direction direction = blockHit.getDirection();
			BlockPos clicked = blockHit.getBlockPos();
			BlockPos pos = clicked.relative(direction);
			if (BabelBlocks.BERRY_CAN.get().defaultBlockState().canSurvive(level, pos)) {
				level.setBlock(pos, BabelBlocks.BERRY_CAN.get().defaultBlockState(), 3);
				level.playSound(null, clicked, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1, 1);
				Direction dir = player.getDirection().getOpposite();
				BlockState bs = level.getBlockState(pos);
				Property<?> property = bs.getBlock().getStateDefinition().getProperty("facing");
				if (property instanceof DirectionProperty dp && dp.getPossibleValues().contains(dir)) {
					level.setBlock(pos, bs.setValue(dp, dir), 3);
				} else {
					property = bs.getBlock().getStateDefinition().getProperty("axis");
					if (property instanceof EnumProperty ap && ap.getPossibleValues().contains(dir.getAxis()))
						level.setBlock(pos, bs.setValue(ap, dir.getAxis()), 3);
				}
				itemstack.shrink(1);
				return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
			}
		}
		return InteractionResultHolder.pass(itemstack);
	}
}
