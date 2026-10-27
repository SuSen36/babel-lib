package com.susen36.babel.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FeeblenessMobEffect extends MobEffect {
    public FeeblenessMobEffect() {
        super(MobEffectCategory.NEUTRAL, -13382401);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, "5f9a3c7e-1d2b-4c8a-9e0f-00000000000a", -0.8, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}