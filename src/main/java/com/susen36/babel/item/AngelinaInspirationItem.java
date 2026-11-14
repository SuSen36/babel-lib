package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

public class AngelinaInspirationItem extends CollectibleItem.CustomCollectibleItem {

    public AngelinaInspirationItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.ENCHANTMENT_TABLE_USE, 2F, 1F)
                        .particle(ParticleTypes.ENCHANT, 72)
                        .showOverlay(true)
                        .build());
        addRangeAttributeModifier(Mob.class, Attributes.MOVEMENT_SPEED, -0.15, AttributeModifier.Operation.MULTIPLY_BASE, ENEMY_OF_PLAYER);
        addRangeEffect(Mob.class, (monster, player, layer) -> {
            monster.addEffect(new MobEffectInstance(
                    BabelMobEffects.MASS_LOSS.get(), 60, 0, false, false));
        }, ENEMY_OF_PLAYER);
    }
}