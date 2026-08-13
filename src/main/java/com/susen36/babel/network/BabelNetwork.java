package com.susen36.babel.network;

import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.network.receive.CollectibleSyncMessage;
import com.susen36.babel.network.receive.EPSyncMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class BabelNetwork {

    private BabelNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(EPSyncMessage.TYPE, EPSyncMessage.STREAM_CODEC, EPSyncMessage::handle);
        registrar.playToClient(CollectibleSyncMessage.TYPE, CollectibleSyncMessage.STREAM_CODEC, CollectibleSyncMessage::handle);
    }

    public static void syncEP(Entity entity, EPCapability ep) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new EPSyncMessage(ep));
    }

    /**
     * 同步某玩家的收藏品状态（已使用集合 + 数值层）到客户端。
     */
    public static void syncCollectibles(Player player) {
        var access = player.registryAccess();
        CompoundTag data = new CompoundTag();
        data.put("used", player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get()).serializeNBT(access));
        data.put("layer", player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_LAYER.get()).serializeNBT(access));
        data.put("cooldown", player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get()).serializeNBT(access));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new CollectibleSyncMessage(player.getId(), data));
    }
}