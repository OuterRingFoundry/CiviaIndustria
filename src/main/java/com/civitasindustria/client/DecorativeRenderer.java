package com.civitasindustria.client;
import com.civitasindustria.common.decoration.DecorativeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
/** Baked original moving parts; motion is derived locally, with no server animation ticker. */
public final class DecorativeRenderer implements BlockEntityRenderer<DecorativeEntity> {
    public static final java.util.List<String> PARTS=java.util.List.of("gear","fan","pump","gauge","piston","vent");
    public static ModelResourceLocation model(String name){return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("civitas_industria","block/moving/"+name));}
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;
    public DecorativeRenderer(BlockEntityRendererProvider.Context context){blocks=context.getBlockRenderDispatcher();}
    @Override public void render(DecorativeEntity entity,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        String name=BuiltInRegistries.BLOCK.getKey(entity.getBlockState().getBlock()).getPath().replace("decorative_","");
        if(!PARTS.contains(name))return;
        double ticks=entity.getLevel()==null?0:entity.getLevel().getGameTime()-entity.startTime+partial;
        float angle=entity.enabled?(float)((ticks*entity.rpm*.3)%360):0;
        double wave=Math.sin(Math.toRadians(angle));
        pose.pushPose();
        var facing=entity.getBlockState().getValue(com.civitasindustria.common.decoration.DecorativeBlock.FACING);
        float yaw=switch(facing){case EAST->-90;case SOUTH->180;case WEST->90;default->0;};
        pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(yaw));pose.translate(-.5,0,-.5);
        switch(name){
            case "gear","fan"->{pose.translate(.5,.5,.5);pose.mulPose(Axis.YP.rotationDegrees(angle));pose.translate(-.5,-.5,-.5);}
            case "pump"->pose.translate(0,(wave+1)*.10,0);
            case "piston"->pose.translate(0,0,-(wave+1)*.06);
            case "gauge"->{pose.translate(.5,.5,.25);pose.mulPose(Axis.ZP.rotationDegrees((float)(wave*35)));pose.translate(-.5,-.5,-.25);}
            case "vent"->{pose.translate(.5,.5,.5);pose.mulPose(Axis.XP.rotationDegrees((float)(wave*12)));pose.translate(-.5,-.5,-.5);}
        }
        var baked=Minecraft.getInstance().getModelManager().getModel(model(name));
        blocks.getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),entity.getBlockState(),baked,1,1,1,light,overlay);
        pose.popPose();
    }
}
