package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

// 狂躁损伤的爆条与减益实现。
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
            applyBurstDamage(frenzyStacks * 0.5F);
        }
    }

    @Override
    public void doNonPlayerBurstTick() {
        if (reviveTick <= 0) {
            removeAttackSpeedBonus();
        } else if (livingEntity.tickCount % 20 == 0) {
            applyBurstDamage(frenzyStacks * 0.75F);
        }
    }

    private void removeAttackSpeedBonus() {
        AttributeInstance attackSpeed = livingEntity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_BONUS.getId());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        if (underBurst()) {
            tag.putInt("frenzyStacks", frenzyStacks);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (underBurst() && tag.contains("frenzyStacks", Tag.TAG_INT)) {
            maxFrenzyStacks = livingEntity instanceof Player ? 10 : 8;
            frenzyStacks = Mth.clamp(tag.getInt("frenzyStacks"), 1, maxFrenzyStacks);
        }
    }

    public void addFrenzyStack() {
        frenzyStacks = Math.min(maxFrenzyStacks, frenzyStacks + 1);
    }
}