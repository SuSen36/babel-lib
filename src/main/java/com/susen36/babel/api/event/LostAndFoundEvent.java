package com.susen36.babel.api.event;

import com.susen36.babel.entity.CannotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

public class LostAndFoundEvent extends PlayerEvent {
    @Nullable
    private final CannotEntity trader;
    private final Item lost;

    public LostAndFoundEvent(Player player, @Nullable CannotEntity trader, Item lost) {
        super(player);
        this.trader = trader;
        this.lost = lost;
    }

    @Nullable
    public CannotEntity getTrader() {
        return trader;
    }

    public Item getLost() {
        return lost;
    }

    @Cancelable
    public static class Pre extends LostAndFoundEvent {
        private Item gained;

        public Pre(Player player, @Nullable CannotEntity trader, Item lost, Item gained) {
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

        public Post(Player player, @Nullable CannotEntity trader, Item lost, Item gained) {
            super(player, trader, lost);
            this.gained = gained;
        }

        public Item getGained() {
            return gained;
        }
    }

    @Nullable
    public static Item onPre(Player player, @Nullable CannotEntity trader, Item lost, Item gained) {
        Pre event = new Pre(player, trader, lost, gained);
        MinecraftForge.EVENT_BUS.post(event);
        Item result = null;
        if (!event.isCanceled()) {
            result = event.getGained();
        }
        return result;
    }

    public static void afterLostAndFound(Player player, @Nullable CannotEntity trader, Item lost, Item gained) {
        MinecraftForge.EVENT_BUS.post(new Post(player, trader, lost, gained));
    }
}