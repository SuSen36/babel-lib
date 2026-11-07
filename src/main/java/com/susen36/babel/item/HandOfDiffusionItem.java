package com.susen36.babel.item;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class HandOfDiffusionItem extends CollectibleItem.CustomCollectibleItem {

    private static final Map<LightningBolt, Set<UUID>> STRUCK_ENTITIES = new WeakHashMap<>();

    public HandOfDiffusionItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
    }

    @SubscribeEvent
    public static void onStruckByLightning(EntityStruckByLightningEvent event) {
        Player player = event.getLightning().getCause();
        if (player != null && event.getEntity() != player
                && Collectibles.isUsed(player, BabelCollectible.HAND_OF_DIFFUSION)
                && STRUCK_ENTITIES.computeIfAbsent(event.getLightning(), bolt -> new HashSet<>()).add(event.getEntity().getUUID())) {
            player.getFoodData().eat(1, 0.0F);
        }
    }
}