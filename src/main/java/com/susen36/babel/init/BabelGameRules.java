package com.susen36.babel.init;

import net.minecraft.world.level.GameRules;

/**
 * babel 库提供的游戏规则。
 * <p>
 * {@code difficultyLevel}：难度等级。
 * 0 或 -1 为 easy（简单模式，不触发难度倍率）；1-18 为 normal（数值即难度，参与倍率计算）。
 */
public final class BabelGameRules {
    private BabelGameRules() {
    }

    public static void init() {
    }

    public static final GameRules.Key<GameRules.IntegerValue> DIFFICULTY_LEVEL =
            GameRules.register("difficultyLevel", GameRules.Category.MISC, GameRules.IntegerValue.create(0));
}