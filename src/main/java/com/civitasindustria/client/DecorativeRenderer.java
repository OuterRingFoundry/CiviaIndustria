package com.civitasindustria.client;
import com.civitasindustria.common.decoration.DecorativeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.Blocks;
public final class DecorativeRenderer implements BlockEntityRenderer<DecorativeEntity> {
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;
    public DecorativeRenderer(BlockEntityRendererProvider.Context context){blocks=context.getBlockRenderDispatcher();}
    @Override public void render(DecorativeEntity entity,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        double ticks=entity.getLevel()==null?0:entity.getLevel().getGameTime()-entity.startTime+partial;
        float angle=entity.enabled?(float)((ticks*entity.rpm*.3)%360):0;
        pose.pushPose();pose.translate(.5,.6,.5);pose.mulPose(Axis.YP.rotationDegrees(angle));pose.scale(.7f,.12f,.7f);pose.translate(-.5,0,-.5);
        blocks.renderSingleBlock(Blocks.COPPER_BLOCK.defaultBlockState(),pose,buffers,entity.getLevel()==null?light:net.minecraft.client.renderer.LevelRenderer.getLightColor(entity.getLevel(),entity.getBlockPos().above()),overlay);pose.popPose();
    }
}
