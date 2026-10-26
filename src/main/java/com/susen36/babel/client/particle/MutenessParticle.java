package com.susen36.babel.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class MutenessParticle extends TextureSheetParticle {
    protected MutenessParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);
        this.setSize(0.2f, 0.2f);
        this.quadSize *= 1.5f;
        this.lifetime = Math.max(1, 40 + (this.random.nextInt(16) - 8));
        this.gravity = -0.1f;
        this.hasPhysics = false;
        this.xd = vx * 0.5;
        this.yd = vy * 0.5;
        this.zd = vz * 0.5;
        this.pickSprite(spriteSet);
    }

    public static MutenessParticleProvider provider(SpriteSet spriteSet) {
        return new MutenessParticleProvider(spriteSet);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class MutenessParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public MutenessParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new MutenessParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
