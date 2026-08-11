package com.susen36.babel.mixin;

import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private double feeblenessUseTickRemainder;

    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void updateUsingItemWithFeebleness(ItemStack itemStack, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.hasEffect(BabelMobEffects.FEEBLENESS)) {
            feeblenessUseTickRemainder = 0.0D;
            return;
        }
        if (this.useItemRemaining == itemStack.getUseDuration(entity)) {
            feeblenessUseTickRemainder = 0.0D;
        }
        itemStack.onUseTick(entity.level(), entity, this.getUseItemRemainingTicks());
        MobEffectInstance feebleness = entity.getEffect(BabelMobEffects.FEEBLENESS);
        int amplifier = feebleness == null ? 0 : feebleness.getAmplifier();
        double useRate = switch (amplifier) {
            case 0 -> 0.8D;
            case 1 -> 0.6D;
            default -> 0.4D;
        };
        feeblenessUseTickRemainder += useRate;
        int ticksToRemove = Mth.floor(feeblenessUseTickRemainder);
        feeblenessUseTickRemainder -= ticksToRemove;
        this.useItemRemaining -= ticksToRemove;
        if (this.useItemRemaining <= 0) {
            feeblenessUseTickRemainder = 0.0D;
            if (!entity.level().isClientSide() && !itemStack.useOnRelease()) {
                this.completeUsingItem();
            }
        }
        ci.cancel();
    }

    @Shadow
    protected int useItemRemaining;

    @Shadow
    protected abstract void completeUsingItem();

    @Shadow
    public abstract int getUseItemRemainingTicks();
}