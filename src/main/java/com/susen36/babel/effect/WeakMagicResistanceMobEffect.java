package com.susen36.babel.effect;

import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class WeakMagicResistanceMobEffect extends MobEffect {
    private static final int DEFAULT_DURATION_TICKS = 300;

    public WeakMagicResistanceMobEffect() {
        super(MobEffectCategory.HARMFUL, -10079488);
        this.addAttributeModifier(BabelAttributes.MAGIC_RESISTANCE.get(), "5f9a3c7e-1d2b-4c8a-9e0f-000000000019", -1, AttributeModifier.Operation.ADDITION);
    }

    public static void apply(LivingEntity entity) {
        apply(entity, 1, DEFAULT_DURATION_TICKS);
    }

    public static void apply(LivingEntity entity, int level, int durationTicks) {
        if (!entity.level().isClientSide) {
            MobEffectInstance effect = entity.getEffect(BabelMobEffects.WEAK_MAGIC_RESISTANCE.get());
            int amplifier = effect == null ? 0 : effect.getAmplifier() + level;
            entity.addEffect(new MobEffectInstance(BabelMobEffects.WEAK_MAGIC_RESISTANCE.get(), durationTicks, amplifier, false, true, true));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}