package com.susen36.babel.difficulty;

import com.susen36.babel.init.BabelGameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * 难度（Surging Waves）通用机制工具。
 * <p>
 * 仅提供难度等级读取、难度倍率计算、玩家进度检测与扩展模式过滤等通用能力，
 */
public final class NDifficulty {
    private NDifficulty() {
    }

    //同步NDifficultyValue
    private static volatile int syncedValue = -1;
    /**设置{@code 同步NDifficultyValue}*/
    public static void acceptSynced(int value) { syncedValue = value; }
    /**清除{@code 同步NDifficultyValue}*/
    public static void clearSynced()          { syncedValue = -1; }

    /**
     * 当前世界的难度等级。
     * <p>
     * 创造模式搜索树构建物品提示时会传入 null 世界（ItemStack.getTooltipLines(null, ...)），此处回退到最低难度。
     * <p>
     * 仅客户端时将会使用缓存值
     */
    public static NDifficultyLevel difficultyLevel(LevelAccessor world) {
        if (world == null || (world instanceof Level lv && lv.isClientSide)) {
            return NDifficultyLevel.of(Math.max(syncedValue, 0));
        }
        return NDifficultyLevel.of(
                world.getLevelData().getGameRules().getInt(BabelGameRules.DIFFICULTY_LEVEL));
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

    /**
     * 返回对应难度下的 {@code Default * multiplier[?]} <p>
     * 数组长度必须为4 <p>
     * 否则返回{@code Default}
     */
    public static int multiplier(LevelAccessor world,int Default,float[] multiplier){
        int effectDuration = Default;
        if (multiplier.length != 4) return effectDuration;

        effectDuration = switch (NDifficulty.difficultyLevel(world).tier()) {
            case EASY -> Math.round(Default * multiplier[0]);
            case NORMAL -> Math.round(Default * multiplier[1]);
            case HARD -> Math.round(Default * multiplier[2]);
            case EXPERT , PERILOUS -> Math.round(Default * multiplier[3]);
        };
        return effectDuration;
    }

    /**
     * 返回对应难度下的 {@code value[?]} <p>
     * 数组长度必须为4 <p>
     * 否则返回{@code Default}
     */
    public static int setValue(LevelAccessor world,int Default,int[] value){
        int valueND = Default;
        if (value.length != 4) return valueND;

        valueND = switch (NDifficulty.difficultyLevel(world).tier()) {
            case EASY -> value[0];
            case NORMAL -> value[1];
            case HARD -> value[2];
            case EXPERT , PERILOUS -> value[3];
        };
        return valueND;
    }
}