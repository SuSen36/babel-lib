package com.susen36.babel.mixin;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.api.elemental.ElementType;
import com.susen36.babel.api.entity.ElementalAttacker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.WitherSkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin implements ElementalAttacker {

    @Unique
    @Override
    public ElementType getElementalType() {
        return ElementType.NECROSIS;
    }

    @Unique
    @Override
    public double getElementalRate() {
        return 0.0D;
    }

    @Unique
    @Override
    public double getElementalInjuryDamage() {
        return 0.0D;
    }

    @Unique
    @Override
    public double getElementalInjuryDamage(Entity directEntity) {
        return BabelConfig.WITHER_ELEMENTAL_DAMAGE.get()
                && directEntity instanceof WitherSkull skull && skull.isDangerous() ? 10.0D : 5.0D;
    }
}