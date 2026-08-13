package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FrenzyInjury extends AbstractEPCapability {
    private static final ResourceLocation ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath("babel", "frenzy_attack_speed");

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
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public void doNonPlayerBurst() {
        frenzyStacks = 3;
        maxFrenzyStacks = 8;
        AttributeInstance attackSpeed = livingEntity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public void doPlayerBurstTick() {
        if (reviveTick <= 0) {
            removeAttackSpeedBonus();
        } else if (livingEntity.tickCount % 20 == 0) {
            livingEntity.hurt(livingEntity.damageSources().starve(), frenzyStacks*0.5F);
        }
    }

    @Override
    public void doNonPlayerBurstTick() {
        if (reviveTick <= 0) {
            removeAttackSpeedBonus();
        } else if (livingEntity.tickCount % 20 == 0) {
            livingEntity.hurt(livingEntity.damageSources().starve(), frenzyStacks*0.75F);
        }
    }

    private void removeAttackSpeedBonus() {
        AttributeInstance attackSpeed = livingEntity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
        }
    }

    public void addFrenzyStack() {
        frenzyStacks = Math.min(maxFrenzyStacks, frenzyStacks + 1);
    }
}
