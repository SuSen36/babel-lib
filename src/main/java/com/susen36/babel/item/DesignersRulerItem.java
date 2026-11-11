package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

public class DesignersRulerItem extends CollectibleItem.CustomCollectibleItem {

    public DesignersRulerItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.ARMOR_EQUIP_NETHERITE, 2F, 1F)
                        .particle(ParticleTypes.CRIT, 72)
                        .showOverlay(true)
                        .build());
        addAttributeModifier(BabelAttributes.PHYSICAL_MISS_RATE.get(), 15.0D, AttributeModifier.Operation.ADDITION);
    }
}