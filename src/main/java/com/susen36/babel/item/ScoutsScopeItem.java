package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;

public class ScoutsScopeItem extends CollectibleItem.CustomCollectibleItem {
    /** 增幅到 +100% 封顶的距离。 */
    private static final float MAX_RANGE = 32.0F;

    public ScoutsScopeItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addDamageEffect(Monster.class, (event, player, target, layer) -> {
            float bonus = Mth.clamp(target.distanceTo(player) / MAX_RANGE, 0.0F, 1.0F);
            event.setAmount(event.getAmount() * (1.0F + bonus));
        });
    }
}