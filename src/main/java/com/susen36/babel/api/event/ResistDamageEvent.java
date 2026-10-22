package com.susen36.babel.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;

public class ResistDamageEvent extends LivingEvent {
    private final DamageSource source;
    private final float amount;
    private double attrValue;

    public ResistDamageEvent(LivingEntity entity, DamageSource source, float amount, double value) {
        super(entity);
        this.source = source;
        this.amount = amount;
        this.attrValue = value;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getAmount() {
        return amount;
    }

    public double getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(double v) {
        this.attrValue = v;
    }

    public static class Defense extends ResistDamageEvent {
        public Defense(LivingEntity entity, DamageSource source, float amount, double value) {
            super(entity, source, amount, value);
        }
    }

    public static class MagicResistance extends ResistDamageEvent {
        public MagicResistance(LivingEntity entity, DamageSource source, float amount, double value) {
            super(entity, source, amount, value);
        }
    }

    public static double modifyEffectiveDefenseValue(LivingEntity entity, DamageSource source, float amount, double value) {
        Defense event = new Defense(entity, source, amount, value);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAttrValue();
    }

    public static double modifyEffectiveResistanceValue(LivingEntity entity, DamageSource source, float amount, double value) {
        MagicResistance event = new MagicResistance(entity, source, amount, value);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAttrValue();
    }
}