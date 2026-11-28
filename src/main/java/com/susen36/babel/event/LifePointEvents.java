package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.init.BabelGameRules;
import com.susen36.babel.init.BabelParticles;
import com.susen36.babel.init.BabelSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class LifePointEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide() && !event.isCanceled()
                && !event.getSource().is(DamageTypes.GENERIC_KILL)
                && player.level().getGameRules().getBoolean(BabelGameRules.TARGET_LIFE_FUNCTION)) {
            HealthCapability health = BabelCapability.getHealth(player);
            LifePointConsumeEvent.Type proposed = LifePointConsumeEvent.Type.FAIL;
            if (health != null && health.getShield() > 0) {
                proposed = LifePointConsumeEvent.Type.SHIELD;
            } else if (health != null && health.getLives() > 1) {
                proposed = LifePointConsumeEvent.Type.LIVES;
            }
            LifePointConsumeEvent.Pre pre = BabelEventFactory.beforeLifePointConsume(player, event.getSource(), proposed);
            if (health != null && !pre.isCanceled()) {
                resolveDeath(event, player, health, pre);
            }
        }
    }

    private static void resolveDeath(LivingDeathEvent event, Player player, HealthCapability health, LifePointConsumeEvent.Pre pre) {
        LifePointConsumeEvent.Type result = LifePointConsumeEvent.Type.FAIL;
        double spent = 0;
        if (pre.getType() == LifePointConsumeEvent.Type.SHIELD && health.getShield() > 0) {
            double before = health.getShield();
            health.setShield(before - pre.getAmount());
            spent = before - health.getShield();
        } else if (pre.getType() == LifePointConsumeEvent.Type.LIVES && health.getLives() > 1) {
            double before = health.getLives();
            health.setLives(before - pre.getAmount());
            spent = before - health.getLives();
        }

        LifePointConsumeEvent.Revival revival = pre.getRevival();
        if (spent > 0) {
            result = pre.getType();
            event.setCanceled(true);
            player.setHealth(player.getMaxHealth());
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            player.level().playSound(null, x, y, z, BabelSounds.TARGET_DAMAGED.get(), SoundSource.PLAYERS, 0.33F, 1.0F);
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(result == LifePointConsumeEvent.Type.SHIELD ? BabelParticles.SHIELDLOSS.get() : BabelParticles.LIFELOSS.get(), x, y + 0.95, z, 72, 0.75, 0.55, 0.75, 0.2);
            }
            health.sync();
        } else if (revival != null) {
            result = LifePointConsumeEvent.Type.REVIVED;
            event.setCanceled(true);
            player.setHealth(revival.health());
            revival.onRevived().run();
        }
        BabelEventFactory.afterLifePointConsume(player, event.getSource(), result, (float) spent);
    }

    // 先让藏品对账更新临时上限，再夹取当前目标生命。
    @SubscribeEvent(priority = EventPriority.LOW)
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