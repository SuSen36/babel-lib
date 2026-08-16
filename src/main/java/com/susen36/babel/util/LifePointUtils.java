package com.susen36.babel.util;

import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.health.HealthCapability;
import net.minecraft.world.entity.Entity;

public class LifePointUtils {

    private LifePointUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static HealthCapability get(Entity entity) {
        return BabelCapability.getHealth(entity);
    }

    public static int getLifePoint(Entity entity) {
        return (int) get(entity).getLives();
    }

    public static void setLifePoint(Entity entity, int value) {
        if (value < 1) {
            return;
        }
        HealthCapability capability = get(entity);
        capability.setLives(value);
        capability.sync();
    }

    public static void addLifePoint(Entity entity, int delta) {
        if (delta == 0) {
            return;
        }
        HealthCapability capability = get(entity);
        capability.setLives(capability.getLives() + delta);
        capability.sync();
    }

    public static int getMaxLifePoint(Entity entity) {
        return (int) get(entity).getMaxLives();
    }

    public static void setMaxLifePoint(Entity entity, int value) {
        if (value < 1) {
            return;
        }
        HealthCapability capability = get(entity);
        capability.setMaxLives(value);
        capability.sync();
    }

    public static int getShieldPoint(Entity entity) {
        return (int) get(entity).getShield();
    }

    public static void setShieldPoint(Entity entity, int value) {
        if (value < 0) {
            return;
        }
        HealthCapability capability = get(entity);
        capability.setShield(value);
        capability.sync();
    }

    public static void addShieldPoint(Entity entity, int delta) {
        if (delta == 0) {
            return;
        }
        HealthCapability capability = get(entity);
        capability.setShield(capability.getShield() + delta);
        capability.sync();
    }
}