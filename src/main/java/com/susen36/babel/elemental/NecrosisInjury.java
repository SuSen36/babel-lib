package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

// 凋亡损伤的爆条与减益实现。
public class NecrosisInjury extends AbstractEPCapability {

    public NecrosisInjury(LivingEntity livingEntity) {
        super(EPType.NECROSIS, livingEntity);
        setMaxReviveTick(300);
    }

    @Override
    public void doPlayerBurst() {
        livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 0, false, false, true));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 0, false, false, true));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1, false, false, true));
    }

    @Override
    public void doNonPlayerBurst() {
        livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2, false, false, true));
    }

    @Override
    public void doPlayerBurstTick() {
        if (livingEntity.tickCount % 20 == 0) {
            applyBurstDamage(1.75F);
        }
    }

    @Override
    public void doNonPlayerBurstTick() {
        if (livingEntity.tickCount % 20 == 0) {
            applyBurstDamage(6.0F);
        }
    }
}