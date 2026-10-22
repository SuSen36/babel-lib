package com.susen36.babel.api.event;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.ElementalInjurySource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class ElementEvent extends LivingEvent {
    private final AbstractEPCapability.EPType type;
    private final ElementalInjurySource<?> source;

    public ElementEvent(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source) {
        super(entity);
        this.type = type;
        this.source = source;
    }

    public AbstractEPCapability.EPType getType() {
        return type;
    }

    public ElementalInjurySource<?> getSource() {
        return source;
    }

    public enum HurtType {
        BLOCK, ENTITY, POTION, FOOD, DEFAULT
    }

    @Cancelable
    public static class Hurt extends ElementEvent {
        private int amount;
        private final HurtType hurtType;
        private final @Nullable LivingEntity attacker;

        public Hurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount) {
            this(entity, type, source, amount, HurtType.DEFAULT, null);
        }

        public Hurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount, HurtType hurtType, @Nullable LivingEntity attacker) {
            super(entity, type, source);
            this.amount = amount;
            this.hurtType = hurtType;
            this.attacker = attacker;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public HurtType getHurtType() {
            return hurtType;
        }

        public @Nullable LivingEntity getAttacker() {
            return attacker;
        }
    }

    public static class PostHurt extends ElementEvent {
        private final AbstractEPCapability.HurtResult result;

        public PostHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, AbstractEPCapability.HurtResult result) {
            super(entity, type, source);
            this.result = result;
        }

        public AbstractEPCapability.HurtResult getHurtResult() {
            return result;
        }
    }

    @Cancelable
    public static class Heal extends ElementEvent {
        private int amount;

        public Heal(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount) {
            super(entity, type, source);
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    public static class Burst extends ElementEvent {
        public Burst(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source) {
            super(entity, type, source);
        }
    }

    public static int onElementalHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount) {
        return onElementalHurt(entity, type, source, amount, HurtType.DEFAULT, null);
    }

    public static int onElementalHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount, HurtType hurtType, @Nullable LivingEntity attacker) {
        Hurt event = new Hurt(entity, type, source, amount, hurtType, attacker);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;
        return event.getAmount();
    }

    public static void afterElementalHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, AbstractEPCapability.HurtResult result) {
        MinecraftForge.EVENT_BUS.post(new PostHurt(entity, type, source, result));
    }

    public static int onElementalHeal(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, int amount) {
        Heal event = new Heal(entity, type, source, amount);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return 0;
        return event.getAmount();
    }

    public static void onElementalBurst(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source) {
        MinecraftForge.EVENT_BUS.post(new Burst(entity, type, source));
    }
}