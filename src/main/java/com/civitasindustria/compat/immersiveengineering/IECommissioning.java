package com.civitasindustria.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Uses the exact IE multiblock orientation/context API, without forcing absent chunks. */
public final class IECommissioning {
    private IECommissioning(){}
    public static BlockEntity resolve(BlockEntity part){
        if(!(part instanceof IMultiblockBE<?> machine)||part instanceof MultiblockBlockEntityMaster<?>)return part;
        var helper=machine.getHelper();var context=helper.getContext();if(context==null)return null;
        var pos=context.getLevel().toAbsolute(helper.getMultiblock().masterPosInMB());var level=part.getLevel();
        return level!=null&&level.hasChunkAt(pos)?level.getBlockEntity(pos):null;
    }
    public static boolean foundation(BlockEntity machine,ResourceLocation material){
        if(!(machine instanceof MultiblockBlockEntityMaster<?> master)||machine.getLevel()==null)return false;
        var helper=master.getHelper();var size=helper.getMultiblock().size(machine.getLevel());var context=helper.getContext();
        if(context==null||size.getX()<1||size.getZ()<1||size.getX()*size.getZ()>128)return false;
        var tag=TagKey.create(Registries.BLOCK,material);
        for(int x=0;x<size.getX();x++)for(int z=0;z<size.getZ();z++){
            var pos=context.getLevel().toAbsolute(new BlockPos(x,-1,z));
            if(!machine.getLevel().hasChunkAt(pos)||!machine.getLevel().getBlockState(pos).is(tag))return false;
        }
        return true;
    }
    public static boolean collisionAllowed(Object raw){
        if(!(raw instanceof blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext<?> context))return false;
        var level=context.getLevel();var pos=level.toAbsolute(CrusherLogic.MASTER_OFFSET);var world=level.getRawLevel();
        if(world==null||!world.hasChunkAt(pos))return false;
        var machine=world.getBlockEntity(pos);return machine!=null&&com.civitasindustria.common.factory.MachineCommissioning.canOperate(machine);
    }
    public static void suspend(BlockEntity machine){
        if(!(machine instanceof MultiblockBlockEntityMaster<?> master)||machine.getLevel()==null)return;
        var state=master.getHelper().getState();
        boolean active=state instanceof CrusherLogic.State s?s.shouldRenderActive():state instanceof ArcFurnaceLogic.State arc?arc.isClientActive():state instanceof DieselGeneratorLogic.State diesel&&diesel.isActive();
        if(!active)return;
        var tag=new CompoundTag();state.writeSyncNBT(tag,machine.getLevel().registryAccess());tag.putBoolean(state instanceof CrusherLogic.State?"renderActive":"active",false);state.readSyncNBT(tag,machine.getLevel().registryAccess());master.getHelper().getContext().markDirtyAndSync();
    }
}
