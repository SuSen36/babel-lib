package com.susen36.babel.collectible;

/**
 * 收藏品的稀有度等级，与原版物品稀有度一一对应并由基类自动映射：
 * CURSED/NORMAL → COMMON，RARE → RARE，ADVANCED → EPIC。默认等级为 {@link #NORMAL}。
 * <p>
 * 配置 ban_advanced_relics 禁用 {@link #ADVANCED}（高稀有度）级藏品。
 */
public enum CollectibleTiers {
    /**
     * 诅咒：携带负面效果的诅咒类藏品，物品名染红（原版稀有度 COMMON）。
     */
    CURSED,
    /**
     * 低稀有度：默认级别，绝大多数藏品（原版稀有度 COMMON）。
     */
    NORMAL,
    /**
     * 中稀有度：中高价值的稀有类藏品（原版稀有度 RARE）。
     */
    RARE,
    /**
     * 高稀有度：被配置 ban_advanced_relics 禁用的藏品（原版稀有度 EPIC）。
     */
    ADVANCED
}