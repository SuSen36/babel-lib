package com.susen36.babel.effect;

import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class FrozenMobEffect extends MobEffect {

    // 药水效果不会同步到跟踪客户端，改用固定UUID的速度修饰符判断
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("5f9a3c7e-1d2b-4c8a-9e0f-00000000000b");

    public FrozenMobEffect() {
        super(MobEffectCategory.HARMFUL, -3342337);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID.toString(), -1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "5f9a3c7e-1d2b-4c8a-9e0f-00000000000c", -1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, "5f9a3c7e-1d2b-4c8a-9e0f-00000000000d", -1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, "5f9a3c7e-1d2b-4c8a-9e0f-00000000000e", -1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "5f9a3c7e-1d2b-4c8a-9e0f-00000000000f", 0.5, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(ForgeMod.STEP_HEIGHT_ADDITION.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000010", -1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public static boolean isBabelFrozen(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(SPEED_MODIFIER_UUID) != null;
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
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            brain.eraseMemory(MemoryModuleType.ANGRY_AT);
            brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);

            if (entity instanceof Blaze) {
                entity.hurt(entity.level().damageSources().freeze(), 3);
            } else if (entity instanceof MagmaCube) {
                entity.hurt(entity.level().damageSources().freeze(), 1);
            }
        }
        if (entity.level() instanceof ServerLevel level) {
            double halfHeight = entity.getBbHeight() * 0.5;
            double halfWidth = entity.getBbWidth() * 0.5;
            level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY() + halfHeight, entity.getZ(), 8, halfWidth, halfHeight, halfWidth, 0.05);
            level.sendParticles(ParticleTypes.ITEM_SNOWBALL, entity.getX(), entity.getY() + halfHeight, entity.getZ(), 2, halfWidth, halfHeight, halfWidth, 0.05);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}