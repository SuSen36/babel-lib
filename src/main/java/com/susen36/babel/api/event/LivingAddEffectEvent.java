package com.susen36.babel.api.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * 实体获得药水效果事件：由 {@code LivingEntity.addEffect} 触发，允许订阅方替换本次应用的效果实例
 * （如收藏品延长增益时长）。替换仅作用于本次调用，需整体替换实例而非修改传入对象，避免污染调用方持有的引用。
 */
public class LivingAddEffectEvent extends LivingEvent {

    private MobEffectInstance effect;

    public LivingAddEffectEvent(LivingEntity entity, MobEffectInstance effect) {
        super(entity);
        this.effect = effect;
    }

    public MobEffectInstance getEffect() {
        return effect;
    }

    public void setEffect(MobEffectInstance effect) {
        this.effect = effect;
    }

    /** 按倍率调整本次效果的时长，其余参数保持不变；倍率为 1 时不产生拷贝。整体替换实例是为了不污染调用方持有的引用。 */
    public void scaleDuration(float multiplier) {
        if (multiplier != 1.0F) {
            MobEffectInstance effect = this.effect;
            this.effect = new MobEffectInstance(effect.getEffect(), (int) (effect.getDuration() * multiplier),
                    effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
        }
    }

    public static MobEffectInstance onAddEffect(LivingEntity entity, MobEffectInstance effect) {
        LivingAddEffectEvent event = new LivingAddEffectEvent(entity, effect);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getEffect();
    }
}