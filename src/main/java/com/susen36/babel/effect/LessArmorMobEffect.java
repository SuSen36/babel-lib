package com.susen36.babel.effect;

import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LessArmorMobEffect extends MobEffect {
    private static final int DEFAULT_DURATION_TICKS = 300;

    public LessArmorMobEffect() {
        super(MobEffectCategory.HARMFUL, -10079233);
        this.addAttributeModifier(Attributes.ARMOR, "5f9a3c7e-1d2b-4c8a-9e0f-000000000014", -1, AttributeModifier.Operation.ADDITION);
    }

    public static void apply(LivingEntity entity) {
        apply(entity, 1, DEFAULT_DURATION_TICKS);
    }

    public static void apply(LivingEntity entity, int level, int durationTicks) {
        if (!entity.level().isClientSide) {
            MobEffectInstance effect = entity.getEffect(BabelMobEffects.LESS_ARMOR.get());
            int amplifier = effect == null ? 0 : effect.getAmplifier() + level;
            entity.addEffect(new MobEffectInstance(BabelMobEffects.LESS_ARMOR.get(), durationTicks, amplifier, false, true, true));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
