package com.susen36.babel.effect;

import com.susen36.babel.init.BabelParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

public class SilencedMobEffect extends MobEffect {

    public SilencedMobEffect() {
        super(MobEffectCategory.HARMFUL, -11904668);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(BabelParticles.MUTING.get(), entity.getX(), entity.getY(), entity.getZ(), 2, 1, 1, 1, 0.1);
        }
        //TODO mixin苦力怕更好
        if (entity instanceof Creeper creeper) {
            CompoundTag tag = new CompoundTag();
            entity.saveWithoutId(tag);
            tag.putBoolean("ignited", false);
            entity.load(tag);
            creeper.setSwellDir(0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}