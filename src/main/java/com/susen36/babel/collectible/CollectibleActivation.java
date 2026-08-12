package com.susen36.babel.collectible;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * 收藏品激活表现配置：收藏品成功激活时统一播放的声音/粒子/激活动画，以及是否在获得时自动使用。
 * <p>
 * 与 {@link CollectibleTiers} 配合：诅咒级（{@link CollectibleTiers#CURSED}）默认使用专属的
 * 声音与粒子，并开启 {@code autoUseOnGain}（获得时自动使用）；其余级别默认使用通用成功表现。
 * 具体收藏品可通过 {@link #builder()} 或按级别工厂覆盖任意项。
 */
public final class CollectibleActivation {

    private final SoundEvent soundEvent;
    private final float volume;
    private final float pitch;
    private final ParticleOptions particle;
    private final int particleCount;
    private final double particleYOffset;
    private final double particleSpeed;
    private final boolean showActivationOverlay;
    private final boolean autoUseOnGain;

    private CollectibleActivation(SoundEvent soundEvent, float volume, float pitch,
                                  ParticleOptions particle, int particleCount,
                                  double particleYOffset, double particleSpeed,
                                  boolean showActivationOverlay, boolean autoUseOnGain) {
        this.soundEvent = soundEvent;
        this.volume = volume;
        this.pitch = pitch;
        this.particle = particle;
        this.particleCount = particleCount;
        this.particleYOffset = particleYOffset;
        this.particleSpeed = particleSpeed;
        this.showActivationOverlay = showActivationOverlay;
        this.autoUseOnGain = autoUseOnGain;
    }

    public SoundEvent soundEvent() { return soundEvent; }
    public float volume() { return volume; }
    public float pitch() { return pitch; }
    public ParticleOptions particle() { return particle; }
    public int paticleCount() { return particleCount; }
    public double particleYOffset() { return particleYOffset; }
    public double particleSpeed() { return particleSpeed; }
    public boolean showActivationOverlay() { return showActivationOverlay; }
    public boolean autoUseOnGain() { return autoUseOnGain; }

    /** 按等级返回默认表现：诅咒级用专属声音/粒子并自动使用，其余用通用成功表现。 */
    public static CollectibleActivation forTier(CollectibleTiers tier) {
        if (tier == CollectibleTiers.CURSED) {
            return cursed();
        }
        return standard();
    }

    /** 通用成功表现：原版物品使用成功音效 + 幸福村民粒子 + 激活动画，不自动使用。 */
    public static CollectibleActivation standard() {
        return builder().build();
    }

    /** 诅咒级表现：灵魂沙谷氛围音效 + 绯红孢粒 + 激活动画，获得时自动使用。 */
    public static CollectibleActivation cursed() {
        return builder()
                .sound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), 2F, 1F)
                .particle(ParticleTypes.CRIMSON_SPORE, 99)
                .autoUseOnGain(true)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private SoundEvent soundEvent = SoundEvents.PLAYER_BURP;
        private float volume = 1.5F;
        private float pitch = 1F;
        private ParticleOptions particle = ParticleTypes.HAPPY_VILLAGER;
        private int particleCount = 72;
        private double particleYOffset = 0D;
        private double particleSpeed = 1D;
        private boolean showActivationOverlay = true;
        private boolean autoUseOnGain = false;

        public Builder sound(SoundEvent e, float v, float p) { this.soundEvent = e; this.volume = v; this.pitch = p; return this; }
        public Builder particle(ParticleOptions p, int count) { this.particle = p; this.particleCount = count; return this; }
        public Builder particleYOffset(double offset) { this.particleYOffset = offset; return this; }
        public Builder particleSpeed(double s) { this.particleSpeed = s; return this; }
        public Builder showOverlay(boolean b) { this.showActivationOverlay = b; return this; }
        public Builder autoUseOnGain(boolean b) { this.autoUseOnGain = b; return this; }

        public CollectibleActivation build() {
            return new CollectibleActivation(soundEvent, volume, pitch, particle,
                    particleCount, particleYOffset, particleSpeed, showActivationOverlay, autoUseOnGain);
        }
    }
}