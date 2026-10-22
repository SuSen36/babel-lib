package com.susen36.babel.elemental;

import com.susen36.babel.BabelMod;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BurnInjury extends AbstractEPCapability {
    private static final String MAGIC_RESISTANCE_NAME = BabelMod.MODID + ":burn_magic_resistance";
    private static final UUID MAGIC_RESISTANCE_ID = UUID.nameUUIDFromBytes(MAGIC_RESISTANCE_NAME.getBytes(StandardCharsets.UTF_8));

    public BurnInjury(LivingEntity livingEntity) {
        super(EPType.BURN, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
        addMagicResistancePenalty();
        DamageSource source = BabelDamageTypes.source(livingEntity.level(), DamageTypes.ON_FIRE);
        livingEntity.setSecondsOnFire(3);
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
            AttributeInstance magicRes = livingEntity.getAttribute(BabelAttributes.MAGIC_RESISTANCE.get());
            if (magicRes != null) {
                magicRes.removeModifier(MAGIC_RESISTANCE_ID);
            }
        }
    }

    @Override
    public void doNonPlayerBurstTick() {
        if (reviveTick <= 0) {
            AttributeInstance magicRes = livingEntity.getAttribute(BabelAttributes.MAGIC_RESISTANCE.get());
            if (magicRes != null) {
                magicRes.removeModifier(MAGIC_RESISTANCE_ID);
            }
        }
    }

    private void addMagicResistancePenalty() {
        AttributeInstance magicRes = livingEntity.getAttribute(BabelAttributes.MAGIC_RESISTANCE.get());
        if (magicRes != null) {
            magicRes.removeModifier(MAGIC_RESISTANCE_ID);
            magicRes.addTransientModifier(new AttributeModifier(MAGIC_RESISTANCE_ID, MAGIC_RESISTANCE_NAME, -20.0, AttributeModifier.Operation.ADDITION));
        }
    }
}