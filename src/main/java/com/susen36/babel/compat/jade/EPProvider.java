package com.susen36.babel.compat.jade;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.capability.ep.EPCapability;
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

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(BabelMod.MODID, "ep_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Entity entity = accessor.getEntity();
        if (entity instanceof LivingEntity living) {
            EPCapability ep = BabelCapability.getEP(living);
            float threshold = BabelAttributes.getImpairmentThreshold(living);
            if (threshold <= 0) return;
            ep.forEachEPType(cap -> {
                if (cap.getValue() <= 0) return;
                tooltip.add(new EPElement(cap.getType(), cap.getValue(), threshold));
            });
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}