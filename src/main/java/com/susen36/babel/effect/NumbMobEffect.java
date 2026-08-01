package com.susen36.babel.effect;

import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.init.BabelParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class NumbMobEffect extends MobEffect {
    public NumbMobEffect() {
        super(MobEffectCategory.HARMFUL, -8355712);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    public static void onAttackBlocked(LivingEntity attacker, LivingEntity target) {
        MobEffectInstance numbInstance = attacker.getEffect(BabelMobEffects.NUMB);
        if (numbInstance == null) return;
        int newAmplifier = numbInstance.getAmplifier() - 1;
        attacker.removeEffect(BabelMobEffects.NUMB);
        if (newAmplifier >= 0) {
            attacker.addEffect(new MobEffectInstance(BabelMobEffects.NUMB, numbInstance.getDuration(), newAmplifier,
                numbInstance.isAmbient(), numbInstance.isVisible(), numbInstance.showIcon()));
        }
        if (target.level() instanceof ServerLevel serverLevel)
            serverLevel.sendParticles(BabelParticles.NUMBNESS.get(), attacker.getX(), attacker.getY() + 1, attacker.getZ(), 12, 1, 1, 1, 0.1);
        target.level().playSound(null, target.blockPosition(), SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.HOSTILE, 2, 1);
    }
}