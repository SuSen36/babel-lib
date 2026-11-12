package com.susen36.babel.init;

import net.minecraft.world.level.GameRules;

/**
 * babel 库提供的游戏规则。
 * <p>
 * {@code difficultyLevel}：难度等级。
 * 0不触发难度倍率，1-18 参与倍率计算。
 */
public final class BabelGameRules {
    private BabelGameRules() {
    }

    public static void init() {
    }

    public static final GameRules.Key<GameRules.IntegerValue> DIFFICULTY_LEVEL =
            GameRules.register("difficultyLevel", GameRules.Category.MISC, GameRules.IntegerValue.create(0));

    public static final GameRules.Key<GameRules.BooleanValue> TARGET_LIFE_FUNCTION =
            GameRules.register("targetLifeFunction", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
}