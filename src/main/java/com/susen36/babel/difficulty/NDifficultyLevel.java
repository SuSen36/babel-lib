package com.susen36.babel.difficulty;

import net.minecraft.util.Mth;

/**
 * 难度等级值类型。
 * <ul>
 *     <li>easy：游戏规则值为 0 或 -1，简单模式，不参与倍率计算。</li>
 *     <li>normal：游戏规则值为 1-18，数值即难度等级，参与倍率计算。</li>
 *     <li>hard：游戏规则值为 19，困难模式，等级最高 19，多出来的1级不参与倍率计算。</li>
 * </ul>
 */
//java小知识:类声明为final意味着该类不能被继承
public final class NDifficultyLevel {
    private static final NDifficultyLevel EASY = new NDifficultyLevel(0);

    private final int value;

    private NDifficultyLevel(int value) {
        this.value = value;
    }

    public static NDifficultyLevel of(int value) {
        return value <= 0 ? EASY : new NDifficultyLevel(value);
    }

    /** 是否为 normal 难度（值 1-18）。 */
    public boolean isNormal() {
        return this.value > 0 && this.value < 19;
    }

    /** 是否为 hard 难度（值 19-21，等级最高）。 */
    public boolean isHard() {
        return this.value >= 19 && this.value <= 21;
    }

    /** 是否为 easy 难度（值 &lt;= 0，即 0 或 -1）。 */
    public boolean isEasy() {
        return this.value <= 0;
    }

    /** 难度等级值[0, 21]。 */
    public int value() {
        return Mth.clamp(this.value, 0, 21);
    }

    @Override
    public String toString() {
        return isEasy() ? "easy" : isHard() ? "hard" : "normal-" + this.value;
    }
}