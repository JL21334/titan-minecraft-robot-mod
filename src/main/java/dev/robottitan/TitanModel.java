package dev.robottitan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Drawn at human-ish proportions (32 px tall) and scaled up 3.2x by the renderer,
 * so the titan stands about 6.4 blocks tall. Feet are at y = 24, like vanilla models.
 */
public class TitanModel extends EntityModel<TitanEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(RobotTitanMod.MODID, "titan"), "main");

    private final ModelPart root;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public TitanModel(ModelPart root) {
        this.root = root;
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();

        // torso and head (head sits forward so the pilot's hatch is open behind it)
        p.addOrReplaceChild("torso",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6, 0, -3, 12, 12, 6), PartPose.ZERO);
        p.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(40, 0).addBox(-3, -6, -8, 6, 6, 6)
                        .texOffs(0, 100).addBox(-2, -4, -9, 4, 2, 1), // glowing visor
                PartPose.ZERO);

        // shoulder pads
        p.addOrReplaceChild("left_pad",
                CubeListBuilder.create().texOffs(30, 104).addBox(-3, -3, -3, 6, 3, 6), PartPose.offset(-8, 1, 0));
        p.addOrReplaceChild("right_pad",
                CubeListBuilder.create().texOffs(60, 104).addBox(-3, -3, -3, 6, 3, 6), PartPose.offset(8, 1, 0));

        // legs
        p.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 24).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-3, 12, 0));
        p.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(20, 24).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(3, 12, 0));

        // arms (the right one carries the cannon)
        p.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 44).addBox(-2, 0, -2, 4, 14, 4), PartPose.offset(-8, 1, 0));
        p.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(20, 44).addBox(-2, 0, -2, 4, 14, 4)
                        .texOffs(40, 24).addBox(-1.5F, 9, -10, 3, 3, 8), // cannon barrel
                PartPose.offset(8, 1, 0));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(TitanEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float swing = Mth.cos(limbSwing * 0.5F) * 1.1F * limbSwingAmount;
        this.leftLeg.xRot = swing;
        this.rightLeg.xRot = -swing;
        this.leftArm.xRot = -swing * 0.6F;
        this.rightArm.xRot = 0.0F; // cannon arm stays level
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float r, float g, float b, float a) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
    }
}
