package com.susen36.babel.util;

public class RobberyClientUtils {
    private static long restrictedUntilTick;

    // Suppresses default constructor, ensuring non-instantiability.
    private RobberyClientUtils() {
    }

    public static long getRemainingTicks() {
        return Math.max(0L, restrictedUntilTick - CooldownUtils.getServerCurrentTick());
    }

    public static void setRestrictedUntilTick(long restrictedUntilTick) {
        RobberyClientUtils.restrictedUntilTick = restrictedUntilTick;
    }
}