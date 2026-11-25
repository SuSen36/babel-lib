package com.susen36.babel.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * 收集品事件：当玩家使用 / 移除收集品或变更收集品层级时发布，供特效、UI、属性刷新等系统松耦合订阅。
 * <p>
 * 三个子事件：
 * <ul>
 *   <li>{@link Used}        - 玩家使用（获得）收集品，成功激活时触发</li>
 *   <li>{@link Removed}     - 玩家移除收集品</li>
 *   <li>{@link LayerChanged}- 收集品层级变更</li>
 * </ul>
 */
public class CollectibleEvent extends PlayerEvent {

    private final Item item;

    public CollectibleEvent(Player player, Item item) {
        super(player);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    /** 玩家使用（获得）收集品。 */
    public static class Used extends CollectibleEvent {

        public Used(Player player, Item item) {
            super(player, item);
        }
    }

    /** 玩家移除收集品。 */
    public static class Removed extends CollectibleEvent {

        public Removed(Player player, Item item) {
            super(player, item);
        }
    }

    /** 收集品层级变更。 */
    public static class LayerChanged extends CollectibleEvent {

        private final int newLayer;

        public LayerChanged(Player player, Item item, int newLayer) {
            super(player, item);
            this.newLayer = newLayer;
        }

        public int getNewLayer() {
            return newLayer;
        }
    }
}