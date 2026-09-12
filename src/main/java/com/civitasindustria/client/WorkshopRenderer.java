package com.civitasindustria.client;
import com.civitasindustria.common.workshop.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
public final class WorkshopRenderer implements BlockEntityRenderer<WorkshopEntity> {
    public static ModelResourceLocation model(String part){return ModelResourceLocation.standalone(ResourceLocation.parse("civitas_industria:block/moving/"+part));}
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;
    public WorkshopRenderer(BlockEntityRendererProvider.Context c){blocks=c.getBlockRenderDispatcher();}
    public void render(WorkshopEntity e,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(e.getLevel()==null)return;var state=e.getBlockState();float angle=state.getValue(WorkshopBlock.ACTIVE)?(float)((e.getLevel().getGameTime()+partial)*18%360):0;
        pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(switch(state.getValue(WorkshopBlock.FACING)){case EAST->-90;case SOUTH->180;case WEST->90;default->0;}));pose.translate(-.5,0,-.5);
        boolean turning=state.getValue(WorkshopBlock.OPERATION)==0;
        String name=turning?"workshop_chuck":"workshop_drill";double x=turning?6.0/16:11.0/16,y=turning?11.0/16:13.0/16,z=turning?8.5/16:13.0/16;
        pose.translate(x,y,z);pose.mulPose((turning?Axis.XP:Axis.YP).rotationDegrees(angle));pose.translate(-x,-y,-z);
        blocks.getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),state,Minecraft.getInstance().getModelManager().getModel(model(name)),1,1,1,light,overlay);pose.popPose();
    }
}
