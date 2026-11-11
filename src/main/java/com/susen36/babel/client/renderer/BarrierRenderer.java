package com.susen36.babel.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelAttributes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BarrierRenderer {
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucentEmissive(
            new ResourceLocation(BabelMod.MODID, "textures/entity/barrier_white.png"));

    // Suppresses default constructor, ensuring non-instantiability.
    private BarrierRenderer() {
    }

    public static void render(LivingEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        AttributeInstance attribute = entity.getAttribute(BabelAttributes.BARRIER.get());
        double barrier = attribute == null ? 0.0 : attribute.getValue();
        float entityWidth = entity.getBbWidth();
        float entityHeight = entity.getBbHeight();
        if (barrier > 0.0 && entityWidth > 0.0F && entityHeight > 0.0F) {
            float time = entity.tickCount + partialTicks;
            float halfWidth = entityWidth * (1.25F + 0.125F * Mth.sin(Mth.DEG_TO_RAD * time * 4.5F)) * 0.5F;
            float height = entityHeight * 1.1F;
            float hurtProgress = entity.hurtTime <= 10 ? entity.hurtTime * 0.1F : 0.0F;
            float bottomAlpha = Mth.clamp(Mth.lerp(hurtProgress, 0.5F, 0.9F), 0.0F, 1.0F);
            float topAlpha = Mth.clamp(Mth.lerp(hurtProgress, 0.1F, 0.3F), 0.0F, 1.0F);
            if (halfWidth > 0.0F && height > 0.0F) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.25F));
                VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
                PoseStack.Pose pose = poseStack.last();
                float startX = -halfWidth;
                float startZ = -halfWidth;
                float endX = halfWidth;
                float endZ = -halfWidth;
                float normalX = 0.0F;
                float normalZ = -1.0F;
                for (int side = 0; side < 4; side++) {
                    for (int vertex = 0; vertex < 4; vertex++) {
                        boolean top = vertex >= 2;
                        boolean end = vertex == 1 || vertex == 2;
                        consumer.vertex(pose.pose(), end ? endX : startX, top ? height : 0.0F, end ? endZ : startZ)
                                .color(1.0F, 1.0F, 1.0F, top ? topAlpha : bottomAlpha)
                                .uv(end ? 1.0F : 0.0F, top ? 0.0F : 1.0F)
                                .overlayCoords(OverlayTexture.NO_OVERLAY)
                                .uv2(LightTexture.FULL_BRIGHT)
                                .normal(pose.normal(), normalX, 0.0F, normalZ)
                                .endVertex();
                    }
                    float previousStartX = startX;
                    float previousEndX = endX;
                    float previousNormalX = normalX;
                    startX = -startZ;
                    startZ = previousStartX;
                    endX = -endZ;
                    endZ = previousEndX;
                    normalX = -normalZ;
                    normalZ = previousNormalX;
                }
                for (int face = 0; face < 2; face++) {
                    boolean top = face == 1;
                    for (int vertex = 0; vertex < 4; vertex++) {
                        boolean end = vertex == 1 || vertex == 2;
                        consumer.vertex(pose.pose(), end ? endX : startX, top ? height : 0.0F, end ? endZ : startZ)
                                .color(1.0F, 1.0F, 1.0F, top ? topAlpha : bottomAlpha)
                                .uv(end ? 1.0F : 0.0F, end ? 1.0F : 0.0F)
                                .overlayCoords(OverlayTexture.NO_OVERLAY)
                                .uv2(LightTexture.FULL_BRIGHT)
                                .normal(pose.normal(), 0.0F, top ? 1.0F : -1.0F, 0.0F)
                                .endVertex();
                    }
                }
                poseStack.popPose();
            }
        }
    }
}