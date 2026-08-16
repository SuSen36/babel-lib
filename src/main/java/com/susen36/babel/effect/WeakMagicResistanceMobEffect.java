package com.susen36.babel.effect;

import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class WeakMagicResistanceMobEffect extends MobEffect {
    private static final int DEFAULT_DURATION_TICKS = 300;

    public WeakMagicResistanceMobEffect() {
        super(MobEffectCategory.HARMFUL, -10079488);
        this.addAttributeModifier(BabelAttributes.MAGIC_RESISTANCE, ResourceLocation.fromNamespaceAndPath("babel", "weak_magic_resistance"), -1, AttributeModifier.Operation.ADD_VALUE);
    }

    public static void apply(LivingEntity entity) {
        apply(entity, 1, DEFAULT_DURATION_TICKS);
    }

    public static void apply(LivingEntity entity, int level, int durationTicks) {
        if (entity.level().isClientSide()) return;
        MobEffectInstance effect = entity.getEffect(BabelMobEffects.WEAK_MAGIC_RESISTANCE);
        int amplifier = effect == null ? 0 : effect.getAmplifier() + level;
        entity.addEffect(new MobEffectInstance(BabelMobEffects.WEAK_MAGIC_RESISTANCE, durationTicks, amplifier, false, true, true));
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}