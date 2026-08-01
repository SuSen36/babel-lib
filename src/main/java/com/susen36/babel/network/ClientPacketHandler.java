package com.susen36.babel.network;

import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.network.receive.EPSyncMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
}