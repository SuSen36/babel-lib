package com.susen36.babel.api;

import com.susen36.babel.api.entity.ElementalAttackModifier;
import com.susen36.babel.api.entity.ElementalAttacker;
import com.susen36.babel.api.entity.ElementalDefenseModifier;
import com.susen36.babel.api.entity.ElementalDefender;
import com.susen36.babel.api.event.ElementEvent;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.ElementalInjurySource;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class BabelAPI {
    private static final Map<LivingEntity, Double> ELEMENTAL_DEFENSE_BASE_MODIFIERS = new WeakHashMap<>();

    private BabelAPI() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static EPCapability getEP(LivingEntity entity) {
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
            rate = attacker.getElementalRate() * 10.0D;
            injuryDamage = attacker.getElementalInjuryDamage();
        }
        if (attackerEntity == null) {
            return new ElementalAttackConfig(type, rate, injuryDamage);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = attackerEntity.getItemBySlot(slot);
            if (stack.getItem() instanceof ElementalAttacker attacker) {
                rate += attacker.getElementalRate() * 10.0D;
                injuryDamage += attacker.getElementalInjuryDamage();
                if (!(attackerEntity instanceof ElementalAttacker)) {
                    type = attacker.getElementalType();
                }
            }
        }
        for (MobEffectInstance effect : attackerEntity.getActiveEffects()) {
            if (effect.getEffect().value() instanceof ElementalAttackModifier modifier) {
                rate = modifier.modifyElementalRate(attackerEntity, rate, effect.getAmplifier());
                injuryDamage += modifier.getElementalInjuryDamage(attackerEntity, effect.getAmplifier());
            }
        }
        return new ElementalAttackConfig(type, rate, injuryDamage);
    }

    public record ElementalDefenseConfig(AbstractEPCapability.EPType type, double baseModifier, double totalModifier) {
    }

    public static void setElementalDefenseBaseModifier(LivingEntity entity, double modifier) {
        if (entity != null) {
            if (entity instanceof ElementalDefender defender) {
                defender.setElementalDefenseBaseModifier(modifier);
            } else {
                ELEMENTAL_DEFENSE_BASE_MODIFIERS.put(entity, modifier);
            }
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
            if (effect.getEffect().value() instanceof ElementalDefenseModifier modifier
                    && modifier.getElementalDefenseType() == type) {
                baseModifier = modifier.modifyElementalDefenseBase(entity, baseModifier, effect.getAmplifier());
                totalModifier = modifier.modifyElementalDefenseTotal(entity, totalModifier, effect.getAmplifier());
            }
        }
        return new ElementalDefenseConfig(type, baseModifier, totalModifier);
    }

    public static boolean hurtElemental(LivingEntity target, AbstractEPCapability.EPType type, int amount) {
        if (target == null || type.isEmpty() || amount <= 0) return false;
        return BabelCapability.getEP(target).hurt(type, amount);
    }

    public static boolean hurtElemental(LivingEntity target, AbstractEPCapability.EPType type, @Nullable LivingEntity attacker, int amount) {
        return hurtElemental(target, type, attacker, amount, ElementEvent.HurtType.ENTITY);
    }

    public static boolean hurtElemental(LivingEntity target, AbstractEPCapability.EPType type, @Nullable LivingEntity attacker, int amount, ElementEvent.HurtType hurtType) {
        if (target == null || type.isEmpty() || amount <= 0) return false;
        ElementalInjurySource<?> source = attacker != null
                ? ElementalInjurySource.from(attacker, null)
                : ElementalInjurySource.fromNothing();
        return BabelCapability.getEP(target).hurt(type, source, amount, hurtType, attacker);
    }

    public static void healElemental(LivingEntity target, AbstractEPCapability.EPType type, int amount) {
        if (target == null || type.isEmpty() || amount <= 0) return;
        BabelCapability.getEP(target).heal(type, amount);
    }

    public static void healToFull(LivingEntity entity, AbstractEPCapability.EPType type) {
        if (entity == null || type.isEmpty()) return;
        EPCapability ep = BabelCapability.getEP(entity);
        AbstractEPCapability cap = ep.getEP(type);
        if (cap == null) return;
        float threshold = BabelAttributes.getMaxElementalValue(entity);
        if (threshold <= 0.0F) return;
        cap.heal(Mth.floor(threshold));
    }

    public static float getRemainProgress(LivingEntity entity, AbstractEPCapability.EPType type) {
        if (entity == null || type.isEmpty()) return 1.0F;
        EPCapability ep = BabelCapability.getEP(entity);
        AbstractEPCapability cap = ep.getEP(type);
        return cap == null ? 1.0F : (float) cap.getInjuryProgress();
    }

    public static int getValue(LivingEntity entity, AbstractEPCapability.EPType type) {
        if (entity == null || type.isEmpty()) return 0;
        EPCapability ep = BabelCapability.getEP(entity);
        AbstractEPCapability cap = ep.getEP(type);
        return cap == null ? 0 : cap.getValue();
    }

    public static float getMaxValue(LivingEntity entity, AbstractEPCapability.EPType type) {
        if (entity == null || type.isEmpty()) return 0.0F;
        return BabelAttributes.getMaxElementalValue(entity);
    }

    public static boolean underBurst(LivingEntity entity, AbstractEPCapability.EPType type) {
        if (entity == null || type.isEmpty()) return false;
        EPCapability ep = BabelCapability.getEP(entity);
        AbstractEPCapability cap = ep.getEP(type);
        return cap != null && cap.underBurst();
    }

    public static boolean isUnderBreak(LivingEntity entity) {
        if (entity == null) return false;
        return BabelCapability.getEP(entity).isUnderBreak();
    }
}