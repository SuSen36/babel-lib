package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class ActorPerfumeItem extends CollectibleItem.CustomCollectibleItem {

    public ActorPerfumeItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return tickCount % 100 == 0;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        player.heal(Math.max(player.getMaxHealth() * 0.05F, 1.0F));
    }
}