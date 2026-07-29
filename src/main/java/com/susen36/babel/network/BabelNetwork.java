package com.susen36.babel.network;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.network.receive.EPSyncMessage;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = BabelMod.MODID)
public class BabelNetwork {

    private BabelNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(EPSyncMessage.TYPE, EPSyncMessage.STREAM_CODEC, EPSyncMessage::handle);
    }

    public static void syncEP(Entity entity, EPCapability ep) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new EPSyncMessage(ep));
    }
}