package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.client.particle.StunParticle;
import com.susen36.babel.client.particle.PalsyingParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BabelParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, BabelMod.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STUN = REGISTRY.register("stun", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PALSYING = REGISTRY.register("palsying", () -> new SimpleParticleType(false));

    private BabelParticles() {
        throw new UnsupportedOperationException("Utility class");
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = BabelMod.MODID)
    public static class ParticleProviderRegistration {
        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(BabelParticles.STUN.get(), StunParticle::provider);
            event.registerSpriteSet(BabelParticles.PALSYING.get(), PalsyingParticle::provider);
        }
    }
}