package com.susen36.babel.network.receive;

import com.susen36.babel.BabelMod;
import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CollectibleItemUseMessage {

    public static final ResourceLocation ID = new ResourceLocation(BabelMod.MODID, "collectible_item_use_sync");

    public final ResourceLocation id;

    public CollectibleItemUseMessage(ResourceLocation id) {
        this.id = id;
    }

    public static final BiConsumer<CollectibleItemUseMessage, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeResourceLocation(message.id);
    };

    public static final Function<FriendlyByteBuf, CollectibleItemUseMessage> DECODER = buffer ->
            new CollectibleItemUseMessage(buffer.readResourceLocation());

    public static final BiConsumer<CollectibleItemUseMessage, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handle(message));
        ctx.get().setPacketHandled(true);
    };
}