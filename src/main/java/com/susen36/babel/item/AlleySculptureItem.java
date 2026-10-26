package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;


public class AlleySculptureItem extends CollectibleItem.CustomCollectibleItem {
	public AlleySculptureItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 3.5F, 1F)
						.particle(ParticleTypes.RAIN, 72)
						.showOverlay(true)
						.build());
		addAttributeModifier(BabelAttributes.MAX_LIFE_POINT.get(), 3, AttributeModifier.Operation.ADDITION);
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
		LifePointUtils.setLifePoint(player, LifePointUtils.getLifePoint(player) + 3);
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		ItemStack setstack = new ItemStack(BabelBlocks.ALLAY_BLOCK.get()).copy();
		setstack.setCount(1);
		ItemHandlerHelper.giveItemToPlayer(player, setstack);
		if (!player.getAbilities().instabuild)
			itemstack.shrink(1);
		return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
	}
}
