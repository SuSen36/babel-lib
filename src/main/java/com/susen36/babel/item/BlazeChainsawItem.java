package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;

public class BlazeChainsawItem extends CollectibleItem.CustomCollectibleItem {
    /** 加成衰减到 +0% 的距离。 */
    private static final float MAX_RANGE = 16.0F;

    public BlazeChainsawItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addDamageEffect(Mob.class, (event, player, target, layer) -> {
            float bonus = Mth.clamp(1.0F - target.distanceTo(player) / MAX_RANGE, 0.0F, 1.0F);
            event.setAmount(event.getAmount() * (1.0F + bonus));
        }, ENEMY_OF_PLAYER);
    }
}