package com.susen36.babel.effect;

import com.susen36.babel.init.BabelAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class MagicDodgeMobEffect extends MobEffect {
    public MagicDodgeMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x5B5BD6);
        this.addAttributeModifier(BabelAttributes.MAGIC_MISS_RATE.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000015", 25.0, AttributeModifier.Operation.ADDITION);
    }
}
