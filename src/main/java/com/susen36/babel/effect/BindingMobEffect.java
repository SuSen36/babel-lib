package com.susen36.babel.effect;

import com.susen36.babel.init.BabelParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

public class BindingMobEffect extends MobEffect {
    public BindingMobEffect() {
        super(MobEffectCategory.HARMFUL, 7890778);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "5f9a3c7e-1d2b-4c8a-9e0f-000000000021", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000022", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, "5f9a3c7e-1d2b-4c8a-9e0f-000000000023", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(ForgeMod.STEP_HEIGHT_ADDITION.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000024", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(BabelParticles.COBWEB.get(),
                    entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.5,
                    entity.getZ(),
                    2, 0.2, 0.3, 0.2, 0.0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}