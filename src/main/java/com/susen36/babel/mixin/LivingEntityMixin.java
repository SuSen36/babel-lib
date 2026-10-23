package com.susen36.babel.mixin;

import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.item.HandOfFlowingWaterItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
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

    @Inject(method = "startUsingItem", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/LivingEntity;useItemRemaining:I", opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER))
    private void resetUseTickRemainderOnStart(InteractionHand hand, CallbackInfo ci) {
        feeblenessUseTickRemainder = 0.0D;
    }

    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void updateUsingItemWithFeebleness(ItemStack itemStack, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && (player.hasEffect(BabelMobEffects.FEEBLENESS.get()) || HandOfFlowingWaterItem.isFlowing(player))) {
            itemStack.onUseTick(player.level(), player, this.getUseItemRemainingTicks());
            if (this.shouldTriggerItemUseEffects()) {
                this.triggerItemUseEffects(itemStack, 5);
            }
            if (this.useItemRemaining <= 0) {
                // stopUsingItem 会把 useItemRemaining 置 0，此时使用已被中断，不能按剩余量跨零判定完成
                feeblenessUseTickRemainder = 0.0D;
            } else {
                double useRate = Mth.clamp(player.getAttributeValue(Attributes.ATTACK_SPEED) / 4.0D, 0.1D, 8.0D);
                if (HandOfFlowingWaterItem.isFlowing(player)) {
                    useRate *= 1.5D;
                }
                feeblenessUseTickRemainder += useRate;
                int ticksToRemove = Mth.floor(feeblenessUseTickRemainder);
                feeblenessUseTickRemainder -= ticksToRemove;
                this.useItemRemaining -= ticksToRemove;
                if (this.useItemRemaining <= 0) {
                    this.useItemRemaining = 0;
                    feeblenessUseTickRemainder = 0.0D;
                    if (!player.level().isClientSide() && !itemStack.useOnRelease()) {
                        this.completeUsingItem();
                    }
                }
            }
            ci.cancel();
        } else {
            feeblenessUseTickRemainder = 0.0D;
        }
    }

    @Shadow
    protected int useItemRemaining;

    @Shadow
    protected abstract void completeUsingItem();

    @Shadow
    public abstract int getUseItemRemainingTicks();

    @Shadow
    protected abstract boolean shouldTriggerItemUseEffects();

    @Shadow
    protected abstract void triggerItemUseEffects(ItemStack stack, int count);
}