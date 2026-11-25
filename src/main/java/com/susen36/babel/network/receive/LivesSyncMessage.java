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

public class LivesSyncMessage {

    public final int entityId;
    @Nullable
    public final CompoundTag nbt;

    public LivesSyncMessage(int entityId, @Nullable CompoundTag nbt) {
        this.entityId = entityId;
        this.nbt = nbt;
    }

    public static final BiConsumer<LivesSyncMessage, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.nbt);
    };

    public static final Function<FriendlyByteBuf, LivesSyncMessage> DECODER = buffer ->
            new LivesSyncMessage(buffer.readInt(), buffer.readNbt());

    public static final BiConsumer<LivesSyncMessage, Supplier<NetworkEvent.Context>> HANDLER =
            ClientboundHandler.create(() -> ClientPacketHandler::handle);
}