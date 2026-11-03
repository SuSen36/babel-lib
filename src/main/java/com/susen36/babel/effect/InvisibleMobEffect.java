package com.susen36.babel.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * 倍数作用于 {@code LivingEntity#getVisibilityPercent}，与之叠加而非覆盖，因此仍保留原版隐身、潜行、同类头颅的额外折减。
 */
public class InvisibleMobEffect extends MobEffect {

    public static final double VISIBILITY_FACTOR = 0.4D;
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("5f9a3c7e-1d2b-4c8a-9e0f-000000000025");

    public InvisibleMobEffect() {
        super(MobEffectCategory.HARMFUL, 8156784);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID.toString(), 0, AttributeModifier.Operation.MULTIPLY_BASE);// 药水效果不会同步到跟踪客户端，改用固定UUID的速度修饰符判断
    }

    public static boolean isBabelInvisible(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(SPEED_MODIFIER_UUID) != null;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY() + 0.1D, entity.getZ(), 2, entity.getBbWidth() * 0.5D, 0.05D, entity.getBbWidth() * 0.5D, 0.01D);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 4 == 0;
    }
}