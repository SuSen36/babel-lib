package com.susen36.babel.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;

// 包装渲染器原模型，强制主体半透明；渲染结束后由 InvisibleRenderHandler 换回原模型
public class BabelTranslucentModel extends EntityModel<LivingEntity> {

    private static final float BODY_ALPHA = 0.75F;

    private final EntityModel<LivingEntity> delegate;

    public BabelTranslucentModel(EntityModel<LivingEntity> delegate) {
        this.delegate = delegate;
    }

    public EntityModel<LivingEntity> getDelegate() {
        return delegate;
    }

    @Override
    public void prepareMobModel(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks) {
        this.delegate.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.delegate.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.attackTime = this.delegate.attackTime;
        this.riding = this.delegate.riding;
        this.young = this.delegate.young;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.delegate.attackTime = this.attackTime;
        this.delegate.riding = this.riding;
        this.delegate.young = this.young;
        this.delegate.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, Math.min(alpha, BODY_ALPHA));
    }
}
