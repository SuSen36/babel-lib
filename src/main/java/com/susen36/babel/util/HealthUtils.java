package com.susen36.babel.util;

import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.health.HealthCapability;
import net.minecraft.world.entity.Entity;

public class HealthUtils {

    private HealthUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static HealthCapability get(Entity entity) {
        return BabelCapability.getHealth(entity);
    }

    // ========== 生命点数 ==========

    public static int getLifePoint(Entity entity) {
        return (int) get(entity).getLives();
    }

    public static void setLifePoint(Entity entity, int value) {
        if (value < 1) {
            return;
        }
        HealthCapability c = get(entity);
        c.setLives(value);
        c.sync();
    }

    public static int getMaxLifePoint(Entity entity) {
        return (int) get(entity).getMaxLives();
    }

    public static void setMaxLifePoint(Entity entity, int value) {
        if (value < 1) {
            return;
        }
        HealthCapability c = get(entity);
        c.setMaxLives(value);
        c.sync();
    }

    // ========== 护盾点数 ==========

    public static int getShieldPoint(Entity entity) {
        return (int) get(entity).getShield();
    }

    public static void setShieldPoint(Entity entity, int value) {
        if (value < 0) {
            return;
        }
        HealthCapability c = get(entity);
        c.setShield(value);
        c.sync();
    }
}