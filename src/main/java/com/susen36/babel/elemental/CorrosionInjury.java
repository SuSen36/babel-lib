package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelDamageTypes;
import com.susen36.babel.effect.LessArmorMobEffect;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class CorrosionInjury extends AbstractEPCapability {
    private static final float CORROSION_BREAK_BASE_DAMAGE = 10.0F;

    public CorrosionInjury(LivingEntity livingEntity) {
        super(EPType.CORROSION, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        LessArmorMobEffect.apply(livingEntity, 1, 1800);
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.CORROSION_BREAK);
        livingEntity.hurt(source, CORROSION_BREAK_BASE_DAMAGE);
    }

    @Override
    public void doNonPlayerBurst() {
        setMaxReviveTick(160);
        LessArmorMobEffect.apply(livingEntity, 2, 1800);
        float damage = Mth.clamp(livingEntity.getMaxHealth() * 0.67F, CORROSION_BREAK_BASE_DAMAGE, CORROSION_BREAK_BASE_DAMAGE * 6);
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.ELEMENT_BREAK);
        livingEntity.hurt(source, damage);
    }

    @Override
    public void burstTick() {
    }
}