package com.susen36.babel.network.receive;

import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MerchantOffersMessage {

    public final int containerId;
    public final CompoundTag offers;

    public MerchantOffersMessage(int containerId, CompoundTag offers) {
        this.containerId = containerId;
        this.offers = offers;
    }

    public static final BiConsumer<MerchantOffersMessage, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeVarInt(message.containerId);
        buffer.writeNbt(message.offers);
    };

    public static final Function<FriendlyByteBuf, MerchantOffersMessage> DECODER = buffer ->
            new MerchantOffersMessage(buffer.readVarInt(), buffer.readNbt());

    public static final BiConsumer<MerchantOffersMessage, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handle(message));
        ctx.get().setPacketHandled(true);
    };
}