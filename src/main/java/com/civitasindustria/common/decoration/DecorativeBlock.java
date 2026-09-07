package com.civitasindustria.common.decoration;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class DecorativeBlock extends BaseEntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public DecorativeBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,net.minecraft.core.Direction.NORTH));}
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    @Override protected boolean isSignalSource(BlockState state){return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this).getPath().equals("decorative_gear");}
    @Override protected int getSignal(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.core.Direction side){return isSignalSource(state)&&level.getBlockEntity(pos) instanceof DecorativeEntity entity?entity.signal():0;}
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack held,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof DecorativeEntity entity)player.displayClientMessage(net.minecraft.network.chat.Component.literal(DecorativeFunctions.use(entity,player,held,player.isShiftKeyDown())),true);
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(DecorativeBlock::new);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DecorativeEntity(p,s);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos p,Player player,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(p) instanceof DecorativeEntity entity)player.displayClientMessage(net.minecraft.network.chat.Component.literal(DecorativeFunctions.use(entity,player,net.minecraft.world.item.ItemStack.EMPTY,player.isShiftKeyDown())),true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
