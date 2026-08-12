package com.susen36.babel.collectible;

/**
 * 收藏品物品标记接口：声明层数范围（min/max/default），供 {@link Collectibles.Layer} 做 clamp 与缺省读取。
 * 默认 0/1/0 等价布尔型；可叠层收藏品（如负值起点的叠层层数）由具体物品覆盖。
 */
public interface CollectibleLike {
    default int minLevel() {
        return 0;
    }

    default int maxLevel() {
        return 1;
    }

    default int defaultLevel() {
        return 0;
    }
}