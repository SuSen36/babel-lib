package com.susen36.babel.api.entity;

import net.minecraft.world.entity.LivingEntity;

public interface ElementalAttackModifier {
    double modifyElementalRate(LivingEntity entity, double rate, int amplifier);

    default double getElementalInjuryDamage(LivingEntity entity, int amplifier) {
        return 0;
    }
}