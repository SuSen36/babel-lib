package com.susen36.babel.capability;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.capability.health.HealthCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class BabelCapability {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BabelMod.MODID);

    public static final Supplier<AttachmentType<EPCapability>> EP_CAP = ATTACHMENT_TYPES.register("ep_cap",
            () -> AttachmentType.serializable((IAttachmentHolder holder) ->
                    new EPCapability(holder instanceof LivingEntity living ? living : null)).build());

    public static final Supplier<AttachmentType<HealthCapability>> HEALTH_CAP = ATTACHMENT_TYPES.register("health_cap",
            () -> AttachmentType.serializable((IAttachmentHolder holder) ->
                    new HealthCapability(holder instanceof LivingEntity living ? living : null)).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    private BabelCapability() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static EPCapability getEP(LivingEntity living) {
        return living.getData(EP_CAP.get());
    }

    public static HealthCapability getHealth(Entity entity) {
        return entity.getData(HEALTH_CAP.get());
    }
}