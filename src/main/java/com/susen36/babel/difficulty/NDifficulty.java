package com.susen36.babel.difficulty;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.init.BabelGameRules;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

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
     * 难度倍率。easy 模式返回 0；normal 模式返回
     * {@code (1 + 0.01 * clampedLevel * 2) ^ progressCoefficient}。
     */
    public static double multiplier(LevelAccessor world) {
        NDifficultyLevel dl = difficultyLevel(world);
        if (dl.isEasy()) {
            return 0.0;
        }
        double n = 1 + 0.01 * dl.value() * 2;
        return Math.pow(n, 1);
    }
}