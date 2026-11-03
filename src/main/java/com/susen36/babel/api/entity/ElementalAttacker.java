package com.susen36.babel.api.entity;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.world.entity.Entity;

public interface ElementalAttacker {
    AbstractEPCapability.EPType getElementalType();

    double getElementalRate();

    double getElementalInjuryDamage();

    default double getElementalInjuryDamage(Entity directEntity) {
        return getElementalInjuryDamage();
    }
}