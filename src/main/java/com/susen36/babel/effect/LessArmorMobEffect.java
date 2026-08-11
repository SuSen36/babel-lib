package com.susen36.babel.effect;

import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LessArmorMobEffect extends MobEffect {
    private static final int DEFAULT_DURATION_TICKS = 300;

    public LessArmorMobEffect() {
        super(MobEffectCategory.HARMFUL, -10079233);
        this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath("babel", "less_armor_armor"), -1, AttributeModifier.Operation.ADD_VALUE);
    }

    public static void apply(LivingEntity entity) {
        apply(entity, 1, DEFAULT_DURATION_TICKS);
    }

    public static void apply(LivingEntity entity, int level, int durationTicks) {
        if (entity.level().isClientSide()) return;
        MobEffectInstance effect = entity.getEffect(BabelMobEffects.LESS_ARMOR);
        int amplifier = effect == null ? 0 : effect.getAmplifier() + level;
        entity.addEffect(new MobEffectInstance(BabelMobEffects.LESS_ARMOR, durationTicks, amplifier, false, true, true));
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}