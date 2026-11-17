package com.susen36.babel.compat.jade;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum AttributeProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = new ResourceLocation(BabelMod.MODID, "attribute_provider");

    @Override
    public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        Entity entity = entityAccessor.getEntity();
        if (entity instanceof LivingEntity living) {
            double elemental = getElemental(living);
            double resistance = getResistance(living);
            double physicalMiss = getAttribute(living, BabelAttributes.PHYSICAL_MISS_RATE.get());
            double magicMiss = getAttribute(living, BabelAttributes.MAGIC_MISS_RATE.get());
            int numb = getNumb(living);
            double barrier = getAttribute(living, BabelAttributes.BARRIER.get());
            double shield = getAttribute(living, BabelAttributes.SHIELD.get());
            if (elemental > 0 || resistance > 0 || physicalMiss > 0 || magicMiss > 0
                    || numb > 0 || barrier > 0 || shield > 0) {
                iTooltip.add(new AttributeElement(elemental, resistance, physicalMiss, magicMiss, numb, barrier, shield));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private double getElemental(LivingEntity living) {
        return getAttribute(living, BabelAttributes.ELEMENTAL_RESISTANCE.get());
    }

    private double getResistance(LivingEntity living) {
        return getAttribute(living, BabelAttributes.MAGIC_RESISTANCE.get());
    }

    private double getAttribute(LivingEntity living, Attribute attribute) {
        AttributeInstance instance = living.getAttribute(attribute);
        return instance != null ? instance.getValue() : 0;
    }

    private int getNumb(LivingEntity living) {
        MobEffectInstance instance = living.getEffect(BabelMobEffects.PALSY.get());
        return instance != null ? instance.getAmplifier() + 1 : 0;
    }
}