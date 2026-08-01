package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class BurnInjury extends AbstractEPCapability {
    private static final float BURN_BREAK_BASE_DAMAGE = 14.0F;
    private static final ResourceLocation MAGIC_RESISTANCE_MOD_ID = ResourceLocation.fromNamespaceAndPath("babel", "burn_magic_resistance");

    public BurnInjury(LivingEntity livingEntity) {
        super(EPType.BURN, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        addMagicResistancePenalty();
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.BURN_BREAK);
        livingEntity.hurt(source, BURN_BREAK_BASE_DAMAGE);
    }

    @Override
    public void doNonPlayerBurst() {
        addMagicResistancePenalty();
        float damage = Mth.clamp(livingEntity.getMaxHealth() * 0.93F, BURN_BREAK_BASE_DAMAGE, BURN_BREAK_BASE_DAMAGE * 6);
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.ELEMENT_BREAK);
        livingEntity.hurt(source, damage);
    }

    @Override
    public void burstTick() {
        if (reviveTick <= 0) {
            AttributeInstance magicRes = livingEntity.getAttribute(BabelAttributes.MAGIC_RESISTANCE);
            if (magicRes != null) {
                magicRes.removeModifier(MAGIC_RESISTANCE_MOD_ID);
            }
        }
    }

    private void addMagicResistancePenalty() {
        AttributeInstance magicRes = livingEntity.getAttribute(BabelAttributes.MAGIC_RESISTANCE);
        if (magicRes != null) {
            magicRes.removeModifier(MAGIC_RESISTANCE_MOD_ID);
            magicRes.addTransientModifier(new AttributeModifier(MAGIC_RESISTANCE_MOD_ID, -20.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}