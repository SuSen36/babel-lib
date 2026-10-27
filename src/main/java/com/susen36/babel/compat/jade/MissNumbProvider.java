package com.susen36.babel.compat.jade;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum MissNumbProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = new ResourceLocation(BabelMod.MODID, "miss_numb_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Entity entity = accessor.getEntity();
        if (entity instanceof LivingEntity living) {
            int physical = (int) getPhysicalMiss(living);
            int magic = (int) getMagicMiss(living);
            int numb = (int) getNumb(living);
            if (physical > 0 || magic > 0 || numb > 0) {
                tooltip.add(new MissNumbElement(physical, magic, numb));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private double getPhysicalMiss(LivingEntity living) {
        AttributeInstance physical = living.getAttribute(BabelAttributes.PHYSICAL_MISS_RATE.get());
        return physical != null ? physical.getValue() : 0;
    }

    private double getMagicMiss(LivingEntity living) {
        AttributeInstance magic = living.getAttribute(BabelAttributes.MAGIC_MISS_RATE.get());
        return magic != null ? magic.getValue() : 0;
    }

    private double getNumb(LivingEntity living) {
        MobEffectInstance instance = living.getEffect(BabelMobEffects.PALSY.get());
        if (instance != null) return instance.getAmplifier() + 1;
        return 0;
    }
}