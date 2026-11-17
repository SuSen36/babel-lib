package com.susen36.babel.item;

import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class MiniatureStageModelItem extends CollectibleItem.CustomCollectibleItem {

    private static final int BUFF_DURATION_TICKS = 400;

    public MiniatureStageModelItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.NOTE, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onLifePointLoss(LifePointConsumeEvent event, Player player, int layer) {
        if (!player.level().isClientSide()) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_DURATION_TICKS, 1));
            player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, BUFF_DURATION_TICKS, 0));
        }
    }
}