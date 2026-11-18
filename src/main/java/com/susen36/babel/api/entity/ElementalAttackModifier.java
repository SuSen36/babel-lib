package com.susen36.babel.api.entity;

import net.minecraft.world.entity.LivingEntity;

/**
 * 攻击侧倍率修改挂钩，通常由药水效果实现：先修改累积速率，再叠加单次损伤值。
 */
public interface ElementalAttackModifier {
    double modifyElementalRate(LivingEntity entity, double rate, int amplifier);

    default double getElementalInjuryDamage(LivingEntity entity, int amplifier) {
        return 0;
    }
}