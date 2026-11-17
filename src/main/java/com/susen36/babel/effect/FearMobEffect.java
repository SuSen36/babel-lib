package com.susen36.babel.effect;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class FearMobEffect extends MobEffect {
    private static final String FLEE_DIR_X_KEY = BabelMod.MODID + ":fear_flee_x";
    private static final String FLEE_DIR_Z_KEY = BabelMod.MODID + ":fear_flee_z";
    private static final double FLEE_DISTANCE = 5.0;
    private static final double MOVE_SPEED = 1.0;

    public FearMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xE46FFF);
    }

    public static void apply(LivingEntity entity, int durationTicks) {
        if (!entity.level().isClientSide) {
            entity.addEffect(new MobEffectInstance(BabelMobEffects.FEAR.get(), durationTicks, 0, false, true, true));
        }
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Mob mob) {
            Vec3 delta = mob.getDeltaMovement();
            double dx = delta.x;
            double dz = delta.z;
            double dirX;
            double dirZ;
            double horizSqr = dx * dx + dz * dz;
            if (horizSqr > 1.0E-4) {
                double horiz = Math.sqrt(horizSqr);
                dirX = dx / horiz;
                dirZ = dz / horiz;
            } else {
                // 静止时以自身朝向的水平前方作为待取反的移动方向
                float radians = mob.getYRot() * ((float) Math.PI / 180F);
                dirX = -Mth.sin(radians);
                dirZ = Mth.cos(radians);
            }
            CompoundTag data = mob.getPersistentData();
            data.putDouble(FLEE_DIR_X_KEY, -dirX);
            data.putDouble(FLEE_DIR_Z_KEY, -dirZ);
            mob.goalSelector.disableControlFlag(Goal.Flag.MOVE);
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.getPersistentData().remove(FLEE_DIR_X_KEY);
            mob.getPersistentData().remove(FLEE_DIR_Z_KEY);
            if (!entity.hasEffect(BabelMobEffects.FEAR.get())) {
                mob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
            }
            mob.getNavigation().stop();
        }
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            CompoundTag data = mob.getPersistentData();
            if (data.contains(FLEE_DIR_X_KEY) && data.contains(FLEE_DIR_Z_KEY) && mob.getNavigation().isDone()) {
                double fleeX = mob.getX() + data.getDouble(FLEE_DIR_X_KEY) * FLEE_DISTANCE;
                double fleeZ = mob.getZ() + data.getDouble(FLEE_DIR_Z_KEY) * FLEE_DISTANCE;
                mob.getNavigation().moveTo(fleeX, mob.getY(), fleeZ, MOVE_SPEED);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}