package com.susen36.babel.util;

public class CooldownUtils {
    private static long serverCurrentTick;

    // Suppresses default constructor, ensuring non-instantiability.
    private CooldownUtils() {
    }

    public static long getServerCurrentTick() {
        return serverCurrentTick;
    }

    public static void setServerCurrentTick(long serverCurrentTick) {
        CooldownUtils.serverCurrentTick = serverCurrentTick;
    }
}