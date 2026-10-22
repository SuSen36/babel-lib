package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.client.particle.LifelossParticle;
import com.susen36.babel.client.particle.ShieldlossParticle;
import com.susen36.babel.client.particle.StunParticle;
import com.susen36.babel.client.particle.MissParticle;
import com.susen36.babel.client.particle.MutenessParticle;
import com.susen36.babel.client.particle.PalsyingParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, BabelMod.MODID);

    public static final RegistryObject<SimpleParticleType> STUN = REGISTRY.register("stun", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> PALSYING = REGISTRY.register("palsying", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MUTENESS = REGISTRY.register("muteness", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MISS_PHYSICAL = REGISTRY.register("miss_physical", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MISS_MAGIC = REGISTRY.register("miss_magic", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LIFELOSS = REGISTRY.register("lifeloss", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SHIELDLOSS = REGISTRY.register("shieldloss", () -> new SimpleParticleType(false));

    private BabelParticles() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BabelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ParticleProviderRegistration {
        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(BabelParticles.STUN.get(), StunParticle::provider);
            event.registerSpriteSet(BabelParticles.PALSYING.get(), PalsyingParticle::provider);
            event.registerSpriteSet(BabelParticles.MUTENESS.get(), MutenessParticle::provider);
            event.registerSpriteSet(BabelParticles.MISS_PHYSICAL.get(), MissParticle::provider);
            event.registerSpriteSet(BabelParticles.MISS_MAGIC.get(), MissParticle::provider);
            event.registerSpriteSet(BabelParticles.LIFELOSS.get(), LifelossParticle::provider);
            event.registerSpriteSet(BabelParticles.SHIELDLOSS.get(), ShieldlossParticle::provider);
        }
    }
}
