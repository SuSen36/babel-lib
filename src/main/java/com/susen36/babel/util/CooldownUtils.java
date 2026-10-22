package com.susen36.babel.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.HashMap;

public class CooldownUtils {
    private static final HashMap<Holder<Item>, Long> lastMaxCooldown = new HashMap<>();
    private static long serverCurrentTick;

    public static long getServerCurrentTick() {
        return serverCurrentTick;
    }

    public static void setServerCurrentTick(long serverCurrentTick) {
        CooldownUtils.serverCurrentTick = serverCurrentTick;
    }

    public static void setLastMaxCooldownTick(Item item, Long maxTick) {
        setLastMaxCooldownTick(BuiltInRegistries.ITEM.wrapAsHolder(item), maxTick);
    }

    public static void setLastMaxCooldownTick(Holder<Item> item, Long maxTick) {
        lastMaxCooldown.put(item, maxTick);
    }

    public static void resetLastMaxCooldownTick(Item item) {
        resetLastMaxCooldownTick(BuiltInRegistries.ITEM.wrapAsHolder(item));
    }

    public static void resetLastMaxCooldownTick(Holder<Item> item) {
        lastMaxCooldown.put(item, 0L);
    }

    public static long getLastMaxCooldownTick(Item item) {
        return getLastMaxCooldownTick(BuiltInRegistries.ITEM.wrapAsHolder(item));
    }

    public static long getLastMaxCooldownTick(Holder<Item> item) {
        return lastMaxCooldown.getOrDefault(item, 0L);
    }
}
