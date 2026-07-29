package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.world.entity.LivingEntity;

public class CorrosionInjury extends AbstractEPCapability {
    public CorrosionInjury(LivingEntity livingEntity) {
        super(EPType.CORROSION, livingEntity);
    }

    @Override
    public void doPlayerBurst() {
    }

    @Override
    public void doNonPlayerBurst() {
    }

    @Override
    public void burstTick() {
    }
}