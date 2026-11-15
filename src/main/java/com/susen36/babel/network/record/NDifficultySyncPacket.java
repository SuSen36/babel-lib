package com.susen36.babel.network.record;

import com.susen36.babel.difficulty.NDifficulty;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record NDifficultySyncPacket(int nDifficultyValue) {

    public static void encode(NDifficultySyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.nDifficultyValue);
    }

    public static NDifficultySyncPacket decode(FriendlyByteBuf buf) {
        return new NDifficultySyncPacket(buf.readVarInt());
    }

    public static void handle(NDifficultySyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> NDifficulty.acceptSynced(msg.nDifficultyValue));   // 必须回主线程
        c.setPacketHandled(true);
    }
}
