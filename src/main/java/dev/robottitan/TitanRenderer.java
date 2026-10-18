package dev.robottitan;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TitanRenderer extends MobRenderer<TitanEntity, TitanModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RobotTitanMod.MODID, "textures/entity/titan.png");

    public TitanRenderer(EntityRendererProvider.Context context) {
        super(context, new TitanModel(context.bakeLayer(TitanModel.LAYER)), 1.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(TitanEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(TitanEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(3.2F, 3.2F, 3.2F);
    }
}
