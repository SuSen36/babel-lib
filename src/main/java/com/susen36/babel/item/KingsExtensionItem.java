package com.susen36.babel.item;

import com.susen36.babel.block.KingPedestalBlockEntity;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelBlocks;
import com.susen36.babel.util.LifePointUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;


public class KingsExtensionItem extends CollectibleItem.CustomCollectibleItem {
	public KingsExtensionItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.TOTEM_USE, 2F, 1F)
						.particle(ParticleTypes.ENCHANTED_HIT, 72)
						.showOverlay(true)
						.build());
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
		return tickCount % 10 == 0;
	}

	@Override
	public void applyEffectTick(Player player, int layer) {
		if (LifePointUtils.getLifePoint(player) <= 1 && !player.level().isClientSide()) {
			int amplifier = Mth.clamp(Mth.ceil(player.getMaxHealth() / 20), 0, 24);
			int currentAmplifier = player.hasEffect(MobEffects.REGENERATION) ? player.getEffect(MobEffects.REGENERATION).getAmplifier() : 0;
			if (currentAmplifier < amplifier) {
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, amplifier, false, false));
			}
		}
	}

	@Override
	public void onExperienceDrop(LivingExperienceDropEvent event, Player player, int layer) {
		if (LifePointUtils.getLifePoint(player) <= 1) {
			event.setDroppedExperience((int) (event.getDroppedExperience() * 1.5));
		}
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		HitResult hit = player.pick(player.getBlockReach(), 1.0F, false);
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
					if (!player.getAbilities().instabuild) {
						itemstack.shrink(1);
					}
					return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
				}
			}
		}
		return InteractionResultHolder.pass(itemstack);
	}
}