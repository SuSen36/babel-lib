package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.*;

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

    // 同一道雷对同一目标只奖励一次
    public static void onStruckByLightning(LightningBolt bolt, Entity target) {
        Player player = bolt.getCause();
        if (player != null && target instanceof LivingEntity living && ENEMY_OF_PLAYER.test(living, player)
                && Collectibles.isUsed(player, BabelCollectible.HAND_OF_DIFFUSION)) {
            boolean firstHit = STRUCK_ENTITIES.computeIfAbsent(bolt, ignored -> new HashSet<>()).add(target.getUUID());
            if (firstHit) {
                player.getFoodData().eat(1, 0.0F);
            }
        }
    }
}