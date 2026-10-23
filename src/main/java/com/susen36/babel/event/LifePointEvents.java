package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.HealthConsumeEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.init.BabelGameRules;
import com.susen36.babel.init.BabelParticles;
import com.susen36.babel.init.BabelSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class LifePointEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide() && !event.isCanceled()
                && player.level().getGameRules().getBoolean(BabelGameRules.TARGET_LIFE_FUNCTION)
                && !HealthConsumeEvent.firePre(player, event.getSource())) {
            HealthCapability health = BabelCapability.getHealth(player);
            boolean blocked = false;
            boolean isShield = false;

            if (health != null) {
                if (health.getShield() > 0) {
                    blocked = true;
                    isShield = true;
                    health.setShield(health.getShield() - 1);
                } else if (health.getLives() > 1) {
                    blocked = true;
                    health.setLives(health.getLives() - 1);
                }

                if (blocked) {
                    event.setCanceled(true);
                    player.setHealth(player.getMaxHealth());
                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();
                    player.level().playSound(null, x, y, z, BabelSounds.TARGET_DAMAGED.get(), SoundSource.PLAYERS, 0.33F, 1.0F);
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(isShield ? BabelParticles.SHIELDLOSS.get() : BabelParticles.LIFELOSS.get(), x, y + 0.95, z, 72, 0.75, 0.55, 0.75, 0.2);
                    }
                    health.sync();
                }
            }
            HealthConsumeEvent.firePost(player, event.getSource(), blocked, blocked && isShield, blocked && !isShield);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            if (!player.level().isClientSide() && player.tickCount % 10 == 0) {
                HealthCapability health = BabelCapability.getHealth(player);
                if (health != null) {
                    double lives = health.getLives();
                    double maxLives = health.getMaxLives();

                    if (lives > maxLives) {
                        health.setLives(maxLives);
                        health.sync();
                    } else if (lives < 1) {
                        health.setLives(1);
                        health.sync();
                    }
                }
            }
        }
    }
}