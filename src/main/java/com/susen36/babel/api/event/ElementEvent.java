package com.susen36.babel.api.event;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.elemental.base.ElementalInjurySource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

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

    public static class Hurt extends ElementEvent implements ICancellableEvent {
        private float amount;

        public Hurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, float amount) {
            super(entity, type, source);
            this.amount = amount;
        }

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
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

    public static class Heal extends ElementEvent implements ICancellableEvent {
        private float amount;

        public Heal(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, float amount) {
            super(entity, type, source);
            this.amount = amount;
        }

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }
    }

    public static class Burst extends ElementEvent {
        public Burst(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source) {
            super(entity, type, source);
        }
    }

    public static float onElementalHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, float amount) {
        Hurt event = new Hurt(entity, type, source, amount);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return 0.0F;
        return event.getAmount();
    }

    public static void afterElementalHurt(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, AbstractEPCapability.HurtResult result) {
        NeoForge.EVENT_BUS.post(new PostHurt(entity, type, source, result));
    }

    public static float onElementalHeal(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source, float amount) {
        Heal event = new Heal(entity, type, source, amount);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) return 0.0F;
        return event.getAmount();
    }

    public static void onElementalBurst(LivingEntity entity, AbstractEPCapability.EPType type, ElementalInjurySource<?> source) {
        NeoForge.EVENT_BUS.post(new Burst(entity, type, source));
    }
}