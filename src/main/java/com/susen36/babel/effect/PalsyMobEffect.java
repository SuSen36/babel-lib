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

/**
 * 麻痹（Numb）药水效果。
 * <p>
 * 属于 {@link MobEffectCategory#HARMFUL 有害} 类别，表现为深灰色粒子效果（颜色值 {@code -8355712}）。
 * <p>
 * <b>推荐持续时间：</b>10 分钟（{@code 10 * 60 * 20 = 12000} ticks）。
 * 在构造 {@link MobEffectInstance} 时建议以此值作为基准持续时间，以确保战斗流程中
 * 效果具有足够的策略深度与可操作性。
 * <p>
 * @see BabelMobEffects#PALSY
 * @see MobEffectInstance
 */
public class PalsyMobEffect extends MobEffect {

    public PalsyMobEffect() {
        super(MobEffectCategory.HARMFUL, -8355712);
    }


    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    public static void onAttackBlocked(LivingEntity attacker, LivingEntity target) {
        MobEffectInstance numbInstance = attacker.getEffect(BabelMobEffects.PALSY);
        if (numbInstance != null) {
            int newAmplifier = numbInstance.getAmplifier() - 1;
            attacker.removeEffect(BabelMobEffects.PALSY);
            if (newAmplifier >= 0) {
                attacker.addEffect(new MobEffectInstance(BabelMobEffects.PALSY, numbInstance.getDuration(), newAmplifier,
                        numbInstance.isAmbient(), numbInstance.isVisible(), numbInstance.showIcon()));
            }
            if (target.level() instanceof ServerLevel serverLevel)
                serverLevel.sendParticles(BabelParticles.PALSYING.get(), attacker.getX(), attacker.getY() + 1, attacker.getZ(), 12, 1, 1, 1, 0.1);
            target.level().playSound(null, target.blockPosition(), SoundEvents.WAXED_SIGN_INTERACT_FAIL, SoundSource.HOSTILE, 2, 1);
        }
    }
}