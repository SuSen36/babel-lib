package com.susen36.babel.api.entity;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.world.entity.LivingEntity;

public interface ElementalDefenseModifier {
    AbstractEPCapability.EPType getElementalDefenseType();

    double modifyElementalDefenseBase(LivingEntity entity, double modifier, int amplifier);

    default double modifyElementalDefenseTotal(LivingEntity entity, double modifier, int amplifier) {
        return modifier;
    }
}
