package com.susen36.babel.network.receive;

import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

// 收藏品状态同步消息：把某玩家的收藏品状态（已激活标记、数值层、冷却）整体同步到客户端。
// 数据载荷为单个 CompoundTag，仅含一个键 states：ListTag，每个元素对应一件收藏品，
// 内含 id（收藏品 id）、used（是否已激活）、layer（数值层）、cooldown（冷却结束 tick）、duration（冷却时长）。
public class CollectibleSyncMessage {

    public final int playerId;
    public final CompoundTag data;

    public CollectibleSyncMessage(int playerId, CompoundTag data) {
        this.playerId = playerId;
        this.data = data;
    }

    public static final BiConsumer<CollectibleSyncMessage, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeInt(message.playerId);
        buffer.writeNbt(message.data);
    };

    public static final Function<FriendlyByteBuf, CollectibleSyncMessage> DECODER = buffer ->
            new CollectibleSyncMessage(buffer.readInt(), buffer.readNbt());

    public static final BiConsumer<CollectibleSyncMessage, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handle(message));
        ctx.get().setPacketHandled(true);
    };
}