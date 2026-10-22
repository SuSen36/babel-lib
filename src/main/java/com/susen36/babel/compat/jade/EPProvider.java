package com.susen36.babel.compat.jade;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EPProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID = new ResourceLocation(BabelMod.MODID, "ep_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getEntity() instanceof LivingEntity living) {
            float threshold = BabelAttributes.getMaxElementalValue(living);
            EPCapability ep = BabelCapability.getEP(living);
            if (threshold > 0 && ep != null) {
                AbstractEPCapability element = ep.getCurrentElement();
                if (element != null && element.getValue() > 0) {
                    tooltip.add(new EPElement(element.getType(), element.getValue(), threshold));
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}