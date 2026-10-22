package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelDamageTypes;
import com.susen36.babel.effect.LessArmorMobEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

public class CorrosionInjury extends AbstractEPCapability {

    public CorrosionInjury(LivingEntity livingEntity) {
        super(EPType.CORROSION, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        LessArmorMobEffect.apply(livingEntity, 1, 1800);
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), DamageTypes.DROWN);
        livingEntity.clearFire();
        livingEntity.hurt(source, 9.0F);
    }

    @Override
    public void doNonPlayerBurst() {
        setMaxReviveTick(160);
        LessArmorMobEffect.apply(livingEntity, 2, 1800);
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.ELEMENT_BREAK);
        livingEntity.hurt(source, (float) 21);
    }

    @Override
    public void doPlayerBurstTick() {
    }

    @Override
    public void doNonPlayerBurstTick() {
    }
}