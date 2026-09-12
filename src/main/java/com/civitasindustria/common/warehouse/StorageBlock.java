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
public class StorageBlock extends BaseEntityBlock {
    public static final MapCodec<StorageBlock> CODEC=simpleCodec(StorageBlock::new);
    public StorageBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moving){super.onPlace(state,level,pos,old,moving);WarehouseStructure.changed(level,pos);if(level instanceof net.minecraft.server.level.ServerLevel server)com.civitasindustria.platform.WorldRuntime.get(server).machineChanged(pos);}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){super.onRemove(state,level,pos,next,moving);WarehouseStructure.changed(level,pos);if(level instanceof net.minecraft.server.level.ServerLevel server)com.civitasindustria.platform.WorldRuntime.get(server).machineChanged(pos);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new CargoBlockEntity(pos,state);}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(this).getPath().equals("pallet")?Block.box(0,0,0,16,4,16):super.getShape(state,level,pos,context);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof CargoBlockEntity cargo)CargoMenu.open(player,cargo);
        return ItemInteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(pos) instanceof CargoBlockEntity cargo)CargoMenu.open(player,cargo);
        return InteractionResult.SUCCESS;
    }
}
