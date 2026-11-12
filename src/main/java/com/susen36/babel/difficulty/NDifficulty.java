package com.susen36.babel.difficulty;

import com.susen36.babel.init.BabelGameRules;
import net.minecraft.world.level.LevelAccessor;

/**
 * 难度（Surging Waves）通用机制工具。
 * <p>
 * 仅提供难度等级读取、难度倍率计算、玩家进度检测与扩展模式过滤等通用能力，
 */
public final class NDifficulty {
    private NDifficulty() {
    }

    /** 当前世界的难度等级。 */
    public static NDifficultyLevel difficultyLevel(LevelAccessor world) {
        return NDifficultyLevel.of(world.getLevelData().getGameRules().getInt(BabelGameRules.DIFFICULTY_LEVEL));
    }

    /**
     * 难度倍率。大于0返回 {@code 1 + 0.01 * clampedLevel * 2}。
     */
    public static double multiplier(LevelAccessor world) {
        NDifficultyLevel dl = difficultyLevel(world);
        if (dl.value() == 0) {
            return 0.0;
        }
        return 1 + 0.01 * dl.value() * 2;
    }
}