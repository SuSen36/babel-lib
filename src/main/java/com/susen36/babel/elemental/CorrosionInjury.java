package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public class CorrosionInjury extends AbstractEPCapability {
    private static final float CORROSION_BREAK_BASE_DAMAGE = 10.0F;

    public CorrosionInjury(LivingEntity livingEntity) {
        super(EPType.CORROSION, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        AttributeInstance defense = livingEntity.getAttribute(BabelAttributes.DEFENSE);
        if (defense != null) {
            defense.setBaseValue(defense.getBaseValue() - 10.0);
        }
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.CORROSION_BREAK);
        livingEntity.hurt(source, CORROSION_BREAK_BASE_DAMAGE);
    }

    @Override
    public void doNonPlayerBurst() {
        setMaxReviveTick(160);
        AttributeInstance defense = livingEntity.getAttribute(BabelAttributes.DEFENSE);
        if (defense != null) {
            defense.setBaseValue(defense.getBaseValue() - 12.0);
        }
        float damage = Mth.clamp(livingEntity.getMaxHealth() * 0.67F, CORROSION_BREAK_BASE_DAMAGE, CORROSION_BREAK_BASE_DAMAGE * 6);
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.ELEMENT_BREAK);
        livingEntity.hurt(source, damage);
    }

    @Override
    public void burstTick() {
    }
}