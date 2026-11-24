package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.difficulty.NDifficulty;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.items.ItemHandlerHelper;


public class CannedCherryItem extends CollectibleItem.CustomCollectibleItem {
	public CannedCherryItem() {
		super(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder().nutrition(6).saturationMod(0.1f).alwaysEat().build()), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
						.particle(ParticleTypes.WAX_OFF, 72)
						.showOverlay(true)
						.build());
		addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.01, AttributeModifier.Operation.MULTIPLY_BASE,
				(player, self) -> switch (NDifficulty.difficultyLevel(player.level()).tier()) {
					case EASY -> 2;
					case NORMAL -> 4;
					case HARD -> 6;
					case EXPERT, PERILOUS -> 8;
				});
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player) {
		ItemStack setstack = new ItemStack(Items.GLASS_BOTTLE).copy();
		setstack.setCount(1);
		ItemHandlerHelper.giveItemToPlayer(player, setstack);
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		return BlockUtils.setBlockByUse(level,player,hand,BabelBlocks.BERRY_CAN);
	}
}