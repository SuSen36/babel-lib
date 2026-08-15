package com.susen36.babel.network;

import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.network.receive.CollectibleItemUseMessage;
import com.susen36.babel.network.receive.CollectibleSyncMessage;
import com.susen36.babel.network.receive.CooldownSyncMessage;
import com.susen36.babel.network.receive.EPSyncMessage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class BabelNetwork {

    private BabelNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(EPSyncMessage.TYPE, EPSyncMessage.STREAM_CODEC, EPSyncMessage::handle);
        registrar.playToClient(CollectibleSyncMessage.TYPE, CollectibleSyncMessage.STREAM_CODEC, CollectibleSyncMessage::handle);
        registrar.playToClient(CollectibleItemUseMessage.TYPE, CollectibleItemUseMessage.STREAM_CODEC, CollectibleItemUseMessage::handle);
        registrar.playToClient(CooldownSyncMessage.TYPE, CooldownSyncMessage.STREAM_CODEC, CooldownSyncMessage::handle);
    }

    public static void syncCollectibles(Player player) {
        var access = player.registryAccess();
        CompoundTag data = new CompoundTag();
        data.put("used", player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get()).serializeNBT(access));
        data.put("layer", player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_LAYER.get()).serializeNBT(access));
        data.put("cooldown", player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get()).serializeNBT(access));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new CollectibleSyncMessage(player.getId(), data));
    }

    public static void syncCollectibleUse(ServerPlayer player, ItemStack stack) {
        PacketDistributor.sendToPlayer(player, new CollectibleItemUseMessage(BuiltInRegistries.ITEM.getKey(stack.getItem())));
    }

    public static void syncCooldown(ServerPlayer player, Item item, Long maxTick, Long currentTick) {
        if (item != null) {
            PacketDistributor.sendToPlayer(player, new CooldownSyncMessage(BuiltInRegistries.ITEM.getKey(item), maxTick, currentTick));
        }
    }

    public static void syncServerCurrentTick(ServerPlayer player, Long currentTick) {
        PacketDistributor.sendToPlayer(player, new CooldownSyncMessage(null, 0L, currentTick));
    }
}