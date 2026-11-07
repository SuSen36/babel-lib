package com.susen36.babel.collectible;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.CollectibleDropEvent;
import com.susen36.babel.api.event.CollectibleEvent;
import com.susen36.babel.api.event.OriginiumIngotDropEvent;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class CollectiblesEventListener {

    @SubscribeEvent
    public static void onCollectibleUsedEvent(@NotNull CollectibleEvent.Used event) {
        var player = event.getEntity();
        var level = player.level();
        if (level instanceof ServerLevel) {
            var cd = Collectibles.getCooldownData(player);
            cd.setCooldownEndTick(event.getItem(), level.getServer(), BabelConfig.collectibleDurationTicks);
        }
    }

    @SubscribeEvent
    public static void onOriginiumIngotDrop(@NotNull OriginiumIngotDropEvent event) {
        if (event.getKiller() instanceof Player player && Collectibles.isUsed(player, BabelCollectible.SUPREME_RING)) {
            event.setMultiplier(event.getMultiplier() * 1.5F);
        }
    }

    @SubscribeEvent
    public static void onCollectibleDrop(@NotNull CollectibleDropEvent event) {
        if (event.getKiller() instanceof Player player && Collectibles.isUsed(player, BabelCollectible.FOUR_LEAF_CLOVER_FOSSIL)) {
            event.setChance(event.getChance() * 2.0F);
        }
    }

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