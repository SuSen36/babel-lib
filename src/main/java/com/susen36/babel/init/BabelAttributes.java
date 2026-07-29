package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public class BabelAttributes {
    public static final DeferredRegister<Attribute> REGISTER = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, BabelMod.MODID);

    public static final DeferredHolder<Attribute, Attribute> IMPAIRMENT_THRESHOLD = simpleRangedAttr("elemental_threshold", 100.0, Double.MAX_VALUE, 1000.0);
    public static final DeferredHolder<Attribute, Attribute> IMPAIRMENT_RESISTANCE = simpleRangedAttr("elemental_resistance", 0.0, 100.0, 0.0);

    public static final DeferredHolder<Attribute, Attribute> NERVOUS_RATE = simpleRangedAttr("nervous_rate", 0.0, Double.MAX_VALUE, 0.0);
    public static final DeferredHolder<Attribute, Attribute> CORROSION_RATE = simpleRangedAttr("corrosion_rate", 0.0, Double.MAX_VALUE, 0.0);
    public static final DeferredHolder<Attribute, Attribute> BURN_RATE = simpleRangedAttr("burn_rate", 0.0, Double.MAX_VALUE, 0.0);
    public static final DeferredHolder<Attribute, Attribute> NECROSIS_RATE = simpleRangedAttr("necrosis_rate", 0.0, Double.MAX_VALUE, 0.0);

    public static final DeferredHolder<Attribute, Attribute> DEFENSE = simpleRangedAttr("defense", 0.0, Double.MAX_VALUE, 0.0);
    public static final DeferredHolder<Attribute, Attribute> MAGIC_RESISTANCE = simpleRangedAttr("magic_resistance", 0.0, 100.0, 0.0);

    public static DeferredHolder<Attribute, Attribute> simpleRangedAttr(String name, double min, double max, double def) {
        return REGISTER.register(name, () -> new RangedAttribute(descrId(name), def, min, max).setSyncable(true));
    }

    public static String descrId(String name) {
        return "attribute." + BabelMod.MODID + "." + name;
    }

    public static float getImpairmentThreshold(LivingEntity living) {
        AttributeInstance instance = living.getAttribute(IMPAIRMENT_THRESHOLD);
        if (instance == null) return -1.0F;
        return (float) instance.getValue();
    }

    @SubscribeEvent
    public static void registerLivingAttributes(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (!LivingEntity.class.isAssignableFrom(entityType.getBaseClass())) continue;
            event.add(entityType, IMPAIRMENT_THRESHOLD);
            event.add(entityType, IMPAIRMENT_RESISTANCE);
            event.add(entityType, DEFENSE);
            event.add(entityType, MAGIC_RESISTANCE);
            event.add(entityType, NERVOUS_RATE);
            event.add(entityType, CORROSION_RATE);
            event.add(entityType, BURN_RATE);
            event.add(entityType, NECROSIS_RATE);
        }
    }

    @EventBusSubscriber(modid = BabelMod.MODID)
    public static class PlayerAttributesSync {
        public static void syncBaseValue(Player before, Player after, Holder<Attribute> attribute) {
            AttributeInstance beforeAttr = before.getAttribute(attribute);
            AttributeInstance afterAttr = after.getAttribute(attribute);
            if (beforeAttr != null && afterAttr != null) {
                afterAttr.setBaseValue(beforeAttr.getBaseValue());
            }
        }

        @SubscribeEvent
        public static void playerClone(PlayerEvent.Clone event) {
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();
            syncBaseValue(oldPlayer, newPlayer, IMPAIRMENT_THRESHOLD);
            syncBaseValue(oldPlayer, newPlayer, IMPAIRMENT_RESISTANCE);
            syncBaseValue(oldPlayer, newPlayer, DEFENSE);
            syncBaseValue(oldPlayer, newPlayer, MAGIC_RESISTANCE);
            syncBaseValue(oldPlayer, newPlayer, NERVOUS_RATE);
            syncBaseValue(oldPlayer, newPlayer, CORROSION_RATE);
            syncBaseValue(oldPlayer, newPlayer, BURN_RATE);
            syncBaseValue(oldPlayer, newPlayer, NECROSIS_RATE);
        }
    }
}