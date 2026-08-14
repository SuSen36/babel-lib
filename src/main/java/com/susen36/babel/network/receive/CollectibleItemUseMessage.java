package com.susen36.babel.network.receive;

import com.susen36.babel.BabelMod;
import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record CollectibleItemUseMessage(ResourceLocation id) implements CustomPacketPayload {

    public static final Type<CollectibleItemUseMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "collectible_item_use_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CollectibleItemUseMessage> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    CollectibleItemUseMessage::id,
                    CollectibleItemUseMessage::new
            );

    public static void handle(CollectibleItemUseMessage message, IPayloadContext context) {
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
