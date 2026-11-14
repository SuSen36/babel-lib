package com.susen36.babel.item;

import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;

public class LittleGranFaroItem extends CollectibleItem.CustomCollectibleItem {

    private static final double STUN_RANGE = 16.0D;
    private static final int STUN_DURATION_TICKS = 100;

    public LittleGranFaroItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.BEACON_ACTIVATE, 2F, 1F)
                        .particle(ParticleTypes.END_ROD, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onLifePointLoss(LifePointConsumeEvent event, Player player, int layer) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class,
                new AABB(player.position(), player.position()).inflate(STUN_RANGE),
                entity -> ENEMY_OF_PLAYER.test(entity, player))) {
            mob.addEffect(new MobEffectInstance(BabelMobEffects.STUN.get(), STUN_DURATION_TICKS, 0, false, false));
        }
    }
}
