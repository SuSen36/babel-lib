package com.susen36.babel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.susen36.babel.effect.InvisibleMobEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Unique
    private static final float INVISIBLE_BODY_ALPHA = 0.75F;

    // alpha 写死在 render() 的 renderToBuffer 调用处，ModifyArg 处理器拿不到方法参数，
    // 只能在 HEAD 捕获当前实体；实体渲染在主线程串行执行，每次 render 开头必然刷新该字段
    @Nullable
    @Unique
    private LivingEntity babelCurrentEntity;

    @Inject(method = "render", at = @At("HEAD"))
    private void babel$captureEntity(LivingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        this.babelCurrentEntity = entity;
    }

    // EntityModel.renderToBuffer 的 8 个参数（index 0-7）依次为：
    // 0 poseStack、1 buffer、2 packedLight、3 packedOverlay、4 red、5 green、6 blue、7 alpha
    @ModifyArg(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),
            index = 7)
    private float babel$forceBodyAlpha(float alpha) {
        boolean babelInvisible = this.babelCurrentEntity != null && InvisibleMobEffect.isBabelInvisible(this.babelCurrentEntity);
        return babelInvisible ? Math.min(alpha, INVISIBLE_BODY_ALPHA) : alpha;
    }

    @Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
    private void applyTranslucentRenderType(LivingEntity entity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (bodyVisible && InvisibleMobEffect.isBabelInvisible(entity)) {
            ResourceLocation texture = ((EntityRenderer<LivingEntity>) (Object) this).getTextureLocation(entity);
            cir.setReturnValue(RenderType.itemEntityTranslucentCull(texture));
        }
    }
}