package com.susen36.babel.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

public class MassLossMobEffect extends MobEffect {
    public MassLossMobEffect() {
        super(MobEffectCategory.NEUTRAL, -13382401);
        this.addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000016", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "5f9a3c7e-1d2b-4c8a-9e0f-000000000017", -0.2, AttributeModifier.Operation.ADDITION);
    }
}