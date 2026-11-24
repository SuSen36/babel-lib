package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
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
	protected int lifePointsFix;
	public RoyalFateItem() {
		super(new Item.Properties().stacksTo(1).fireResistant(), 25, false, CollectibleTiers.RARE, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.WARDEN_DEATH, 2F, 1F)
						.particle(ParticleTypes.END_ROD, 72)
						.showOverlay(true)
						.build());
		addConditionalAttributeModifier(BabelAttributes.MAX_LIFE_POINT.get(), -1, AttributeModifier.Operation.ADDITION, (player, self) -> {return lifePointsFix;});
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player) {
		int maxLives = LifePointUtils.getMaxLifePoint(player);
		Collectibles data = Collectibles.getData(player);
		if (data != null) {
			int is = 0 ;
			for (Item relic : data.getUsed()) {
				if(relic.asItem() == this) {
					is++;
				}
			}
			if (is == 0) {
				lifePointsFix = 0 ;
			}
		}
		lifePointsFix = lifePointsFix+maxLives-1;
		LifePointUtils.addShieldPoint(player, maxLives-1);
		LifePointUtils.addShieldPoint(player,  3);
		LifePointUtils.setLifePoint(player, 1);
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		return BlockUtils.setBlockByUse(level,player,hand,BabelBlocks.KING_PEDESTAL);
	}
}