package com.susen36.babel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.susen36.babel.client.renderer.BarrierRenderer;
import com.susen36.babel.client.renderer.FrozenIceRenderer;
import com.susen36.babel.effect.FrozenMobEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 注入基类 EntityRenderer.render，原版活体渲染器经 LivingEntityRenderer.render 尾部的 super.render 调用到此处，
// GeckoLib 等第三方渲染器同样经由该基类方法被覆盖。
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderFrozenIce(Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity instanceof LivingEntity living && !living.isSpectator() && FrozenMobEffect.isBabelFrozen(living)) {
            FrozenIceRenderer.render(living, poseStack, buffer, packedLight);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderBarrier(Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity instanceof LivingEntity living && !living.isSpectator()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (entity != minecraft.getCameraEntity() || !minecraft.options.getCameraType().isFirstPerson()) {
                BarrierRenderer.render(living, partialTicks, poseStack, buffer);
            }
        }
    }
}