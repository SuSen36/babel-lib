package com.susen36.babel.mixin;

import com.susen36.babel.effect.InvisibleMobEffect;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 半透明主体的 alpha 改由 InvisibleRenderHandler 换模型实现；
// 此处不注入 render 内部指令，避免与其他模组的 @Redirect 冲突
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Unique
    private static final float INVISIBLE_BODY_ALPHA = 0.75F;

    @Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
    private void applyTranslucentRenderType(LivingEntity entity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (bodyVisible && InvisibleMobEffect.isBabelInvisible(entity)) {
            ResourceLocation texture = ((EntityRenderer<LivingEntity>) (Object) this).getTextureLocation(entity);
            cir.setReturnValue(RenderType.itemEntityTranslucentCull(texture));
        }
    }
}
