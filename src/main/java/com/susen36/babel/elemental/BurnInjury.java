package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.world.entity.LivingEntity;

public class BurnInjury extends AbstractEPCapability {
    public BurnInjury(LivingEntity livingEntity) {
        super(EPType.BURN, livingEntity);
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