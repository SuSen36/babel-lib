package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class FrenzyInjury extends AbstractEPCapability {
    private static final AttributeModifier ATTACK_SPEED_BONUS = new AttributeModifier(UUID.fromString("7d4e9a2b-3f6c-4b8e-9d1a-5c2e8f0b3a17"), "babel:frenzy_attack_speed", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);

    private int frenzyStacks = 2;
    private  int maxFrenzyStacks = 10;

    public FrenzyInjury(LivingEntity livingEntity) {
        super(EPType.FRENZY, livingEntity);
        setMaxReviveTick(300);
    }

    @Override
    public void doPlayerBurst() {
        frenzyStacks = 1;
        AttributeInstance attackSpeed = livingEntity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_BONUS.getId());
            attackSpeed.addTransientModifier(ATTACK_SPEED_BONUS);
        }
    }

    @Override
    public void doNonPlayerBurst() {
        frenzyStacks = 3;
        maxFrenzyStacks = 8;
        AttributeInstance attackSpeed = livingEntity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_BONUS.getId());
            attackSpeed.addTransientModifier(ATTACK_SPEED_BONUS);
        }
    }

    @Override
    public void doPlayerBurstTick() {
        if (reviveTick <= 0) {
            removeAttackSpeedBonus();
        } else if (livingEntity.tickCount % 20 == 0) {
            livingEntity.hurt(BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.FRENZY_BREAK), frenzyStacks * 0.5F);
        }
    }

    @Override
    public void doNonPlayerBurstTick() {
        if (reviveTick <= 0) {
            removeAttackSpeedBonus();
        } else if (livingEntity.tickCount % 20 == 0) {
            livingEntity.hurt(BabelDamageTypes.source(livingEntity.level(), BabelDamageTypes.FRENZY_BREAK), frenzyStacks * 0.75F);
        }
    }

    private void removeAttackSpeedBonus() {
        AttributeInstance attackSpeed = livingEntity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_BONUS.getId());
        }
    }

    public void addFrenzyStack() {
        frenzyStacks = Math.min(maxFrenzyStacks, frenzyStacks + 1);
    }
}