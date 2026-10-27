package com.susen36.babel.capability;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class BabelCapabilityEvents {

    private BabelCapabilityEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide && entity instanceof LivingEntity living) {
            EPCapability ep = BabelCapability.getEP(living);
            if (ep != null) {
                ep.sync();
            }
            HealthCapability health = BabelCapability.getHealth(living);
            if (health != null) {
                health.sync();
            }
            if (living instanceof Player player) {
                BabelNetwork.syncCollectibles(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            EPCapability ep = BabelCapability.getEP(player);
            if (ep != null) {
                ep.sync();
            }
            HealthCapability health = BabelCapability.getHealth(player);
            if (health != null) {
                health.sync();
            }
            BabelNetwork.syncCollectibles(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            EPCapability ep = BabelCapability.getEP(player);
            if (ep != null) {
                ep.sync();
            }
            HealthCapability health = BabelCapability.getHealth(player);
            if (health != null) {
                health.sync();
            }
            BabelNetwork.syncCollectibles(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            EPCapability ep = BabelCapability.getEP(player);
            if (ep != null) {
                ep.sync();
            }
            HealthCapability health = BabelCapability.getHealth(player);
            if (health != null) {
                health.sync();
            }
            BabelNetwork.syncCollectibles(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player oldPlayer = event.getOriginal();
        oldPlayer.reviveCaps();
        EPCapability oldEP = BabelCapability.getEP(oldPlayer);
        EPCapability ep = BabelCapability.getEP(player);
        if (oldEP != null && ep != null) {
            ep.deserializeNBT(oldEP.serializeNBT());
        }
        HealthCapability oldHealth = BabelCapability.getHealth(oldPlayer);
        HealthCapability health = BabelCapability.getHealth(player);
        if (oldHealth != null && health != null) {
            health.deserializeNBT(oldHealth.serializeNBT());
            health.sync();
        }
    }
}