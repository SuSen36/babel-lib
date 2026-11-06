package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelEntityTypeTags;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public class CondensedSuppressantItem extends CollectibleItem.CustomCollectibleItem {

    private static final float DAMAGE_MULTIPLIER = 1.5F;
    private static final int SILENCE_DURATION_TICKS = 60;

    public CondensedSuppressantItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onDealDamage(LivingDamageEvent event, Player player, LivingEntity target, int layer) {
        if (target.getType().is(BabelEntityTypeTags.INFECTED_CREATURE)) {
            event.setAmount(event.getAmount() * DAMAGE_MULTIPLIER);
            target.addEffect(new MobEffectInstance(BabelMobEffects.SILENCED.get(), SILENCE_DURATION_TICKS, 0));
        }
    }
}