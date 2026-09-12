package com.civitasindustria.client;
import com.civitasindustria.compat.create.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
public final class PowerBridgeRenderer implements BlockEntityRenderer<PowerBridgeEntity> {
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;
    public PowerBridgeRenderer(BlockEntityRendererProvider.Context c){blocks=c.getBlockRenderDispatcher();}
    public void render(PowerBridgeEntity e,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(e.getLevel()==null)return;var state=e.getBlockState();float angle=(float)((e.getLevel().getGameTime()+partial)*e.getSpeed()*.3%360);
        pose.pushPose();pose.translate(.5,.5,.5);
        switch(state.getValue(PowerBridgeBlock.FACING)){case EAST->pose.mulPose(Axis.YP.rotationDegrees(-90));case WEST->pose.mulPose(Axis.YP.rotationDegrees(90));case SOUTH->pose.mulPose(Axis.YP.rotationDegrees(180));case UP->pose.mulPose(Axis.XP.rotationDegrees(90));case DOWN->pose.mulPose(Axis.XP.rotationDegrees(-90));default->{}}
        pose.mulPose(Axis.ZP.rotationDegrees(angle));pose.translate(-.5,-.5,-.5);
        blocks.getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),state,Minecraft.getInstance().getModelManager().getModel(WorkshopRenderer.model("power_rotor")),1,1,1,light,overlay);pose.popPose();
    }
    public static void register(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(PowerBridge.ENTITY.get(),PowerBridgeRenderer::new);}
}
