package com.susen36.babel.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MassLossMobEffect extends MobEffect {
    public MassLossMobEffect() {
        super(MobEffectCategory.NEUTRAL, -13382401);
        this.addAttributeModifier(Attributes.GRAVITY, ResourceLocation.fromNamespaceAndPath("babel", "weightless_gravity"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath("babel", "weightless_explosion_knockback_resistance"), -0.2, AttributeModifier.Operation.ADD_VALUE);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath("babel", "weightless_knockback_resistance"), -0.2, AttributeModifier.Operation.ADD_VALUE);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}