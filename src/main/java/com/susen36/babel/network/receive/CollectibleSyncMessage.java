package com.susen36.babel.network.receive;

import com.susen36.babel.BabelMod;
import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * 收藏品状态同步消息：把某玩家的「已使用集合 + 数值层」整体同步到客户端。
 * <p>
 * 数据载荷为单个 {@link CompoundTag}，内含两个子键：
 * <ul>
 *   <li>{@code used}：{@code ListTag}，已使用收藏品 id 集合（对应 {@code Collectibles.serializeNBT}）</li>
 *   <li>{@code layer}：{@code CompoundTag}，数值层存储（对应 {@code Collectibles.Layer.serializeNBT}）</li>
 * </ul>
 * 客户端在 {@link ClientPacketHandler#handle(CollectibleSyncMessage, IPayloadContext)} 中反写回玩家 attachment。
 */
public record CollectibleSyncMessage(int playerId, CompoundTag data) implements CustomPacketPayload {

    public static final Type<CollectibleSyncMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "collectible_sync"));

    public static final StreamCodec<FriendlyByteBuf, CollectibleSyncMessage> STREAM_CODEC = StreamCodec.of(
            CollectibleSyncMessage::encode,
            CollectibleSyncMessage::decode
    );

    public static CollectibleSyncMessage decode(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        CompoundTag data = buffer.readNbt();
        return new CollectibleSyncMessage(id, data);
    }

    public static void encode(FriendlyByteBuf buffer, CollectibleSyncMessage message) {
        buffer.writeInt(message.playerId());
        buffer.writeNbt(message.data());
    }

    public static void handle(CollectibleSyncMessage message, IPayloadContext context) {
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