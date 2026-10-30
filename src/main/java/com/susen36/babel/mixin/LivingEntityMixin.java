package com.susen36.babel.mixin;

import com.susen36.babel.effect.InvisibleMobEffect;
import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.item.DreamingEssenceItem;
import com.susen36.babel.item.HandOfFlowingWaterItem;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        if (entity instanceof Player player && (player.hasEffect(BabelMobEffects.FEEBLENESS.get())
                || (HandOfFlowingWaterItem.isFlowing(player) && HandOfFlowingWaterItem.isBowOrCrossbow(itemStack)))) {
            itemStack.onUseTick(player.level(), player, this.getUseItemRemainingTicks());
            if (this.shouldTriggerItemUseEffects()) {
                this.triggerItemUseEffects(itemStack, 5);
            }
            // 满蓄力后冻结倒计时：渲染器拉弓插值假设每 tick 恰好 -1，变速递减会把满蓄力正弦颤动放大成抖动
            int fullChargeTicks = itemStack.getItem() instanceof CrossbowItem ? CrossbowItem.getChargeDuration(itemStack) : BowItem.MAX_DRAW_DURATION;
            if (this.useItemRemaining <= 0) {
                // stopUsingItem 会把 useItemRemaining 置 0，此时使用已被中断，不能按剩余量跨零判定完成
                feeblenessUseTickRemainder = 0.0D;
            } else if (!itemStack.useOnRelease() || this.useItemRemaining > itemStack.getUseDuration() - fullChargeTicks) {
                double useRate = Mth.clamp(player.getAttributeValue(Attributes.ATTACK_SPEED) / 4.0D, 0.1D, 8.0D);
                if (HandOfFlowingWaterItem.isFlowing(player)) {
                    useRate *= 1.0D + 0.25D * HandOfFlowingWaterItem.activeStacks(player);
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

    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), argsOnly = true, index = 1)
    private MobEffectInstance boostBeneficialEffectDuration(MobEffectInstance effect) {
        LivingEntity self = (LivingEntity) (Object) this;
        MobEffectInstance result = effect;
        // 返回副本而非原地修改，避免污染调用方持有的实例导致时长连乘
        if (self instanceof Player player
                && DreamingEssenceItem.isUsed(player)
                && effect.getEffect().getCategory() == MobEffectCategory.BENEFICIAL
                && effect.getDuration() > 0) {
            result = new MobEffectInstance(effect.getEffect(), (int) (effect.getDuration() * 1.5F),
                    effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
        }
        return result;
    }

    @Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
    private void reduceVisibilityWhenInvisible(Entity looker, CallbackInfoReturnable<Double> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        MobEffectInstance invisible = self.getEffect(BabelMobEffects.INVISIBLE.get());
        if (invisible != null) {
            float factor = (float) (InvisibleMobEffect.VISIBILITY_FACTOR / (1 << invisible.getAmplifier()));
            cir.setReturnValue(cir.getReturnValueD() * factor);
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