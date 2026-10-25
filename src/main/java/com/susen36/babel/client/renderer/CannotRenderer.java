package com.susen36.babel.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.susen36.babel.BabelMod;
import com.susen36.babel.client.model.CannotModel;
import com.susen36.babel.entity.CannotEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CannotRenderer extends MobRenderer<CannotEntity, CannotModel> {
    private static final float SCALE = 0.875F;
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(BabelMod.MODID, "textures/entity/cannot.png");

    public CannotRenderer(EntityRendererProvider.Context context) {
        super(context, new CannotModel(context.bakeLayer(CannotModel.LAYER_LOCATION)), 0.5F);
    }

    protected void scale(CannotEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(SCALE, SCALE, SCALE);
        super.scale(entity, poseStack, partialTickTime);
    }

    public ResourceLocation getTextureLocation(CannotEntity entity) {
        return TEXTURE_LOCATION;
    }
}
