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
     * 玩家进度系数。对每个在线玩家，取 {@code surging_waves_entry} 前四个进度中最后一个已完成的序号 +2
     * 作为该玩家系数（默认 1），最终取所有玩家的最大值。
     */
    public static int progressCoefficient(LevelAccessor world) {
        int coef = 1;
        List<? extends String> entries = BabelConfig.surgingWavesEntry;
        for (Entity player : world.players()) {
            if (player instanceof ServerPlayer plr) {
                int cur = 1;
                for (int i = 0; i < Math.min(entries.size(), 4); i++) {
                    ResourceLocation entryAdvancement = ResourceLocation.parse(entries.get(i));
                    AdvancementHolder advancement = plr.getServer().getAdvancements().get(entryAdvancement);

                    if (advancement != null && plr.getAdvancements().getOrStartProgress(advancement).isDone()) {
                        cur = i + 2;
                    }
                }
                if (cur > coef) {
                    coef = cur;
                }
            }
        }
        return coef;
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
        return Math.pow(n, progressCoefficient(world));
    }
}