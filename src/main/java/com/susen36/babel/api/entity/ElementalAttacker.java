package com.susen36.babel.api.entity;

import com.susen36.babel.elemental.base.AbstractEPCapability;

public interface ElementalAttacker {
    AbstractEPCapability.EPType getElementalType();

    double getElementalRate();

    double getElementalInjuryDamage();
}