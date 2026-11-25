package com.susen36.babel.api.event;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

/**
 * 坎诺特「失与得」兑换事件：{@link Pre} 在结算前发布，可取消或通过 {@link Pre#setGained(Item)} 替换获得的藏品；
 * {@link Post} 在兑换完成后发布，只读。
 */
public class LostAndFoundEvent extends PlayerEvent {
    @Nullable
    private final AbstractVillager trader;
    private final Item lost;

    public LostAndFoundEvent(Player player, @Nullable AbstractVillager trader, Item lost) {
        super(player);
        this.trader = trader;
        this.lost = lost;
    }

    @Nullable
    public AbstractVillager getTrader() {
        return trader;
    }

    public Item getLost() {
        return lost;
    }

    @Cancelable
    public static class Pre extends LostAndFoundEvent {
        private Item gained;

        public Pre(Player player, @Nullable AbstractVillager trader, Item lost, Item gained) {
            super(player, trader, lost);
            this.gained = gained;
        }

        public Item getGained() {
            return gained;
        }

        public void setGained(Item gained) {
            this.gained = gained;
        }
    }

    public static class Post extends LostAndFoundEvent {
        private final Item gained;

        public Post(Player player, @Nullable AbstractVillager trader, Item lost, Item gained) {
            super(player, trader, lost);
            this.gained = gained;
        }

        public Item getGained() {
            return gained;
        }
    }
}