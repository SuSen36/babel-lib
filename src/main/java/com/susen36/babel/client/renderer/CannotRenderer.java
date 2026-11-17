package com.susen36.babel.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.susen36.babel.BabelMod;
import com.susen36.babel.client.model.CannotModel;
import com.susen36.babel.entity.CannotEntity;
import com.susen36.babel.util.RobberyClientUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CannotRenderer extends MobRenderer<CannotEntity, CannotModel> {
    private static final float SCALE = 0.875F;
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(BabelMod.MODID, "textures/entity/cannot.png");
    private static final ResourceLocation DURATION_ICON = new ResourceLocation(BabelMod.MODID, "textures/gui/overlay/duration.png");
    private static final float ICON_SIZE = 9.0F;
    private static final float ICON_GAP = 1.0F;
    private static final float TEXT_SCALE = 0.025F;
    private static final int TICKS_PER_SECOND = 20;

    public CannotRenderer(EntityRendererProvider.Context context) {
        super(context, new CannotModel(context.bakeLayer(CannotModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public void render(CannotEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        long remainingTicks = RobberyClientUtils.getRemainingTicks();
        if (remainingTicks > 0L && !entity.isRobberyActive()) {
            long seconds = (remainingTicks + TICKS_PER_SECOND - 1L) / TICKS_PER_SECOND;
            Component text = Component.literal(seconds / TICKS_PER_SECOND + ":" + String.format("%02d", seconds % TICKS_PER_SECOND));
            Font font = this.getFont();
            poseStack.pushPose();
            poseStack.translate(0.0D, entity.getBbHeight() + 0.275D, 0.0D);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
            Matrix4f matrix = poseStack.last().pose();
            float startX = -(ICON_SIZE + ICON_GAP + font.width(text)) / 2.0F;
            float endX = startX + ICON_SIZE;
            VertexConsumer consumer = buffer.getBuffer(RenderType.text(DURATION_ICON));
            consumer.vertex(matrix, startX, ICON_SIZE, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(packedLight).endVertex();
            consumer.vertex(matrix, endX, ICON_SIZE, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(packedLight).endVertex();
            consumer.vertex(matrix, endX, 0.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(packedLight).endVertex();
            consumer.vertex(matrix, startX, 0.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(packedLight).endVertex();
            font.drawInBatch(text, endX + ICON_GAP, 1.0F, -1, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
            poseStack.popPose();
        }
    }

    @Override
    protected void scale(CannotEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(SCALE, SCALE, SCALE);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(CannotEntity entity) {
        return TEXTURE_LOCATION;
    }
}
