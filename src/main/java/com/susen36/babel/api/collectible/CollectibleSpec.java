package com.susen36.babel.api.collectible;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

/** 注册参数。activation 为空时使用该稀有度的默认表现。 */
public record CollectibleSpec(int useTicks, boolean canAlwaysUse, Tier tier,
                              int minLevel, int maxLevel, int defaultLevel, @Nullable Activation activation) {
    public enum Tier { CURSED, NORMAL, RARE, ADVANCED }

    public record Activation(SoundEvent sound, float volume, float pitch, ParticleOptions particle,
                             int particleCount, double particleYOffset, double particleSpeed,
                             boolean showOverlay, boolean autoUseOnGain) {
    }
}