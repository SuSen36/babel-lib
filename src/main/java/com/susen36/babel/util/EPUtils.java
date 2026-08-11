package com.susen36.babel.util;

import com.susen36.babel.manager.EPManager;
import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class EPUtils {
    private EPUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void causeElementalInjury(LivingEntity target, AbstractEPCapability.EPType type, double value) {
        causeElementalInjury(target, type, null, value);
    }

    public static void causeElementalInjury(LivingEntity target, AbstractEPCapability.EPType type, LivingEntity attacker, double value) {
        EPManager.hurtElemental(target, type, attacker, Mth.floor(value));
    }

    public static void causeSanityInjury(LivingEntity target, double value) {
        causeElementalInjury(target, AbstractEPCapability.EPType.NERVOUS, value);
    }

    public static void causeSanityInjury(LivingEntity target, LivingEntity attacker, double value) {
        causeElementalInjury(target, AbstractEPCapability.EPType.NERVOUS, attacker, value);
    }

    public static void causeBurnInjury(LivingEntity target, double value) {
        causeElementalInjury(target, AbstractEPCapability.EPType.BURN, value);
    }

    public static void causeBurnInjury(LivingEntity target, LivingEntity attacker, double value) {
        causeElementalInjury(target, AbstractEPCapability.EPType.BURN, attacker, value);
    }

    public static void causeCorrosionInjury(LivingEntity target, double value) {
        causeElementalInjury(target, AbstractEPCapability.EPType.CORROSION, value);
    }

    public static void causeCorrosionInjury(LivingEntity target, LivingEntity attacker, double value) {
        causeElementalInjury(target, AbstractEPCapability.EPType.CORROSION, attacker, value);
    }

    public static void causeNecrosisInjury(LivingEntity target, double value) {
        causeElementalInjury(target, AbstractEPCapability.EPType.NECROSIS, value);
    }

    public static void causeNecrosisInjury(LivingEntity target, LivingEntity attacker, double value) {
        causeElementalInjury(target, AbstractEPCapability.EPType.NECROSIS, attacker, value);
    }

    public static void healElemental(LivingEntity target, AbstractEPCapability.EPType type, double value) {
        EPManager.healElemental(target, type, Mth.floor(value));
    }

    public static void healAllElemental(LivingEntity target, double value) {
        for (AbstractEPCapability.EPType type : AbstractEPCapability.EPType.values()) {
            if (!type.isEmpty()) {
                healElemental(target, type, value);
            }
        }
    }

    public static void healAllElementalToFull(LivingEntity target) {
        for (AbstractEPCapability.EPType type : AbstractEPCapability.EPType.values()) {
            if (!type.isEmpty()) {
                EPManager.healToFull(target, type);
            }
        }
    }

    public static boolean isUnderBurst(LivingEntity target, AbstractEPCapability.EPType type) {
        return EPManager.underBurst(target, type);
    }

    public static boolean isAnyBurst(LivingEntity target) {
        for (AbstractEPCapability.EPType type : AbstractEPCapability.EPType.values()) {
            if (!type.isEmpty() && EPManager.underBurst(target, type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSanityBursting(LivingEntity target) {
        return isUnderBurst(target, AbstractEPCapability.EPType.NERVOUS);
    }

    public static boolean isBurnBursting(LivingEntity target) {
        return isUnderBurst(target, AbstractEPCapability.EPType.BURN);
    }

    public static boolean isCorrosionBursting(LivingEntity target) {
        return isUnderBurst(target, AbstractEPCapability.EPType.CORROSION);
    }

    public static boolean isNecrosisBursting(LivingEntity target) {
        return isUnderBurst(target, AbstractEPCapability.EPType.NECROSIS);
    }

    public static boolean isImmune(LivingEntity target, AbstractEPCapability.EPType type) {
        return EPManager.isImmune(target, type);
    }

    public static boolean isAnyImmune(LivingEntity target) {
        for (AbstractEPCapability.EPType type : AbstractEPCapability.EPType.values()) {
            if (!type.isEmpty() && EPManager.isImmune(target, type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSanityImmune(LivingEntity target) {
        return isImmune(target, AbstractEPCapability.EPType.NERVOUS);
    }

    public static boolean isBurnImmune(LivingEntity target) {
        return isImmune(target, AbstractEPCapability.EPType.BURN);
    }

    public static boolean isCorrosionImmune(LivingEntity target) {
        return isImmune(target, AbstractEPCapability.EPType.CORROSION);
    }

    public static boolean isNecrosisImmune(LivingEntity target) {
        return isImmune(target, AbstractEPCapability.EPType.NECROSIS);
    }

    public static void setImmune(LivingEntity target, AbstractEPCapability.EPType type, int ticks) {
        EPManager.setImmune(target, type, ticks);
    }

    public static void setAllImmune(LivingEntity target, int ticks) {
        EPManager.setAllImmune(target, ticks);
    }

    public static void setSanityImmune(LivingEntity target, int ticks) {
        setImmune(target, AbstractEPCapability.EPType.NERVOUS, ticks);
    }

    public static void setBurnImmune(LivingEntity target, int ticks) {
        setImmune(target, AbstractEPCapability.EPType.BURN, ticks);
    }

    public static void setCorrosionImmune(LivingEntity target, int ticks) {
        setImmune(target, AbstractEPCapability.EPType.CORROSION, ticks);
    }

    public static void setNecrosisImmune(LivingEntity target, int ticks) {
        setImmune(target, AbstractEPCapability.EPType.NECROSIS, ticks);
    }

    public static int getElementValue(LivingEntity target, AbstractEPCapability.EPType type) {
        return EPManager.getValue(target, type);
    }

    public static float getElementMaxValue(LivingEntity target, AbstractEPCapability.EPType type) {
        return EPManager.getMaxValue(target, type);
    }

    public static float getElementRemainProgress(LivingEntity target, AbstractEPCapability.EPType type) {
        return EPManager.getRemainProgress(target, type);
    }

    public static int getSanityValue(LivingEntity target) {
        return getElementValue(target, AbstractEPCapability.EPType.NERVOUS);
    }

    public static int getBurnValue(LivingEntity target) {
        return getElementValue(target, AbstractEPCapability.EPType.BURN);
    }

    public static int getCorrosionValue(LivingEntity target) {
        return getElementValue(target, AbstractEPCapability.EPType.CORROSION);
    }

    public static int getNecrosisValue(LivingEntity target) {
        return getElementValue(target, AbstractEPCapability.EPType.NECROSIS);
    }

    public static AbstractEPCapability.EPType getLowestElementType(LivingEntity target) {
        AbstractEPCapability.EPType lowest = AbstractEPCapability.EPType.EMPTY;
        int lowestValue = Integer.MAX_VALUE;
        for (AbstractEPCapability.EPType type : AbstractEPCapability.EPType.values()) {
            if (type.isEmpty()) continue;
            int value = EPManager.getValue(target, type);
            if (value < lowestValue) {
                lowestValue = value;
                lowest = type;
            }
        }
        return lowest;
    }

    public static int getLowestElementValue(LivingEntity target) {
        AbstractEPCapability.EPType type = getLowestElementType(target);
        return type.isEmpty() ? 0 : EPManager.getValue(target, type);
    }

    public static AbstractEPCapability.EPType getHighestElementType(LivingEntity target) {
        AbstractEPCapability.EPType highest = AbstractEPCapability.EPType.EMPTY;
        int highestValue = Integer.MIN_VALUE;
        for (AbstractEPCapability.EPType type : AbstractEPCapability.EPType.values()) {
            if (type.isEmpty()) continue;
            int value = EPManager.getValue(target, type);
            if (value > highestValue) {
                highestValue = value;
                highest = type;
            }
        }
        return highest;
    }

    public static int getHighestElementValue(LivingEntity target) {
        AbstractEPCapability.EPType type = getHighestElementType(target);
        return type.isEmpty() ? 0 : EPManager.getValue(target, type);
    }
}