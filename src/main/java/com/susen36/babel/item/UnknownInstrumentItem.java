package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class UnknownInstrumentItem extends CollectibleItem.CustomCollectibleItem {

    public UnknownInstrumentItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2F, 1F)
                        .particle(ParticleTypes.NOTE, 72)
                        .showOverlay(true)
                        .build());
        addAttributeModifier(Attributes.MAX_HEALTH, 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}