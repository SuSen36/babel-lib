package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.init.BabelItemTags;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.phys.HitResult;


public class RelicCrownItem extends CollectibleItem.CustomCollectibleItem {
	public RelicCrownItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
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
	public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		HitResult hit = player.pick(5.0D, 1.0F, false);
		if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = BlockPos.containing(hit.getLocation());
			BlockState blockstate = level.getBlockState(pos);
			if (blockstate.is(Blocks.DEEPSLATE_BRICK_SLAB)) {
				Direction dir = player.getDirection().getOpposite();
				BlockState bs = BabelBlocks.BLOCK_CROWN.get().defaultBlockState();
				Property<?> property = bs.getBlock().getStateDefinition().getProperty("facing");
				if (property instanceof DirectionProperty dp && dp.getPossibleValues().contains(dir)) {
					bs = bs.setValue(dp, dir);
				} else {
					property = bs.getBlock().getStateDefinition().getProperty("axis");
					if (property instanceof EnumProperty ap && ap.getPossibleValues().contains(dir.getAxis()))
						bs = bs.setValue(ap, dir.getAxis());
				}
				level.setBlock(pos, bs, 3);
				if (!player.getAbilities().instabuild)
					itemstack.shrink(1);
				return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
			}
		}
		return InteractionResultHolder.pass(itemstack);
	}
}
