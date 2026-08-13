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

import java.util.List;

@EventBusSubscriber(modid = BabelMod.MODID)
public class BabelAttributes {
    public static final DeferredRegister<Attribute> REGISTER = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, BabelMod.MODID);

    public static final DeferredHolder<Attribute, Attribute> MAX_ELEMENTAL_VALUE = simpleRangedAttr("elemental_max_value", 0.0, Double.MAX_VALUE, 1000.0);
    public static final DeferredHolder<Attribute, Attribute> ELEMENTAL_RESISTANCE = simpleRangedAttr("elemental_resistance", 0.0, 100.0, 0.0);
    public static final DeferredHolder<Attribute, Attribute> DEFENSE = simpleRangedAttr("defense", 0.0, Double.MAX_VALUE, 0.0);
    public static final DeferredHolder<Attribute, Attribute> MAGIC_RESISTANCE = simpleRangedAttr("magic_resistance", 0.0, 100.0, 0.0);
    public static final DeferredHolder<Attribute, Attribute> ELEMENTAL_MODIFIER = REGISTER.register("elemental_modifier", () -> new RangedAttribute(descrId("elemental_modifier"), 1, 0, 999).setSyncable(true));

    private static final List<DeferredHolder<Attribute, Attribute>> LIVING_ATTRIBUTES = List.of(
            MAX_ELEMENTAL_VALUE,
            ELEMENTAL_RESISTANCE,
            DEFENSE,
            MAGIC_RESISTANCE,
            ELEMENTAL_MODIFIER
    );

    public static DeferredHolder<Attribute, Attribute> simpleRangedAttr(String name, double min, double max, double def) {
        return REGISTER.register(name, () -> new RangedAttribute(descrId(name), def, min, max).setSyncable(true));
    }

    public static String descrId(String name) {
        return "attribute." + BabelMod.MODID + "." + name;
    }

    public static float getMaxElementalValue(LivingEntity living) {
        AttributeInstance instance = living.getAttribute(MAX_ELEMENTAL_VALUE);
        return instance == null ? 1000.0F : (float) instance.getValue();
    }

    @SubscribeEvent
    public static void registerLivingAttributes(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (!LivingEntity.class.isAssignableFrom(entityType.getBaseClass())) continue;
            for (DeferredHolder<Attribute, Attribute> attr : LIVING_ATTRIBUTES) {
                event.add(entityType, attr);
            }
        }
    }
}