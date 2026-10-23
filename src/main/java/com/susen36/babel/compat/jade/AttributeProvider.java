package com.susen36.babel.compat.jade;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
            double def = getElemental(living);
            double resistance = getResistance(living);
            double barrier = getBarrier(living);
            if (def > 0 || resistance > 0) {
                iTooltip.add(new AttributeElement(def, resistance, 0));
            }
            if (barrier > 0) {
                iTooltip.add(new AttributeElement(0, 0, barrier));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private double getElemental(LivingEntity living) {
        AttributeInstance instance = living.getAttribute(BabelAttributes.ELEMENTAL_RESISTANCE.get());
        if (instance != null) return instance.getValue();
        return 0;
    }

    private double getResistance(LivingEntity living) {
        AttributeInstance instance = living.getAttribute(BabelAttributes.MAGIC_RESISTANCE.get());
        if (instance != null) return instance.getValue();
        return 0;
    }

    private double getBarrier(LivingEntity living) {
        AttributeInstance instance = living.getAttribute(BabelAttributes.BARRIER.get());
        if (instance != null) return instance.getValue();
        return 0;
    }
}