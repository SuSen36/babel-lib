package com.susen36.babel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Unique
    private static final float INVISIBLE_BODY_ALPHA = 0.75F;

    @Unique
    @Nullable
    private LivingEntity babel$renderedEntity;

    @Inject(method = "render", at = @At("HEAD"))
    private void captureRenderedEntity(LivingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        babel$renderedEntity = entity;
    }

    @Redirect(method = "render*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void applyInvisibleAlpha(EntityModel<?> model, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float finalAlpha = alpha;
        if (babel$renderedEntity != null && isBabelInvisible(babel$renderedEntity) && alpha > INVISIBLE_BODY_ALPHA) {
            finalAlpha = INVISIBLE_BODY_ALPHA;
        }
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, finalAlpha);
    }

    @Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
    private void applyTranslucentRenderType(LivingEntity entity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (bodyVisible && isBabelInvisible(entity)) {
            ResourceLocation texture = ((EntityRenderer<LivingEntity>) (Object) this).getTextureLocation(entity);
            cir.setReturnValue(RenderType.itemEntityTranslucentCull(texture));
        }
    }

    // 药水效果不会同步到跟踪客户端，改用固定UUID的速度修饰符判断
    @Unique
    private static boolean isBabelInvisible(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(UUID.fromString("5f9a3c7e-1d2b-4c8a-9e0f-000000000025")) != null;
    }
}