package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

// 神经损伤的爆条与减益实现。
public class NervousInjury extends AbstractEPCapability {
    public NervousInjury(LivingEntity livingEntity) {
        super(EPType.NERVOUS, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        livingEntity.addEffect(new MobEffectInstance(BabelMobEffects.STUN.get(), 200, 0, false, false));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, true));
        applyBurstDamage(12.0F);
        livingEntity.level().playSound(null,
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.ELDER_GUARDIAN_CURSE, livingEntity.getSoundSource(), 2.2f, 1);
    }

    @Override
    public void doNonPlayerBurst() {
        MobEffectInstance existingNumb = livingEntity.getEffect(BabelMobEffects.PALSY.get());
        int existingAmplifier = existingNumb != null ? existingNumb.getAmplifier() : -1;
        if (existingAmplifier < 2) {
            livingEntity.removeEffect(BabelMobEffects.PALSY.get());
            livingEntity.addEffect(new MobEffectInstance(BabelMobEffects.PALSY.get(), Integer.MAX_VALUE, 2, false, false, true));
        }
        applyBurstDamage(Mth.clamp(livingEntity.getMaxHealth() * 0.8F, 12.0F, 72.0F));
        livingEntity.level().playSound(null,
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.ELDER_GUARDIAN_CURSE, livingEntity.getSoundSource(), 2.2f, 1);
    }

    @Override
    public void doPlayerBurstTick() {
    }

    @Override
    public void doNonPlayerBurstTick() {
    }
}