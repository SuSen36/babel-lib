package com.susen36.babel.capability;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.capability.health.HealthCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class BabelCapability {

    public static final Capability<EPCapability> EP_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<HealthCapability> HEALTH_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final ResourceLocation EP_CAP_ID = new ResourceLocation(BabelMod.MODID, "ep_cap");
    public static final ResourceLocation HEALTH_CAP_ID = new ResourceLocation(BabelMod.MODID, "health_cap");

    private BabelCapability() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(BabelCapability::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(EPCapability.class);
        event.register(HealthCapability.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living) {
            CapabilityProvider provider = new CapabilityProvider(living);
            event.addCapability(EP_CAP_ID, provider);
            event.addCapability(HEALTH_CAP_ID, provider);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static EPCapability getEP(LivingEntity living) {
        return living.getCapability(EP_CAP).orElse(null);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static HealthCapability getHealth(Entity entity) {
        return entity.getCapability(HEALTH_CAP).orElse(null);
    }

    public static class CapabilityProvider implements ICapabilitySerializable<CompoundTag> {
        private final LazyOptional<EPCapability> epCap;
        private final LazyOptional<HealthCapability> healthCap;

        public CapabilityProvider(LivingEntity living) {
            this.epCap = LazyOptional.of(() -> new EPCapability(living));
            this.healthCap = LazyOptional.of(() -> new HealthCapability(living));
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == EP_CAP) {
                return epCap.cast();
            }
            if (cap == HEALTH_CAP) {
                return healthCap.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            epCap.ifPresent(ep -> tag.put("ep", ep.serializeNBT()));
            healthCap.ifPresent(health -> tag.put("health", health.serializeNBT()));
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            epCap.ifPresent(ep -> {
                if (tag.contains("ep")) {
                    ep.deserializeNBT(tag.getCompound("ep"));
                }
            });
            healthCap.ifPresent(health -> {
                if (tag.contains("health")) {
                    health.deserializeNBT(tag.getCompound("health"));
                }
            });
        }
    }
}
