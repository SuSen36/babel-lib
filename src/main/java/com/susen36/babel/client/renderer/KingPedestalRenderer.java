package com.susen36.babel.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.susen36.babel.block.KingPedestalBlock;
import com.susen36.babel.block.KingPedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KingPedestalRenderer implements BlockEntityRenderer<KingPedestalBlockEntity> {
    private static final float ITEM_SCALE = 0.5625F;

    private final ItemRenderer itemRenderer;

    public KingPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(KingPedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack itemstack = blockEntity.getItem();
        if (itemstack.isEmpty()) {
            return;
        }
        Direction facing = blockEntity.getBlockState().getValue(KingPedestalBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.71875, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);
        poseStack.popPose();
    }
}
