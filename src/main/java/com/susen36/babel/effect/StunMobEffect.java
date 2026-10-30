package com.susen36.babel.effect;

import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.init.BabelParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.common.ForgeMod;

public class StunMobEffect extends MobEffect {
    public StunMobEffect() {
        super(MobEffectCategory.HARMFUL, -3355444);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, "5f9a3c7e-1d2b-4c8a-9e0f-000000000003", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, "5f9a3c7e-1d2b-4c8a-9e0f-000000000004", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "5f9a3c7e-1d2b-4c8a-9e0f-000000000005", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000006", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(ForgeMod.BLOCK_REACH.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000007", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(ForgeMod.ENTITY_REACH.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000008", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(ForgeMod.STEP_HEIGHT_ADDITION.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000009", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Mob mob && !entity.hasEffect(BabelMobEffects.STUN.get()) && !entity.hasEffect(BabelMobEffects.SLEEP.get())) {
            mob.targetSelector.enableControlFlag(Goal.Flag.TARGET);
        }
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            if (!mob.noPhysics) {
                mob.setNoGravity(false);
            }
            Brain<?> brain = mob.getBrain();
            if (brain != null) {
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                brain.eraseMemory(MemoryModuleType.ANGRY_AT);
                brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            }
        }
        if (entity.level() instanceof ServerLevel level && entity.tickCount % 10 == 0) {
            level.sendParticles(BabelParticles.STUN.get(),
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.8,
                    entity.getZ(),
                    2, 0.3, 0.1, 0.3, 0.02);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}