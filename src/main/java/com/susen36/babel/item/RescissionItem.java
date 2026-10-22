package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;


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
		Vec3 center = new Vec3(player.getX(), player.getY(), player.getZ());
		List<Entity> entfound = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1.0D), e -> true)
				.stream().sorted(Comparator.comparingDouble(entcnd -> entcnd.distanceToSqr(center))).toList();
		for (Entity entityiterator : entfound) {
			if (entityiterator == player) {
				continue;
			}
			Entity owner = entityiterator;
			if (entityiterator instanceof TamableAnimal tamEnt && tamEnt.isTame()) {
				owner = tamEnt.getOwner();
			}
			if (owner != player) {
				continue;
			}
			if (entityiterator instanceof LivingEntity livEnt11 && livEnt11.hasEffect(BabelMobEffects.UNTAME_CONFIRM.get())) {
				if (entityiterator instanceof TamableAnimal ent) {
					ent.setTame(false);
				}
				if (!player.level().isClientSide())
					player.displayClientMessage(Component.literal(entityiterator.getDisplayName().getString()
							+ Component.translatable("item.babel.util_rescission.message_2").getString()), false);
				livEnt11.removeEffect(BabelMobEffects.UNTAME_CONFIRM.get());
				if (level instanceof ServerLevel serverLevel)
					serverLevel.sendParticles(ParticleTypes.ASH, entityiterator.getX(), entityiterator.getY(), entityiterator.getZ(), 72, 1, 1, 1, 0.5);
				stack.shrink(1);
				if (entityiterator instanceof Wolf && level instanceof ServerLevel serverLevel) {
					MinecraftServer server = serverLevel.getServer();
					server.tell(new TickTask(server.getTickCount() + Mth.nextInt(RandomSource.create(), 40, 80), () -> {
						if (!player.level().isClientSide())
							player.displayClientMessage(Component.literal("§o" + Component.translatable("item.babel.util_rescission.message_3").getString()), false);
					}));
				}
			} else {
				if (!player.level().isClientSide())
					player.displayClientMessage(Component.literal("§c" + Component.translatable("item.babel.util_rescission.message_0").getString()
							+ entityiterator.getDisplayName().getString()
							+ Component.translatable("item.babel.util_rescission.message_1").getString()), false);
				if (entityiterator instanceof LivingEntity living && !player.level().isClientSide())
					living.addEffect(new MobEffectInstance(BabelMobEffects.UNTAME_CONFIRM.get(), 300, 0, false, false));
			}
			break;
		}
	}
}