package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class RescissionItem extends CollectibleItem.CustomCollectibleItem {
	public RescissionItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON), 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0),
				CollectibleActivation.builder()
						.sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
						.particle(ParticleTypes.ASH, 72)
						.showOverlay(true)
						.build());
	}

	@Override
	public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
		player.giveExperienceLevels(4);
	}

	@Override
	protected InteractionResultHolder<ItemStack> onSneakUse(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		double entityReach = player.getEntityReach();
		double blockReach = player.getBlockReach();
		double pickRange = Math.max(entityReach, blockReach);
		Vec3 eye = player.getEyePosition(1.0F);
		Vec3 sight = eye.add(player.getViewVector(1.0F).scale(pickRange));
		HitResult blockHit = player.pick(pickRange, 1.0F, false);
		double limitSqr = pickRange * pickRange;
		if (blockHit.getType() != HitResult.Type.MISS && blockHit.getLocation().distanceToSqr(eye) <= blockReach * blockReach) {
			limitSqr = blockHit.getLocation().distanceToSqr(eye);
		}
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eye, sight,
				player.getBoundingBox().expandTowards(sight.subtract(eye)).inflate(1.0D),
				candidate -> candidate != player && !candidate.isSpectator() && candidate.isPickable(), limitSqr);
		InteractionResultHolder<ItemStack> result = InteractionResultHolder.pass(itemstack);
		if (entityHit != null) {
			Entity target = entityHit.getEntity();
			Entity owner = target instanceof TamableAnimal tamable && tamable.isTame() ? tamable.getOwner() : target;
			if (owner == player) {
				if (!level.isClientSide() && target instanceof TamableAnimal tamable) {
					tamable.setTame(false);
				}
				if (!level.isClientSide()) {
					player.displayClientMessage(Component.literal(target.getDisplayName().getString()
							+ Component.translatable("item.babel.rescission.message_2").getString()), false);
				}
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.ASH, target.getX(), target.getY(), target.getZ(), 72, 1, 1, 1, 0.5);
					if (target instanceof Wolf) {
						MinecraftServer server = serverLevel.getServer();
						server.tell(new TickTask(server.getTickCount() + Mth.nextInt(RandomSource.create(), 40, 80), () -> {
							if (!player.level().isClientSide()) {
								player.displayClientMessage(Component.literal("§o" + Component.translatable("item.babel.rescission.message_3").getString()), false);
							}
						}));
					}
				}
				if (!player.getAbilities().instabuild) {
					itemstack.shrink(1);
				}
				result = InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
			} else if (!level.isClientSide()) {
				player.displayClientMessage(Component.literal("§c" + Component.translatable("item.babel.rescission.message_1").getString()), false);
			}
		}
		return result;
	}
}