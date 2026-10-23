package com.susen36.babel.item;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelItemTags;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class HandFireworkItem extends CollectibleItem.CustomCollectibleItem {

    private static final double EXPLOSION_RADIUS = 5.0D;

    public HandFireworkItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        if (!event.isCanceled() && event.getSource().getDirectEntity() != player) {
            Level level = player.level();
            ItemStack mainHandItem = player.getMainHandItem();
            String registryName = BuiltInRegistries.ITEM.getKey(mainHandItem.getItem()).toString();
            if (isFireworkWeapon(mainHandItem, registryName) && player.getRandom().nextFloat() < 0.33F) {
                detonateFirework(level, target, player, event.getAmount());
            }
        }
    }

    private static boolean isFireworkWeapon(ItemStack stack, String registryName) {
        return stack.is(Tags.Items.TOOLS_BOWS) || stack.getItem() instanceof BowItem
                || stack.is(BabelItemTags.FIREWORK_WEAPONS)
                || matchesConfigured(BabelConfig.handFireworkRecognizable, registryName);
    }

    private static void detonateFirework(Level level, LivingEntity target, Player player, double amount) {
        Vec3 center = target.position();
        level.playSound(null, center.x, center.y, center.z, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 3.6F, 1);
        BabelNetwork.sendFireworkParticles(target, center.x, center.y + 0.5, center.z, randomFireworkNbt(player.getRandom()));
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(EXPLOSION_RADIUS),
                candidate -> candidate != player && (candidate.getType() == target.getType() || candidate instanceof Monster));
        for (LivingEntity candidate : nearbyEntities) {
            boolean visible = false;
            for (int i = 0; i < 2; ++i) {
                Vec3 sample = new Vec3(candidate.getX(), candidate.getY(0.5D * i), candidate.getZ());
                HitResult hitResult = level.clip(new ClipContext(center, sample, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                if (hitResult.getType() == HitResult.Type.MISS) {
                    visible = true;
                    break;
                }
            }
            if (visible) {
                double dist = Math.sqrt(candidate.distanceToSqr(center.x, center.y, center.z));
                float damage = (float) (amount * 3 * Math.sqrt((EXPLOSION_RADIUS - dist) / EXPLOSION_RADIUS));
                candidate.hurt(level.damageSources().playerAttack(player), damage);
            }
        }
    }

    private static CompoundTag randomFireworkNbt(RandomSource random) {
        CompoundTag explosion = new CompoundTag();
        explosion.putByte("Type", (byte) random.nextInt(5));
        if (random.nextBoolean()) {
            explosion.putBoolean("Flicker", true);
        }
        if (random.nextBoolean()) {
            explosion.putBoolean("Trail", true);
        }
        int colorCount = random.nextInt(8) + 1;
        int[] colors = new int[colorCount];
        for (int i = 0; i < colorCount; i++) {
            colors[i] = DyeColor.values()[random.nextInt(DyeColor.values().length)].getFireworkColor();
        }
        explosion.putIntArray("Colors", colors);
        ListTag explosions = new ListTag();
        explosions.add(explosion);
        CompoundTag fireworkNbt = new CompoundTag();
        fireworkNbt.put("Explosions", explosions);
        return fireworkNbt;
    }
}
