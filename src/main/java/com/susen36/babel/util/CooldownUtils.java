package com.susen36.babel.util;

public class CooldownUtils {
    private static long serverCurrentTick;

    public static long getServerCurrentTick() {
        return serverCurrentTick;
    }

    public static void setServerCurrentTick(long serverCurrentTick) {
        CooldownUtils.serverCurrentTick = serverCurrentTick;
    }
}