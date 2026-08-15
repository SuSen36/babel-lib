package com.susen36.babel.network.receive;

import com.susen36.babel.BabelMod;
import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CooldownSyncMessage(
        ResourceLocation id,
        Long maxTick,
        Long currentTick
) implements CustomPacketPayload {

    public static final Type<CooldownSyncMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "cooldown_sync"));

    public static final StreamCodec<FriendlyByteBuf, CooldownSyncMessage> STREAM_CODEC = StreamCodec.of(
            CooldownSyncMessage::encode,
            CooldownSyncMessage::decode
    );

    public static void encode(FriendlyByteBuf buffer, CooldownSyncMessage message) {
        buffer.writeUtf(message.id() == null ? "" : message.id().toString());
        buffer.writeLong(message.maxTick());
        buffer.writeLong(message.currentTick());
    }

    public static CooldownSyncMessage decode(FriendlyByteBuf buffer) {
        String id = buffer.readUtf();
        ResourceLocation resourceLocation = id.isEmpty() ? null : ResourceLocation.parse(id);
        return new CooldownSyncMessage(
                resourceLocation,
                buffer.readLong(),
                buffer.readLong()
        );
    }

    public static void handle(CooldownSyncMessage message, IPayloadContext context) {
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