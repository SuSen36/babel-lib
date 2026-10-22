package com.susen36.babel.elemental;

import com.susen36.babel.BabelMod;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelDamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FrenzyInjury extends AbstractEPCapability {
    private static final String ATTACK_SPEED_NAME = BabelMod.MODID + ":frenzy_attack_speed";
    private static final UUID ATTACK_SPEED_ID = UUID.nameUUIDFromBytes(ATTACK_SPEED_NAME.getBytes(StandardCharsets.UTF_8));

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
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED_ID, ATTACK_SPEED_NAME, 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @Override
    public void doNonPlayerBurst() {
        frenzyStacks = 3;
        maxFrenzyStacks = 8;
        AttributeInstance attackSpeed = livingEntity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED_ID, ATTACK_SPEED_NAME, 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
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
            attackSpeed.removeModifier(ATTACK_SPEED_ID);
        }
    }

    public void addFrenzyStack() {
        frenzyStacks = Math.min(maxFrenzyStacks, frenzyStacks + 1);
    }
}
