package com.susen36.babel.mixin;

import com.susen36.babel.BabelConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.WitherSkull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherSkull.class)
public abstract class WitherSkullMixin {

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean skipWitherEffectWhenElemental(LivingEntity target, MobEffectInstance effect, Entity source) {
        WitherSkull skull = (WitherSkull) (Object) this;
        if (skull.isDangerous() || !BabelConfig.witherElementalDamage) {
            return target.addEffect(effect, source);
        }
        return false;
    }
}