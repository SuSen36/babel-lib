package com.susen36.babel.network.receive;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record HealthSyncMessage(int entityId, CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<HealthSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "health_sync"));

    public static final StreamCodec<FriendlyByteBuf, HealthSyncMessage> STREAM_CODEC = StreamCodec.of(
            HealthSyncMessage::encode,
            HealthSyncMessage::decode
    );

    public HealthSyncMessage(HealthCapability health) {
        this(health.getEntity().getId(), health.serializeNBT(health.getEntity().registryAccess()));
    }

    public static HealthSyncMessage decode(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        CompoundTag nbt = buffer.readNbt();
        return new HealthSyncMessage(id, nbt);
    }

    public static void encode(FriendlyByteBuf buffer, HealthSyncMessage message) {
        buffer.writeInt(message.entityId());
        buffer.writeNbt(message.nbt());
    }

    public static void handle(HealthSyncMessage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                ClientPacketHandler.handle(message, context);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}