package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.UUID;

public class AngelinaInspirationItem extends CollectibleItem.CustomCollectibleItem {

    private static final AttributeModifier ANGELINA_SLOW = new AttributeModifier(
            UUID.fromString("3a6d8f2b-9c4e-4d7a-8b1f-5e0c9a7d2b4c"), "babel:angelina_inspiration", -0.15, AttributeModifier.Operation.MULTIPLY_BASE);

    public AngelinaInspirationItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.ENCHANTMENT_TABLE_USE, 2F, 1F)
                        .particle(ParticleTypes.ENCHANT, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(Monster.class, (monster, player, layer) -> {
            AttributeInstance speed = monster.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.getModifier(UUID.fromString("3a6d8f2b-9c4e-4d7a-8b1f-5e0c9a7d2b4c")) == null) {
                speed.addTransientModifier(ANGELINA_SLOW);
            }
            monster.addEffect(new MobEffectInstance(BabelMobEffects.MASS_LOSS.get(), 60, 0));
        });
    }
}