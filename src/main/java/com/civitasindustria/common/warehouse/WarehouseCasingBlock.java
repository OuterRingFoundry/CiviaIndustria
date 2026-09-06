package com.civitasindustria.common.warehouse;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
public final class WarehouseCasingBlock extends Block {
    public WarehouseCasingBlock(Properties properties){super(properties);}
    @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moving){super.onPlace(state,level,pos,old,moving);WarehouseStructure.changed(level,pos);}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){super.onRemove(state,level,pos,next,moving);WarehouseStructure.changed(level,pos);}
}
