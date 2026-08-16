package com.susen36.babel.capability;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.capability.health.HealthCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = BabelMod.MODID)
public class BabelCapabilityEvents {

    private BabelCapabilityEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide() && entity instanceof LivingEntity living) {
            BabelCapability.getEP(living).sync();
            BabelCapability.getHealth(living).sync();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            BabelCapability.getEP(player).sync();
            BabelCapability.getHealth(player).sync();
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            BabelCapability.getEP(player).sync();
            BabelCapability.getHealth(player).sync();
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            BabelCapability.getEP(player).sync();
            BabelCapability.getHealth(player).sync();
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        Player oldPlayer = event.getOriginal();
        oldPlayer.revive();
        EPCapability oldEP = BabelCapability.getEP(oldPlayer);
        EPCapability ep = BabelCapability.getEP(player);
        ep.deserializeNBT(oldPlayer.registryAccess(), oldEP.serializeNBT(oldPlayer.registryAccess()));
        HealthCapability oldHealth = BabelCapability.getHealth(oldPlayer);
        HealthCapability health = BabelCapability.getHealth(player);
        health.deserializeNBT(oldPlayer.registryAccess(), oldHealth.serializeNBT(oldPlayer.registryAccess()));
    }
}