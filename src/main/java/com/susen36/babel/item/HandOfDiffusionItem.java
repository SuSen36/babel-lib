package com.susen36.babel.item;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class HandOfDiffusionItem extends CollectibleItem.CustomCollectibleItem {

    private static final int PER_HIT_TICKS = 2 * 20;
    private static final int MAX_DURATION_TICKS = 45 * 20;
    private static final Map<LightningBolt, Set<UUID>> STRUCK_ENTITIES = new WeakHashMap<>();

    public HandOfDiffusionItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
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
                && Collectibles.getData(player).isUsed(BabelCollectible.HAND_OF_DIFFUSION.get())
                && STRUCK_ENTITIES.computeIfAbsent(event.getLightning(), bolt -> new HashSet<>()).add(event.getEntity().getUUID())) {
            MobEffectInstance current = player.getEffect(MobEffects.DAMAGE_BOOST);
            int duration = current != null ? Math.min(current.getDuration() + PER_HIT_TICKS, MAX_DURATION_TICKS) : PER_HIT_TICKS;
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 1));
        }
    }
}