package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HandSwipeItem extends CollectibleItem.CustomCollectibleItem {

    public HandSwipeItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return tickCount % 20 == 0;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        if (player.getOffhandItem().is(Items.BRUSH) && player.hasEffect(MobEffects.REGENERATION)) {
            Level level = player.level();
            Vec3 center = new Vec3(player.getX(), player.getY(), player.getZ());
            List<Monster> monsters = level.getEntitiesOfClass(Monster.class,
                    new AABB(center, center).inflate(8), entity -> true);
            for (Monster monster : monsters) {
                if (player.distanceToSqr(monster) < 64) {
                    MobEffectInstance regen = player.getEffect(MobEffects.REGENERATION);
                    monster.hurt(level.damageSources().indirectMagic(player, player),
                            (float) (5.0 * Math.pow(1 + (regen != null ? regen.getAmplifier() : 0), 1.25)));
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.WAX_OFF, monster.getX(), monster.getY(), monster.getZ(), 12, 0.8, 1, 0.8, 0.1);
                    }
                }
            }
        }
    }
}
