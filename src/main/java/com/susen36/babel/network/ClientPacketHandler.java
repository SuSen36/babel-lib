package com.susen36.babel.network;

import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.network.receive.CollectibleSyncMessage;
import com.susen36.babel.network.receive.EPSyncMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {

    private ClientPacketHandler() {
    }

    public static void handle(EPSyncMessage message, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(message.entityId());
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                ep.deserializeNBT(mc.level.registryAccess(), message.nbt());
            }
        }
    }

    public static void handle(CollectibleSyncMessage message, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(message.playerId());
            if (entity instanceof Player player) {
                var access = mc.level.registryAccess();
                player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get())
                        .deserializeNBT(access, message.data().getList("used", Tag.TAG_STRING));
                player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_LAYER.get())
                        .deserializeNBT(access, message.data().getCompound("layer"));
            }
        }
    }
}