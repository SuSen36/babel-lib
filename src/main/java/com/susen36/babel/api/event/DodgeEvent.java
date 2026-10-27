package com.susen36.babel.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;

public class DodgeEvent extends LivingEvent {
    private final DamageSource source;

    public DodgeEvent(LivingEntity entity, DamageSource source) {
        super(entity);
        this.source = source;
    }

    public DamageSource getSource() {
        return source;
    }

    public static void postDodge(LivingEntity entity, DamageSource source) {
        MinecraftForge.EVENT_BUS.post(new DodgeEvent(entity, source));
    }
}