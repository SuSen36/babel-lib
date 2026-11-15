package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelGameRules;
import com.susen36.babel.network.BabelNetwork;
import com.susen36.babel.network.record.NDifficultySyncPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class NDifficultyValueSyncEvents {
    private static int lastSent = Integer.MIN_VALUE;

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            push(sp.server, PacketDistributor.PLAYER.with(() -> sp));
        }
    }

    /** 每20tick更新一次 */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % 20 != 0) return;

        int v = current(server);
        if (v != lastSent) {
            lastSent = v;
            BabelNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new NDifficultySyncPacket(v));
        }
    }

    /**服务器关闭时设置为初始值*/
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        lastSent = Integer.MIN_VALUE;
    }

    private static int current(MinecraftServer server) {
        return server.overworld().getGameRules().getInt(BabelGameRules.DIFFICULTY_LEVEL);
    }

    private static void push(MinecraftServer server, PacketDistributor.PacketTarget target) {
        lastSent = current(server);
        BabelNetwork.CHANNEL.send(target, new NDifficultySyncPacket(lastSent));
    }
}
