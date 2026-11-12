package com.susen36.babel.difficulty;

/**
 * 探索难度阶段划分。
 */
public enum NDifficultyTier {
    /** [0, 2]：简单。 */
    EASY(0, 2),
    /** [3, 5]：普通。 */
    NORMAL(3, 5),
    /** [6, 8]：困难。 */
    HARD(6, 8),
    /** [9, 11]：专家。 */
    EXPERT(9, 11),
    /** [12, 18]：险境。 */
    PERILOUS(12, 18);

    private final int min;
    private final int max;

    NDifficultyTier(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int min() {
        return this.min;
    }

    public int max() {
        return this.max;
    }

    /** 根据具体数值获取对应的阶段，默认兜底为 EASY。 */
    public static NDifficultyTier fromValue(int value) {
        NDifficultyTier result = EASY;
        for (NDifficultyTier tier : values()) {
            if (value >= tier.min && value <= tier.max) {
                result = tier;
                break;
            }
        }
        return result;
    }
}