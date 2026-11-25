package com.susen36.babel.api.entity;

import com.susen36.babel.api.elemental.ElementType;
import net.minecraft.world.entity.LivingEntity;

/**
 * 防御侧倍率修改挂钩，通常由药水效果实现；仅在效果类型与受击方损伤类型一致时生效。
 */
public interface ElementalDefenseModifier {
    ElementType getElementalDefenseType();

    double modifyElementalDefenseBase(LivingEntity entity, double modifier, int amplifier);

    default double modifyElementalDefenseTotal(LivingEntity entity, double modifier, int amplifier) {
        return modifier;
    }
}