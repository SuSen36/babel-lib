package com.susen36.babel.init;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod.EventBusSubscriber(modid = BabelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BabelAttributes {
    public static final DeferredRegister<Attribute> REGISTER = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, BabelMod.MODID);

    public static final RegistryObject<Attribute> MAX_ELEMENTAL_VALUE = simpleRangedAttr("elemental_max_value", 0.0, Double.MAX_VALUE, 50.0);
    public static final RegistryObject<Attribute> ELEMENTAL_RESISTANCE = simpleRangedAttr("elemental_resistance", 0.0, 100.0, 0.0);
    public static final RegistryObject<Attribute> DEFENSE = simpleRangedAttr("defense", 0.0, Double.MAX_VALUE, 0.0);
    public static final RegistryObject<Attribute> MAGIC_RESISTANCE = simpleRangedAttr("magic_resistance", 0.0, 100.0, 0.0);
    public static final RegistryObject<Attribute> ELEMENTAL_MODIFIER = REGISTER.register("elemental_modifier", () -> new RangedAttribute(descrId("elemental_modifier"), 1, 0, 999).setSyncable(true));
    public static final RegistryObject<Attribute> MAGIC_MISS_RATE = simpleRangedAttr("magic_missrate", 0.0, 95.0, 0.0);
    public static final RegistryObject<Attribute> PHYSICAL_MISS_RATE = simpleRangedAttr("physical_missrate", 0.0, 95.0, 0.0);
    public static final RegistryObject<Attribute> BARRIER = simpleRangedAttr("barrier", 0.0, 214748364.0, 0.0);
    public static final RegistryObject<Attribute> MAX_LIFE_POINT = simpleRangedAttr("max_life_point", 1.0, 214748364.0, BabelConfig.LIFE_POINT_INIT.getDefault());

    private static final List<RegistryObject<Attribute>> LIVING_ATTRIBUTES = List.of(
            MAX_ELEMENTAL_VALUE,
            ELEMENTAL_RESISTANCE,
            DEFENSE,
            MAGIC_RESISTANCE,
            ELEMENTAL_MODIFIER,
            MAGIC_MISS_RATE,
            PHYSICAL_MISS_RATE,
            BARRIER,
            MAX_LIFE_POINT
    );

    public static RegistryObject<Attribute> simpleRangedAttr(String name, double min, double max, double def) {
        return REGISTER.register(name, () -> new RangedAttribute(descrId(name), def, min, max).setSyncable(true));
    }

    public static String descrId(String name) {
        return "attribute." + BabelMod.MODID + "." + name;
    }

    public static float getMaxElementalValue(LivingEntity living) {
        AttributeInstance instance = living.getAttribute(MAX_ELEMENTAL_VALUE.get());
        return instance == null ? 50.0F : (float) instance.getValue();
    }

    @SubscribeEvent
    public static void registerLivingAttributes(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            for (RegistryObject<Attribute> attr : LIVING_ATTRIBUTES) {
                event.add(entityType, attr.get());
            }
        }
    }
}