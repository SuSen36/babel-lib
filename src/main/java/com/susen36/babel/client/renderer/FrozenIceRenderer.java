package com.susen36.babel.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;


@OnlyIn(Dist.CLIENT)
public final class FrozenIceRenderer {
    private static final BlockState ICE_BLOCK = Blocks.ICE.defaultBlockState();
    private static final float CUBE_SCALE = 0.5F;
    private static final float CUBE_HEIGHT = 0.4F;

    private FrozenIceRenderer() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        long id = entity.getId();
        RandomSource random = RandomSource.create(id * id * 3121L + id * 45238971L);
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        int cubeCount = Mth.floor(height / CUBE_HEIGHT) + 2;
        for (int i = 0; i < cubeCount; i++) {
            float dx = ((random.nextFloat() * (width * 2.0F)) - width) * 0.1F;
            float dy = Mth.clamp(1.5F - random.nextFloat() * height, -0.1F, 1.5F);
            float dz = ((random.nextFloat() * (width * 2.0F)) - width) * 0.1F;
            poseStack.pushPose();
            poseStack.translate(dx, dy, dz);
            poseStack.scale(CUBE_SCALE, CUBE_SCALE, CUBE_SCALE);
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(ICE_BLOCK, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.translucentMovingBlock());
            poseStack.popPose();
        }
    }
}