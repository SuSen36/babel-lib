package com.susen36.babel.network.receive;

import com.susen36.babel.network.ClientPacketHandler;
import com.susen36.babel.network.ClientboundHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FireworkParticleMessage {

    public final double x;
    public final double y;
    public final double z;
    @Nullable
    public final CompoundTag fireworkNbt;

    public FireworkParticleMessage(double x, double y, double z, @Nullable CompoundTag fireworkNbt) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fireworkNbt = fireworkNbt;
    }

    public static final BiConsumer<FireworkParticleMessage, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeNbt(message.fireworkNbt);
    };

    public static final Function<FriendlyByteBuf, FireworkParticleMessage> DECODER = buffer ->
            new FireworkParticleMessage(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readNbt());

    public static final BiConsumer<FireworkParticleMessage, Supplier<NetworkEvent.Context>> HANDLER =
            ClientboundHandler.create(() -> ClientPacketHandler::handle);
}