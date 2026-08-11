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
 * 核心机制：
 * <ul>
 *   <li>每 tick 均触发一次效果 tick（{@link #shouldApplyEffectTickThisTick} 始终返回 {@code true}）</li>
 *   <li>当佩戴者的攻击被目标格挡时，通过 {@link #onAttackBlocked} 将效果等级 -1，
 *       同时播放粒子与音效以提供反馈。</li>
 * </ul>
 *
 * @see BabelMobEffects#NUMB
 * @see MobEffectInstance
 */
public class NumbMobEffect extends MobEffect {
    /**
     * 构造麻痹药水效果。
     * <p>
     * 注册为有害类别，颜色代码 {@code -8355712}（深灰色）。
     * 对应的 {@link MobEffectInstance} 推荐使用 10 分钟（12000 ticks）作为持续时间。
     */
    public NumbMobEffect() {
        super(MobEffectCategory.HARMFUL, -8355712);
    }

    /**
     * 决定是否在当前 tick 执行效果逻辑。
     * <p>
     * Numb 效果每 tick 都会被调度，以配合其他系统（如元素系统与战斗事件）
     * 在高频节奏下的实时判断。
     *
     * @param duration  当前效果剩余 tick 数
     * @param amplifier 效果等级（0 为 I 级，1 为 II 级，以此类推）
     * @return 始终返回 {@code true}，即每 tick 均触发 {@link #applyEffectTick}
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 当佩戴者的攻击被目标格挡时的回调处理。
     * <p>
     * 将佩戴者身上的 {@link BabelMobEffects#NUMB Numb} 效果等级降低 1 级：
     * <ol>
     *   <li>若等级降为 -1，则直接移除效果；</li>
     *   <li>若等级仍 ≥ 0，则保留原持续时间与显示标志重新注册为新等级的效果。</li>
     * </ol>
     * 同时在服务端向攻击者位置发射 12 个麻痹粒子，并播放格挡失败的交互音效，
     * 以在视觉与听觉上给予玩家明确反馈。
     *
     * @param attacker 发起攻击但被格挡的实体（效果佩戴者）
     * @param target   成功格挡攻击的目标实体（用于播放音效位置）
     */
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