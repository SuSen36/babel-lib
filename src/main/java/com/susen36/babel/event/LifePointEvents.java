package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.HealthConsumeEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.init.BabelGameRules;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = BabelMod.MODID)
public class LifePointEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide() || event.isCanceled()) {
            return;
        }
        if (!player.level().getGameRules().getBoolean(BabelGameRules.TARGET_LIFE_FUNCTION)) {
            return;
        }
        if (HealthConsumeEvent.firePre(player, event.getSource())) {
            return;
        }

        HealthCapability health = BabelCapability.getHealth(player);
        boolean blocked = false;
        boolean isShield = false;

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
            health.sync();
        }
        HealthConsumeEvent.firePost(player, event.getSource(), blocked, blocked && isShield, blocked && !isShield);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 10 != 0) {
            return;
        }

        HealthCapability health = BabelCapability.getHealth(player);
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