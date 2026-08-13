package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class BurnInjury extends AbstractEPCapability {
    private static final ResourceLocation MAGIC_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("babel", "burn_magic_resistance");

    public BurnInjury(LivingEntity livingEntity) {
        super(EPType.BURN, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        addMagicResistancePenalty();
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), DamageTypes.ON_FIRE);
        livingEntity.igniteForTicks(60);
        livingEntity.hurt(source, 11.0F);
    }

    @Override
    public void doNonPlayerBurst() {
        addMagicResistancePenalty();
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.ELEMENT_BREAK);
        livingEntity.hurt(source, 36.0F);
    }

    @Override
    public void doPlayerBurstTick() {
        if (reviveTick <= 0) {
            AttributeInstance magicRes = livingEntity.getAttribute(BabelAttributes.MAGIC_RESISTANCE);
            if (magicRes != null) {
                magicRes.removeModifier(MAGIC_RESISTANCE_ID);
            }
        }
    }

    @Override
    public void doNonPlayerBurstTick() {
        if (reviveTick <= 0) {
            AttributeInstance magicRes = livingEntity.getAttribute(BabelAttributes.MAGIC_RESISTANCE);
            if (magicRes != null) {
                magicRes.removeModifier(MAGIC_RESISTANCE_ID);
            }
        }
    }

    private void addMagicResistancePenalty() {
        AttributeInstance magicRes = livingEntity.getAttribute(BabelAttributes.MAGIC_RESISTANCE);
        if (magicRes != null) {
            magicRes.removeModifier(MAGIC_RESISTANCE_ID);
            magicRes.addTransientModifier(new AttributeModifier(MAGIC_RESISTANCE_ID, -20.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}