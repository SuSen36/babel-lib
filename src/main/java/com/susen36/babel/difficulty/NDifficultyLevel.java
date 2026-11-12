package com.susen36.babel.difficulty;

import net.minecraft.util.Mth;

/**
 * 难度等级值类型。
 */
public final class NDifficultyLevel {
    private static final NDifficultyLevel EASY = new NDifficultyLevel(0);

    private final int value;
    private final NDifficultyTier tier;

    private NDifficultyLevel(int value) {
        this.value = value;
        this.tier = NDifficultyTier.fromValue(value);
    }

    public static NDifficultyLevel of(int value) {
        return value <= 0 ? EASY : new NDifficultyLevel(value);
    }

    /** 获取当前难度的阶段分类（容易、普通、困难、专家、险境）。 */
    public NDifficultyTier tier() {
        return this.tier;
    }

    /** 难度等级值[0, 18]。 */
    public int value() {
        return Mth.clamp(this.value, 0, 18);
    }

    @Override
    public String toString() {
        return this.tier.name().toLowerCase() + "-" + value();
    }
}