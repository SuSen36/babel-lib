package com.susen36.babel.network.receive;

import com.susen36.babel.network.ClientPacketHandler;
import com.susen36.babel.network.ClientboundHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RobberyRestrictionSyncMessage {

    public final long restrictedUntilTick;

    public RobberyRestrictionSyncMessage(long restrictedUntilTick) {
        this.restrictedUntilTick = restrictedUntilTick;
    }

    public static final BiConsumer<RobberyRestrictionSyncMessage, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeLong(message.restrictedUntilTick);
    };

    public static final Function<FriendlyByteBuf, RobberyRestrictionSyncMessage> DECODER = buffer ->
            new RobberyRestrictionSyncMessage(buffer.readLong());

    public static final BiConsumer<RobberyRestrictionSyncMessage, Supplier<NetworkEvent.Context>> HANDLER =
            ClientboundHandler.create(() -> ClientPacketHandler::handle);
}