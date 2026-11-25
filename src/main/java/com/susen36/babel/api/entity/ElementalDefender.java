package com.susen36.babel.api.entity;

import com.susen36.babel.api.elemental.ElementType;

/**
 * 可被元素损伤命中的防御方。实现后由 EPManager 读取受击时的损伤类型与基础/总倍率，
 * 未实现时按神经损伤与 1.0 倍率处理。
 */
public interface ElementalDefender {
    default ElementType getElementalDefenseType() {
        return ElementType.NERVOUS;
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