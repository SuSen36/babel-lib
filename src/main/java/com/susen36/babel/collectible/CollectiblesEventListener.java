package com.susen36.babel.collectible;

import com.susen36.babel.BabelMod;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class CollectiblesEventListener {

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        Collectibles.expireUsedCollectibles(event.getEntity());
        BabelNetwork.syncCollectibles(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.level() instanceof ServerLevel serverLevel)
            BabelNetwork.syncServerCurrentTick(serverPlayer, serverLevel.getServer().overworld().getGameTime());
    }

    @SubscribeEvent
    public static void onLivingDeathEvent(@NotNull PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel) {
            BabelNetwork.syncCollectibles(player);
            BabelNetwork.syncServerCurrentTick(player, serverLevel.getServer().overworld().getGameTime());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimensionEvent(@NotNull PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel) {
            BabelNetwork.syncCollectibles(player);
            BabelNetwork.syncServerCurrentTick(player, serverLevel.getServer().overworld().getGameTime());
        }
    }

    @SubscribeEvent
    public static void onServerTick(@NotNull TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            event.getServer().getPlayerList().getPlayers().forEach(player -> BabelNetwork.syncServerCurrentTick(player, event.getServer().overworld().getGameTime()));
        }
    }
}