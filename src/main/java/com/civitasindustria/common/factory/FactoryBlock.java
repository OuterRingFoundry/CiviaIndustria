package com.civitasindustria.common.factory;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.civitasindustria.common.registry.CivitasRegistries;
public final class FactoryBlock extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
    private static final MapCodec<FactoryBlock> CODEC=simpleCodec(FactoryBlock::new);
    public FactoryBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(ACTIVE,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ACTIVE);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new FactoryBlockEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState s,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,CivitasRegistries.FACTORY_ENTITY.get(),(l,p,state,f)->{if(l.getGameTime()%20==0)f.step();});}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level level,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(!level.isClientSide&&level.getBlockEntity(p) instanceof FactoryBlockEntity f)f.interact(player,stack,player.isShiftKeyDown());return ItemInteractionResult.SUCCESS;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos p,Player player,BlockHitResult hit){if(!level.isClientSide&&level.getBlockEntity(p) instanceof FactoryBlockEntity f)f.interact(player,ItemStack.EMPTY,player.isShiftKeyDown());return InteractionResult.SUCCESS;}
}
