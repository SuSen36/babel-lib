package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class NecrosisInjury extends AbstractEPCapability {
    private static final float NECROSIS_TICK_DAMAGE_PLAYER = 100.0F;
    private static final float NECROSIS_TICK_DAMAGE_NON_PLAYER = 800.0F;

    public NecrosisInjury(LivingEntity livingEntity) {
        super(EPType.NECROSIS, livingEntity);
        setMaxReviveTick(300);
    }

    @Override
    public void doPlayerBurst() {
        livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 1, false, false, true));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 2, false, false, true));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, false, true));
    }

    @Override
    public void doNonPlayerBurst() {
        livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 1, false, false, true));
        livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0, false, false, true));
    }

    @Override
    public void burstTick() {
        if (livingEntity.tickCount % 20 == 0) {
            float damage = livingEntity instanceof Player ? NECROSIS_TICK_DAMAGE_PLAYER : NECROSIS_TICK_DAMAGE_NON_PLAYER;
            DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.ELEMENT_BREAK);
            livingEntity.hurt(source, damage);
        }
    }
}