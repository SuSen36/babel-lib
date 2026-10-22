package com.susen36.babel.network;

import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.network.receive.CollectibleItemUseMessage;
import com.susen36.babel.network.receive.CollectibleSyncMessage;
import com.susen36.babel.network.receive.CooldownSyncMessage;
import com.susen36.babel.network.receive.EPSyncMessage;
import com.susen36.babel.network.receive.HealthSyncMessage;
import com.susen36.babel.util.CooldownUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ClientPacketHandler {

    private ClientPacketHandler() {
    }

    public static void handle(EPSyncMessage message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(message.entityId);
            if (entity instanceof LivingEntity living) {
                EPCapability ep = BabelCapability.getEP(living);
                if (ep != null) {
                    ep.deserializeNBT(message.nbt);
                }
            }
        }
    }

    public static void handle(HealthSyncMessage message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(message.entityId);
            if (entity instanceof LivingEntity living) {
                HealthCapability health = BabelCapability.getHealth(living);
                if (health != null) {
                    health.deserializeNBT(message.nbt);
                }
            }
        }
    }

    public static void handle(CollectibleSyncMessage message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(message.playerId);
            if (entity instanceof Player player) {
                Collectibles.deserializeUsed(player, message.data.getList("used", Tag.TAG_STRING));
                Collectibles.deserializeLayer(player, message.data.getCompound("layer"));
                Collectibles.deserializeCooldown(player, message.data.getCompound("cooldown"));
            }
        }
    }

    public static void handle(CollectibleItemUseMessage message) {
        Item item = ForgeRegistries.ITEMS.getValue(message.id);
        if (item != null) {
            Minecraft.getInstance().gameRenderer.displayItemActivation(item.getDefaultInstance());
        }
    }

    public static void handle(CooldownSyncMessage message) {
        if (message.id != null) {
            Item item = ForgeRegistries.ITEMS.getValue(message.id);
            if (item != null) {
                CooldownUtils.setLastMaxCooldownTick(item, message.maxTick);
            }
        }
        CooldownUtils.setServerCurrentTick(message.currentTick);
    }
}
