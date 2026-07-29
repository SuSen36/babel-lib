package com.susen36.babel.network.receive;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EPSyncMessage(int entityId, CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<EPSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "ep_sync"));

    public static final StreamCodec<FriendlyByteBuf, EPSyncMessage> STREAM_CODEC = StreamCodec.of(
            EPSyncMessage::encode,
            EPSyncMessage::decode
    );

    public EPSyncMessage(EPCapability ep) {
        this(ep.getEntity().getId(), ep.serializeNBT(ep.getEntity().registryAccess()));
    }

    public static EPSyncMessage decode(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        CompoundTag nbt = buffer.readNbt();
        return new EPSyncMessage(id, nbt);
    }

    public static void encode(FriendlyByteBuf buffer, EPSyncMessage message) {
        buffer.writeInt(message.entityId());
        buffer.writeNbt(message.nbt());
    }

    public static void handle(EPSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientPacketHandler.handle(message, context);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}