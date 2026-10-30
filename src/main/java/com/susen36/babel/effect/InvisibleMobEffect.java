package com.susen36.babel.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 倍数作用于 {@code LivingEntity#getVisibilityPercent}，与之叠加而非覆盖，因此仍保留原版隐身、潜行、同类头颅的额外折减。
 */
public class InvisibleMobEffect extends MobEffect {

    public static final double VISIBILITY_FACTOR = 0.4D;

    public InvisibleMobEffect() {
        super(MobEffectCategory.HARMFUL, 8156784);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "5f9a3c7e-1d2b-4c8a-9e0f-000000000025", 0, AttributeModifier.Operation.MULTIPLY_BASE);
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
