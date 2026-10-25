package com.susen36.babel.network;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.network.receive.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public class BabelNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BabelMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private BabelNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, EPSyncMessage.class, EPSyncMessage.ENCODER, EPSyncMessage.DECODER, EPSyncMessage.HANDLER, Optional.empty());
        CHANNEL.registerMessage(packetId++, HealthSyncMessage.class, HealthSyncMessage.ENCODER, HealthSyncMessage.DECODER, HealthSyncMessage.HANDLER, Optional.empty());
        CHANNEL.registerMessage(packetId++, CollectibleSyncMessage.class, CollectibleSyncMessage.ENCODER, CollectibleSyncMessage.DECODER, CollectibleSyncMessage.HANDLER, Optional.empty());
        CHANNEL.registerMessage(packetId++, CollectibleItemUseMessage.class, CollectibleItemUseMessage.ENCODER, CollectibleItemUseMessage.DECODER, CollectibleItemUseMessage.HANDLER, Optional.empty());
        CHANNEL.registerMessage(packetId++, CooldownSyncMessage.class, CooldownSyncMessage.ENCODER, CooldownSyncMessage.DECODER, CooldownSyncMessage.HANDLER, Optional.empty());
        CHANNEL.registerMessage(packetId++, FireworkParticleMessage.class, FireworkParticleMessage.ENCODER, FireworkParticleMessage.DECODER, FireworkParticleMessage.HANDLER, Optional.empty());
        CHANNEL.registerMessage(packetId++, MerchantOffersMessage.class, MerchantOffersMessage.ENCODER, MerchantOffersMessage.DECODER, MerchantOffersMessage.HANDLER, Optional.empty());
    }

    public static void sendTo(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToTrackingAndSelf(Entity entity, Object message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public static void syncCollectibles(Player player) {
        CompoundTag data = new CompoundTag();
        data.put("used", Collectibles.serializeUsed(player));
        data.put("layer", Collectibles.serializeLayer(player));
        data.put("cooldown", Collectibles.serializeCooldown(player));
        sendToTrackingAndSelf(player, new CollectibleSyncMessage(player.getId(), data));
    }

    public static void syncCollectibleUse(ServerPlayer player, ItemStack stack) {
        sendTo(new CollectibleItemUseMessage(ForgeRegistries.ITEMS.getKey(stack.getItem())), player);
    }

    public static void syncCooldown(ServerPlayer player, @Nullable Item item, Long maxTick, Long currentTick) {
        if (item != null) {
            sendTo(new CooldownSyncMessage(ForgeRegistries.ITEMS.getKey(item), maxTick, currentTick), player);
        }
    }

    public static void syncServerCurrentTick(ServerPlayer player, Long currentTick) {
        sendTo(new CooldownSyncMessage(null, 0L, currentTick), player);
    }

    public static void sendFireworkParticles(Entity anchor, double x, double y, double z, CompoundTag fireworkNbt) {
        sendToTrackingAndSelf(anchor, new FireworkParticleMessage(x, y, z, fireworkNbt));
    }
}
