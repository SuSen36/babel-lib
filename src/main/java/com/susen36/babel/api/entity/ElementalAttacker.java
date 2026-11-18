package com.susen36.babel.api.entity;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.world.entity.Entity;

/**
 * 可施加元素损伤的攻击方。实体本身或任意装备栏物品实现该接口后，
 * EPManager 读取其损伤类型、累积速率与单次损伤值并合并计算。
 */
public interface ElementalAttacker {
    AbstractEPCapability.EPType getElementalType();

    double getElementalRate();

    double getElementalInjuryDamage();

    default double getElementalInjuryDamage(Entity directEntity) {
        return getElementalInjuryDamage();
    }
}