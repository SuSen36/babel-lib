package com.susen36.babel.event;

import com.susen36.babel.collectible.Collectibles;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class ServerTickEventListener {
    @SubscribeEvent
    public static void onServerTickEvent(ServerTickEvent event) {
        var server = event.getServer();
        var players = server.getPlayerList().getPlayers();
        long currentTick = server.getTickCount();
        for (var player : players) {
            player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get()).clearAllExpired(currentTick);
        }
    }
}
