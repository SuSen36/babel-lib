package com.susen36.babel.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ClientboundHandler {
    private ClientboundHandler() {
    }

    // 注册发生在两端；仅在物理客户端、正确的逻辑方向上解析接收器并处理游戏状态。
    public static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> create(Supplier<Consumer<T>> receiver) {
        return (message, contextSupplier) -> {
            NetworkEvent.Context context = contextSupplier.get();
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT && FMLEnvironment.dist == Dist.CLIENT) {
                context.enqueueWork(() -> receiver.get().accept(message));
            }
            context.setPacketHandled(true);
        };
    }
}