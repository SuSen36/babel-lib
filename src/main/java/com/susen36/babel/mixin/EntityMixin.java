package com.susen36.babel.mixin;

import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void sleepingIsInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity living
                && living.hasEffect(BabelMobEffects.SLEEP.get())
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && !source.isCreativePlayer()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isInvulnerable", at = @At("HEAD"), cancellable = true)
    private void sleepingIsInvulnerable(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity living && living.hasEffect(BabelMobEffects.SLEEP.get())) {
            cir.setReturnValue(true);
        }
    }
}