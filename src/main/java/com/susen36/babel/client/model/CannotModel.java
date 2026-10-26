package com.susen36.babel.client.model;

import com.susen36.babel.BabelMod;
import com.susen36.babel.client.model.anim.CannotAnimations;
import com.susen36.babel.entity.CannotEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CannotModel extends HierarchicalModel<CannotEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(BabelMod.MODID, "cannot"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart bone2;
    private final ModelPart bone4;
    private final ModelPart bone5;
    private final ModelPart bone6;
    private final ModelPart bone9;
    private final ModelPart bone10;
    private final ModelPart leg;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public CannotModel(ModelPart root) {
        this.root = root.getChild("root");
        this.head = this.root.getChild("head");
        this.body = this.root.getChild("body");
        this.bone2 = this.body.getChild("bone2");
        this.bone4 = this.bone2.getChild("bone4");
        this.bone5 = this.body.getChild("bone5");
        this.bone6 = this.bone5.getChild("bone6");
        this.bone9 = this.bone5.getChild("bone9");
        this.bone10 = this.bone9.getChild("bone10");
        this.leg = this.root.getChild("leg");
        this.left_leg = this.leg.getChild("left_leg");
        this.right_leg = this.leg.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -30.75F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(120, 57).addBox(-3.375F, -11.25F, -3.375F, 6.75F, 11.25F, 6.75F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(42, 57).addBox(-5.25F, -6.675F, -4.5F, 10.5F, 6.75F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(42, 75).addBox(-3.5F, 0.0F, -3.0F, 7.5F, 14.25F, 6.75F, new CubeDeformation(0.0F))
                .texOffs(69, 0).addBox(-1.8F, 3.75F, -4.725F, 3.75F, 5.25F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -30.75F, 0.0F));

        PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(69, 0).addBox(-2.25F, -2.25F, -0.75F, 3.75F, 5.25F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.45F, 6.975F, -3.15F, 0.0F, -0.6545F, 0.0F));

        PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(42, 40).addBox(-6.0F, -5.25F, -5.25F, 12.0F, 5.25F, 8.25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.75F, 2.775F, 0.4363F, 0.0F, 0.0F));

        PartDefinition bone2 = body.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(42, 0).addBox(-3.0F, -2.25F, -4.875F, 8.25F, 5.25F, 10.5F, new CubeDeformation(0.0F)), PartPose.offset(-7.5F, 0.0F, 0.0F));

        PartDefinition bone4 = bone2.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -3.75F, 5.25F, 25.5F, 8.25F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone5 = body.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(42, 20).addBox(-0.6926F, 0.0F, -8.0445F, 8.25F, 5.25F, 10.5F, new CubeDeformation(0.0F)), PartPose.offset(3.75F, -2.25F, 4.5F));

        PartDefinition bone6 = bone5.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 39).addBox(-3.75F, -1.5F, -3.75F, 5.25F, 25.5F, 8.25F, new CubeDeformation(0.0F))
                .texOffs(168, 6).addBox(1.125F, -0.75F, -3.375F, 0.0F, 19.5F, 7.5F, new CubeDeformation(0.0F))
                .texOffs(168, 37).addBox(0.375F, -0.75F, -3.375F, 0.0F, 19.5F, 7.5F, new CubeDeformation(0.0F)), PartPose.offset(4.5574F, 2.25F, -3.1695F));

        PartDefinition bone9 = bone5.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(157, 0).addBox(-0.75F, -1.5F, -9.375F, 5.25F, 4.5F, 8.25F, new CubeDeformation(-0.075F)), PartPose.offset(1.5574F, 2.25F, 2.8305F));

        PartDefinition bone10 = bone9.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -0.75F, 0.0F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(2.25F, 0.0F, -8.25F));

        PartDefinition leg = root.addOrReplaceChild("leg", CubeListBuilder.create(), PartPose.offset(0.0F, -15.0F, 0.0F));

        PartDefinition left_leg = leg.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(75, 75).addBox(-1.5F, -1.5F, -3.0F, 5.25F, 16.5F, 6.75F, new CubeDeformation(0.0F)), PartPose.offset(-3.75F, 0.0F, 0.0F));

        PartDefinition right_leg = leg.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 78).addBox(-3.75F, -1.5F, -3.0F, 5.25F, 16.5F, 6.75F, new CubeDeformation(0.0F)), PartPose.offset(3.75F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 192, 192);
    }

    public void setupAnim(CannotEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.idleAnimationState, CannotAnimations.IDLE, ageInTicks);
        this.animateWalk(CannotAnimations.MOVE, limbSwing, limbSwingAmount, 4.5F, 2.5F);
    }

    public ModelPart root() {
        return this.root;
    }
}
