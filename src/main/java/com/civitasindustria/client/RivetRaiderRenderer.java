package com.civitasindustria.client;
import com.civitasindustria.common.threat.IndustrialRaider;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
public final class RivetRaiderRenderer extends HumanoidMobRenderer<IndustrialRaider,RivetRaiderModel> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("minecraft:textures/block/iron_block.png");
    public RivetRaiderRenderer(EntityRendererProvider.Context c){super(c,new RivetRaiderModel(c.bakeLayer(RivetRaiderModel.LAYER)),.5f);addLayer(new BrassDetails(this));}
    @Override public ResourceLocation getTextureLocation(IndustrialRaider e){return TEXTURE;}
    private static final class BrassDetails extends net.minecraft.client.renderer.entity.layers.RenderLayer<IndustrialRaider,RivetRaiderModel> {
        BrassDetails(RivetRaiderRenderer renderer){super(renderer);}
        @Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,IndustrialRaider entity,float limb,float amount,float partial,float age,float yaw,float pitch){
            var m=getParentModel();var brass=buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(ResourceLocation.parse("minecraft:textures/block/gold_block.png")));
            pose.pushPose();m.head.translateAndRotate(pose);m.goggles.visible=true;m.goggles.render(pose,brass,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);m.goggles.visible=false;
            var glass=buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(ResourceLocation.parse("minecraft:textures/block/cyan_stained_glass.png")));
            m.lenses.visible=true;m.lenses.render(pose,glass,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);m.lenses.visible=false;pose.popPose();
            pose.pushPose();m.body.translateAndRotate(pose);m.boiler.visible=true;m.boiler.render(pose,brass,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);m.boiler.visible=false;pose.popPose();
        }
    }
}
