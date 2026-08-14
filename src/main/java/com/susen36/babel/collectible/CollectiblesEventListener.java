package com.susen36.babel.collectible;

import com.susen36.babel.api.event.CollectibleEvent;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class CollectiblesEventListener {
    @SubscribeEvent
    public static void onCollectibleUsedEvent(@NotNull CollectibleEvent.Used event) {
        var player = event.getEntity();
        var level = player.level();
        if (level instanceof ServerLevel) {
            var cd = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get());
            cd.setCooldownEndTick(event.getItem(), level.getServer(), 12000L);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        BabelNetwork.syncCollectibles(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.level() instanceof ServerLevel serverLevel)
            BabelNetwork.syncServerCurrentTick(serverPlayer, serverLevel.getServer().overworld().getGameTime());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        event.getServer().getPlayerList().getPlayers().forEach(player -> BabelNetwork.syncServerCurrentTick(player, event.getServer().overworld().getGameTime()));
    }

    @SubscribeEvent
    public static void onRegisterItemDecorationsEvent(RegisterItemDecorationsEvent event) {
        var items = Collectibles.Collectibles.values();
        for (var item : items) {
            event.register(item.value(), new CollectiblesDecorator());
        }
    }
}
