package com.susen36.babel.api.entity;

import com.susen36.babel.elemental.base.AbstractEPCapability;

public interface ElementalDefender {
    default AbstractEPCapability.EPType getElementalDefenseType() {
        return AbstractEPCapability.EPType.NERVOUS;
    }

    default double getElementalDefenseBaseModifier() {
        return 1.0;
    }

    default double getElementalDefenseTotalModifier() {
        return 1.0;
    }

    default void setElementalDefenseBaseModifier(double modifier) {
    }
}
