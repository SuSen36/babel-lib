package com.susen36.babel.elemental.base;

import com.susen36.babel.api.entity.ElementalAttackModifier;
import com.susen36.babel.api.entity.ElementalAttacker;
import com.susen36.babel.api.entity.ElementalDefender;
import com.susen36.babel.api.entity.ElementalDefenseModifier;
import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class EPManager {
    private static final Map<LivingEntity, Double> ELEMENTAL_DEFENSE_BASE_MODIFIERS = new WeakHashMap<>();

    private EPManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static @Nullable EPCapability getEP(LivingEntity entity) {
        return BabelCapability.getEP(entity);
    }

    public record ElementalAttackConfig(AbstractEPCapability.EPType type, double rate, double injuryDamage) {
    }

    public static ElementalAttackConfig getElementalAttackConfig(Entity entity) {
        LivingEntity attackerEntity = entity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner  ? owner
                : entity instanceof LivingEntity living ? living : null;
        AbstractEPCapability.EPType type = AbstractEPCapability.EPType.NERVOUS;
        double rate = 0;
        double injuryDamage = 0;
        if (attackerEntity instanceof ElementalAttacker attacker) {
            type = attacker.getElementalType();
            rate = attacker.getElementalRate();
            injuryDamage = attacker.getElementalInjuryDamage(entity);
        }
        if (attackerEntity != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = attackerEntity.getItemBySlot(slot);
                if (stack.getItem() instanceof ElementalAttacker attacker) {
                    rate += attacker.getElementalRate();
                    injuryDamage += attacker.getElementalInjuryDamage();
                    if (!(attackerEntity instanceof ElementalAttacker)) {
                        type = attacker.getElementalType();
                    }
                }
            }
            for (MobEffectInstance effect : attackerEntity.getActiveEffects()) {
                if (effect.getEffect() instanceof ElementalAttackModifier modifier) {
                    rate = modifier.modifyElementalRate(attackerEntity, rate, effect.getAmplifier());
                    injuryDamage += modifier.getElementalInjuryDamage(attackerEntity, effect.getAmplifier());
                }
            }
        }
        return new ElementalAttackConfig(type, rate, injuryDamage);
    }

    public record ElementalDefenseConfig(AbstractEPCapability.EPType type, double baseModifier, double totalModifier) {
    }

    public static void setElementalDefenseBaseModifier(LivingEntity entity, double modifier) {
        if (entity instanceof ElementalDefender defender) {
            defender.setElementalDefenseBaseModifier(modifier);
        } else if (entity != null) {
            ELEMENTAL_DEFENSE_BASE_MODIFIERS.put(entity, modifier);
        }
    }

    public static ElementalDefenseConfig getElementalDefenseConfig(LivingEntity entity) {
        AbstractEPCapability.EPType type = AbstractEPCapability.EPType.NERVOUS;
        double baseModifier = 1.0;
        double totalModifier = 1.0;
        if (entity instanceof ElementalDefender defender) {
            type = defender.getElementalDefenseType();
            baseModifier = defender.getElementalDefenseBaseModifier();
            totalModifier = defender.getElementalDefenseTotalModifier();
        }
        if (ELEMENTAL_DEFENSE_BASE_MODIFIERS.containsKey(entity)) {
            baseModifier = ELEMENTAL_DEFENSE_BASE_MODIFIERS.get(entity);
        }
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            if (effect.getEffect() instanceof ElementalDefenseModifier modifier
                    && modifier.getElementalDefenseType() == type) {
                baseModifier = modifier.modifyElementalDefenseBase(entity, baseModifier, effect.getAmplifier());
                totalModifier = modifier.modifyElementalDefenseTotal(entity, totalModifier, effect.getAmplifier());
            }
        }
        return new ElementalDefenseConfig(type, baseModifier, totalModifier);
    }

    public static boolean hurtElemental(LivingEntity target, AbstractEPCapability.EPType type, int amount) {
        boolean hurt = false;
        if (target != null && !type.isEmpty() && amount > 0) {
            EPCapability ep = getEP(target);
            hurt = ep != null && ep.hurt(type, amount);
        }
        return hurt;
    }

    public static boolean hurtElemental(LivingEntity target, AbstractEPCapability.EPType type, @Nullable LivingEntity attacker, int amount) {
        return hurtElemental(target, type, attacker, amount, ElementEvent.HurtType.ENTITY);
    }

    public static boolean hurtElemental(LivingEntity target, AbstractEPCapability.EPType type, @Nullable LivingEntity attacker, int amount, ElementEvent.HurtType hurtType) {
        boolean hurt = false;
        if (target != null && !type.isEmpty() && amount > 0) {
            ElementalInjurySource<?> source = attacker != null
                    ? ElementalInjurySource.from(attacker, null)
                    : ElementalInjurySource.fromNothing();
            EPCapability ep = getEP(target);
            hurt = ep != null && ep.hurt(type, source, amount, hurtType, attacker);
        }
        return hurt;
    }

    public static void healElemental(LivingEntity target, AbstractEPCapability.EPType type, int amount) {
        if (target != null && !type.isEmpty() && amount > 0) {
            EPCapability ep = getEP(target);
            if (ep != null) {
                ep.heal(type, amount);
            }
        }
    }

    public static void healToFull(LivingEntity entity, AbstractEPCapability.EPType type) {
        if (entity != null && !type.isEmpty()) {
            EPCapability ep = getEP(entity);
            AbstractEPCapability cap = ep != null ? ep.getEP(type) : null;
            float threshold = BabelAttributes.getMaxElementalValue(entity);
            if (cap != null && threshold > 0.0F) {
                cap.heal(Mth.floor(threshold));
            }
        }
    }

    public static float getRemainProgress(LivingEntity entity, AbstractEPCapability.EPType type) {
        float progress = 1.0F;
        if (entity != null && !type.isEmpty()) {
            EPCapability ep = getEP(entity);
            AbstractEPCapability cap = ep != null ? ep.getEP(type) : null;
            progress = cap == null ? 1.0F : (float) cap.getInjuryProgress();
        }
        return progress;
    }

    public static int getValue(LivingEntity entity, AbstractEPCapability.EPType type) {
        int value = 0;
        if (entity != null && !type.isEmpty()) {
            EPCapability ep = getEP(entity);
            AbstractEPCapability cap = ep != null ? ep.getEP(type) : null;
            value = cap == null ? 0 : cap.getValue();
        }
        return value;
    }

    public static float getMaxValue(LivingEntity entity, AbstractEPCapability.EPType type) {
        float maxValue = 0.0F;
        if (entity != null && !type.isEmpty()) {
            maxValue = BabelAttributes.getMaxElementalValue(entity);
        }
        return maxValue;
    }

    public static boolean underBurst(LivingEntity entity, AbstractEPCapability.EPType type) {
        boolean underBurst = false;
        if (entity != null && !type.isEmpty()) {
            EPCapability ep = getEP(entity);
            AbstractEPCapability cap = ep != null ? ep.getEP(type) : null;
            underBurst = cap != null && cap.underBurst();
        }
        return underBurst;
    }

    public static boolean isUnderBreak(LivingEntity entity) {
        boolean underBreak = false;
        if (entity != null) {
            EPCapability ep = getEP(entity);
            underBreak = ep != null && ep.isUnderBreak();
        }
        return underBreak;
    }

    public static boolean isImmune(LivingEntity entity, AbstractEPCapability.EPType type) {
        boolean immune = false;
        if (entity != null && !type.isEmpty()) {
            EPCapability ep = getEP(entity);
            AbstractEPCapability cap = ep != null ? ep.getEP(type) : null;
            immune = cap != null && cap.isImmune(ElementalInjurySource.fromNothing());
        }
        return immune;
    }

    public static void setImmune(LivingEntity entity, AbstractEPCapability.EPType type, int ticks) {
        if (entity != null && !type.isEmpty() && ticks >= 0) {
            EPCapability ep = getEP(entity);
            if (ep != null) {
                AbstractEPCapability cap = ep.getEP(type);
                if (cap != null) {
                    cap.setImmune(ticks);
                }
            }
        }
    }

    public static void setAllImmune(LivingEntity entity, int ticks) {
        if (entity != null && ticks >= 0) {
            EPCapability ep = getEP(entity);
            if (ep != null) {
                for (AbstractEPCapability.EPType type : AbstractEPCapability.EPType.values()) {
                    if (!type.isEmpty()) {
                        AbstractEPCapability cap = ep.getEP(type);
                        if (cap != null) {
                            cap.setImmune(ticks);
                        }
                    }
                }
            }
        }
    }

    public static void setPermanentImmunity(LivingEntity entity, AbstractEPCapability.EPType type) {
        if (entity != null && !type.isEmpty()) {
            EPCapability ep = getEP(entity);
            if (ep != null) {
                AbstractEPCapability cap = ep.getEP(type);
                if (cap != null) {
                    cap.setPermanentImmunity();
                }
            }
        }
    }
}