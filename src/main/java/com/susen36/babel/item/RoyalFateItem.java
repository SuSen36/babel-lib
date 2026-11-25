package com.susen36.babel.item;

import com.susen36.babel.collectible.*;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.util.BlockUtils;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

//Visage of Crowned Fates
public class RoyalFateItem extends CollectibleItem.CustomCollectibleItem {
	public RoyalFateItem() {
		super(new Item.Properties().stacksTo(1).fireResistant(), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.WARDEN_DEATH, 2F, 1F)
						.particle(ParticleTypes.END_ROD, 72)
						.showOverlay(true)
						.build());
		addAttributeModifier(BabelAttributes.MAX_LIFE_POINT.get(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override
	protected CollectibleEffect prepareActivation(Player player) {
		// 同一 tick 内移除再获取时，先清理旧修饰；重登尚未对账时，按已持有状态跳过兑换。
		CollectibleAttributes.reconcile(player, this);
		Collectibles data = Collectibles.getData(player);
		int converted = data != null && !data.isUsed(this) ? LifePointUtils.getMaxLifePoint(player) - 1 : 0;
		return (stack, level, owner) -> {
			LifePointUtils.addShieldPoint(owner, converted);
			onEffectStarted(stack, level, owner);
		};
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player) {
		LifePointUtils.addShieldPoint(player, 3);
		LifePointUtils.setLifePoint(player, 1);
		CollectibleAttributes.reconcile(player, this);
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		return BlockUtils.setBlockByUse(level,player,hand,BabelBlocks.KING_PEDESTAL);
	}
}