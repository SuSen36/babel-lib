package com.susen36.babel.event;

import com.susen36.babel.api.event.CollectibleEvent;
import com.susen36.babel.collectible.Collectibles;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class CollectibleUsedEventLinstener {
    @SubscribeEvent
    public static void onCollectibleUsedEvent(CollectibleEvent.Used event) {
        var player = event.getEntity();
        var level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            long currentTick = serverLevel.getServer().getTickCount();
            var cd = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get());
            cd.setCooldown(event.getItem(), 600, currentTick);
        }
    }

    @SubscribeEvent
    public static void onServerTickEvent(ServerTickEvent.Post event) {
        var server = event.getServer();
        var players = server.getPlayerList().getPlayers();
        long currentTick = server.getTickCount();
        for (var player : players) {
            player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get()).clearAllExpired(currentTick);
        }
    }

    @SubscribeEvent
    public static void onClientTickEvent(ClientTickEvent.Post event){
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var cd = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get());
            for (var item : cd.getCooldowns()){
                player.getCooldowns().addCooldown(item.value(),cd.getCooldown(item));
            }
        }
    }
}
