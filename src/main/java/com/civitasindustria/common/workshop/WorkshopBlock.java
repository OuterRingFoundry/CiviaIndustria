package com.civitasindustria.common.workshop;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import com.civitasindustria.common.registry.CivitasRegistries;
public final class WorkshopBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty OPERATION=IntegerProperty.create("operation",0,2);
    public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
    public WorkshopBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(ACTIVE,false).setValue(OPERATION,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(WorkshopBlock::new);}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,ACTIVE,OPERATION);}
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new WorkshopEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,CivitasRegistries.WORKSHOP_ENTITY.get(),(level,pos,state,e)->e.tick());}
    private InteractionResult open(Level l,BlockPos p,Player player){if(!l.isClientSide&&l.getBlockEntity(p) instanceof WorkshopEntity e&&e.mayUse(player))player.openMenu(e,p);return InteractionResult.SUCCESS;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){return open(l,p,player);}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){open(l,p,player);return ItemInteractionResult.SUCCESS;}
}
