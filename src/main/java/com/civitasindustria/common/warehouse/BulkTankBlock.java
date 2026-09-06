package com.civitasindustria.common.warehouse;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class BulkTankBlock extends BaseEntityBlock {
    private static final MapCodec<BulkTankBlock> CODEC=simpleCodec(BulkTankBlock::new);
    public BulkTankBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new BulkTankBlockEntity(pos,state);}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof BulkTankBlockEntity tank){
            net.neoforged.neoforge.fluids.FluidUtil.interactWithFluidHandler(player,hand,tank);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Tank: "+tank.amount()+" mB"),true);
        }
        return ItemInteractionResult.SUCCESS;
    }
}
