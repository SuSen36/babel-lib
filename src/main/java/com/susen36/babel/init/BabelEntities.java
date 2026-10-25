package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.entity.CannotEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BabelMod.MODID);

    public static final RegistryObject<EntityType<CannotEntity>> CANNOT = REGISTRY.register("cannot",
            () -> EntityType.Builder.of(CannotEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 2.1F)
                    .clientTrackingRange(10)
                    .build("babel:cannot"));

    private BabelEntities() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = BabelMod.MODID)
    public static class AttributeRegistration {
        @SubscribeEvent
        public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
            event.put(BabelEntities.CANNOT.get(), CannotEntity.createAttributes().build());
        }
    }
}