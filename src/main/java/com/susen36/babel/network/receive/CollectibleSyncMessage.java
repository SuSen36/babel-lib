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

/**
 * 收藏品状态同步消息：把某玩家的收藏品状态整体同步到客户端。
 * <p>
 * 数据载荷为单个 {@link CompoundTag}，只含一个 {@code states} 子键：
 * <ul>
 *   <li>{@code states}：{@code ListTag}，每个元素是一个 {@code CompoundTag}，包含
 *   {@code id}、{@code used}、{@code used_up}、{@code layer}、{@code cooldown}、
 *   {@code duration}、{@code configured_duration}</li>
 * </ul>
 */
public class CollectibleSyncMessage {

    public final int playerId;
    @Nullable
    public final CompoundTag data;

    public CollectibleSyncMessage(int playerId, @Nullable CompoundTag data) {
        this.playerId = playerId;
        this.data = data;
    }

    public static final BiConsumer<CollectibleSyncMessage, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeInt(message.playerId);
        buffer.writeNbt(message.data);
    };

    public static final Function<FriendlyByteBuf, CollectibleSyncMessage> DECODER = buffer ->
            new CollectibleSyncMessage(buffer.readInt(), buffer.readNbt());

    public static final BiConsumer<CollectibleSyncMessage, Supplier<NetworkEvent.Context>> HANDLER =
            ClientboundHandler.create(() -> ClientPacketHandler::handle);
}