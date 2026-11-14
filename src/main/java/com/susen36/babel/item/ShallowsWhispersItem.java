package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

public class ShallowsWhispersItem extends CollectibleItem.CustomCollectibleItem {

    public ShallowsWhispersItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.CURSED, new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.FIRE_EXTINGUISH, 2F, 1F)
                        .particle(ParticleTypes.SMOKE, 72)
                        .autoUseOnGain(true)
                        .build());
        addRangeAttributeModifier(Mob.class, Double.POSITIVE_INFINITY, Attributes.ATTACK_DAMAGE, 0.08D, AttributeModifier.Operation.MULTIPLY_BASE, ENEMY_OF_PLAYER);
        addRangeAttributeModifier(Mob.class, Double.POSITIVE_INFINITY, Attributes.ARMOR, 0.08D, AttributeModifier.Operation.MULTIPLY_BASE, ENEMY_OF_PLAYER);
        addRangeAttributeModifier(Mob.class, Double.POSITIVE_INFINITY, Attributes.MAX_HEALTH, 0.08D, AttributeModifier.Operation.MULTIPLY_BASE, ENEMY_OF_PLAYER);
        addAttributeModifier(Attributes.MAX_HEALTH, -0.15D, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}