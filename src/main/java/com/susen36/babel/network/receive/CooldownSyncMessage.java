package com.susen36.babel.network.receive;

import com.susen36.babel.BabelMod;
import com.susen36.babel.network.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CooldownSyncMessage {

    public static final ResourceLocation ID = new ResourceLocation(BabelMod.MODID, "cooldown_sync");

    @Nullable
    public final ResourceLocation id;
    public final long maxTick;
    public final long currentTick;

    public CooldownSyncMessage(@Nullable ResourceLocation id, long maxTick, long currentTick) {
        this.id = id;
        this.maxTick = maxTick;
        this.currentTick = currentTick;
    }

    public static final BiConsumer<CooldownSyncMessage, FriendlyByteBuf> ENCODER = (message, buffer) -> {
        buffer.writeUtf(message.id == null ? "" : message.id.toString());
        buffer.writeLong(message.maxTick);
        buffer.writeLong(message.currentTick);
    };

    public static final Function<FriendlyByteBuf, CooldownSyncMessage> DECODER = buffer -> {
        String id = buffer.readUtf();
        ResourceLocation resourceLocation = id.isEmpty() ? null : new ResourceLocation(id);
        return new CooldownSyncMessage(resourceLocation, buffer.readLong(), buffer.readLong());
    };

    public static final BiConsumer<CooldownSyncMessage, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        ctx.get().enqueueWork(() -> ClientPacketHandler.handle(message));
        ctx.get().setPacketHandled(true);
    };
}
