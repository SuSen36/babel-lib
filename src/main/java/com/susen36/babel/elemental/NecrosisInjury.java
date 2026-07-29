package com.susen36.babel.elemental;

import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.world.entity.LivingEntity;

public class NecrosisInjury extends AbstractEPCapability {
    public NecrosisInjury(LivingEntity livingEntity) {
        super(EPType.NECROSIS, livingEntity);
        this.maxReviveTick = 300;
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