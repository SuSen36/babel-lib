package com.susen36.babel.network.receive;

import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 收藏品状态同步消息：把某玩家的「已使用集合 + 数值层 + 冷却」整体同步到客户端。
 * <p>
 * 数据载荷为单个 {@link CompoundTag}，内含三个子键：
 * <ul>
 *   <li>{@code used}：{@code ListTag}，已使用收藏品 id 集合</li>
 *   <li>{@code layer}：{@code CompoundTag}，数值层存储</li>
 *   <li>{@code cooldown}：{@code CompoundTag}，冷却存储</li>
 * </ul>
 */
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