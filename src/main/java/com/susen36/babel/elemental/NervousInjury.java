package com.susen36.babel.elemental;

import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelDamageTypes;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class NervousInjury extends AbstractEPCapability {
    // TODO: migrate to BabelConfig if configurable damage is needed
    private static final float SANITY_BREAK_BASE_DAMAGE = 12.0F;

    public NervousInjury(LivingEntity livingEntity) {
        super(EPType.NERVOUS, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        DamageSource sanityBreakDamage = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.SANITY_BREAK);
        BabelCapability.getEP(livingEntity).setUnderBreak(200);
        livingEntity.addEffect(new MobEffectInstance(BabelMobEffects.STUN, 200, 0, false, false));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, true));
        livingEntity.hurt(sanityBreakDamage, SANITY_BREAK_BASE_DAMAGE);
        livingEntity.level().playSound(null,
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.ELDER_GUARDIAN_CURSE, livingEntity.getSoundSource(), 2.2f, 1);
    }

    @Override
    public void doNonPlayerBurst() {
        DamageSource sanityBreakDamage = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.SANITY_BREAK);
        BabelCapability.getEP(livingEntity).setUnderBreak(200);
        MobEffectInstance existingNumb = livingEntity.getEffect(BabelMobEffects.PALSY);
        int existingAmplifier = existingNumb != null ? existingNumb.getAmplifier() : -1;
        if (existingAmplifier < 2) {
            livingEntity.removeEffect(BabelMobEffects.PALSY);
            livingEntity.addEffect(new MobEffectInstance(BabelMobEffects.PALSY, Integer.MAX_VALUE, 2, false, false, true));
        }
        float damage = Mth.clamp(livingEntity.getMaxHealth() * 0.8F, SANITY_BREAK_BASE_DAMAGE, SANITY_BREAK_BASE_DAMAGE * 6);
        livingEntity.hurt(sanityBreakDamage, damage);
        livingEntity.level().playSound(null,
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.ELDER_GUARDIAN_CURSE, livingEntity.getSoundSource(), 2.2f, 1);
    }

    @Override
    public void burstTick() {
    }
}