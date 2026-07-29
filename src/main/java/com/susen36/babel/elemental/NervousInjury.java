package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class NervousInjury extends AbstractEPCapability {
    public NervousInjury(LivingEntity livingEntity) {
        super(EPType.NERVOUS, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        livingEntity.hurt(livingEntity.level().damageSources().drown(), 12);
        giveDizzyBuff();
    }

    @Override
    public void doNonPlayerBurst() {
        float baseDamage = 12;
        float damage = (float) Mth.clamp(livingEntity.getMaxHealth() * 0.4, baseDamage, baseDamage * 6);
        livingEntity.hurt(livingEntity.level().damageSources().drown(), damage);
        giveDizzyBuff();
        giveParalysis();
    }

    public void giveDizzyBuff() {
        livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
    }

    public void giveParalysis() {
    }

    @Override
    public void burstTick() {
    }
}