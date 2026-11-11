package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;

public class JetBlackDanceShoesItem extends CollectibleItem.CustomCollectibleItem {

    public JetBlackDanceShoesItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 2F, 1F)
                        .particle(ParticleTypes.SMOKE, 72)
                        .showOverlay(true)
                        .build());
        addAttributeModifier(Attributes.MOVEMENT_SPEED, 0.08, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(ForgeMod.STEP_HEIGHT_ADDITION.get(), 0.45, AttributeModifier.Operation.ADDITION);
    }
}
