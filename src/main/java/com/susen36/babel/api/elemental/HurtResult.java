package com.susen36.babel.api.elemental;

import org.jetbrains.annotations.Nullable;

/** 单次损伤的结算快照；burstDamage 是爆条基数，不是最终扣血或周期伤害总和；source 是来源视图，不复制其中的游戏对象。 */
public record HurtResult(boolean failed, boolean shouldBurst, Reason reason, int damage,
                         int burstDamage, @Nullable ElementSource<?> source) {
    public enum Reason { LOCKED, RESISTED, THRESHOLD, NORMAL, FAILED, ZERO_AMOUNT }
}