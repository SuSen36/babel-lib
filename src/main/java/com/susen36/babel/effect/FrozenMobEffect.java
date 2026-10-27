package com.susen36.babel.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraftforge.common.ForgeMod;

public class FrozenMobEffect extends MobEffect {

    public FrozenMobEffect() {
        super(MobEffectCategory.HARMFUL, -3342337);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "5f9a3c7e-1d2b-4c8a-9e0f-00000000000b", -1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "5f9a3c7e-1d2b-4c8a-9e0f-00000000000c", -1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, "5f9a3c7e-1d2b-4c8a-9e0f-00000000000d", -1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, "5f9a3c7e-1d2b-4c8a-9e0f-00000000000e", -1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "5f9a3c7e-1d2b-4c8a-9e0f-00000000000f", 0.5, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(ForgeMod.STEP_HEIGHT_ADDITION.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000010", -1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Creeper creeper) {
            CompoundTag tag = new CompoundTag();
            entity.saveWithoutId(tag);
            tag.putBoolean("ignited", false);
            entity.load(tag);
            creeper.setSwellDir(0);
        } else if (entity instanceof Blaze) {
            entity.hurt(entity.level().damageSources().freeze(), 3);
        } else if (entity instanceof MagmaCube) {
            entity.hurt(entity.level().damageSources().freeze(), 1);
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