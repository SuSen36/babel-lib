package com.susen36.babel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.susen36.babel.client.renderer.FrozenIceRenderer;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 冰壳不依赖实体模型，故挂在 {@link EntityRenderer#render} 上，可同时覆盖原版活体渲染器与 GeckoLib 等第三方渲染器。
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderFrozenIce(Entity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity instanceof LivingEntity living && !living.isSpectator() && living.hasEffect(BabelMobEffects.FROZEN.get())) {
            FrozenIceRenderer.render(living, poseStack, buffer, packedLight);
        }
    }
}