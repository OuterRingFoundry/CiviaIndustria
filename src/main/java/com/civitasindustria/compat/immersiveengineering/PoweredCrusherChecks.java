package com.civitasindustria.compat.immersiveengineering;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic;
import com.civitasindustria.common.factory.MachineCommissioning;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
/** Builds IE's own template, forms it through its public API, and uses real energy/item ports. */
public final class PoweredCrusherChecks {
    public static void run(GameTestHelper h){
        var level=h.getLevel();var origin=h.absolutePos(new BlockPos(2,3,2));var template=IEMultiblocks.CRUSHER;
        for(var info:template.getStructure(level))level.setBlock(origin.offset(info.pos()),info.state(),3);
        if(!template.createStructure(level,origin.offset(template.getTriggerOffset()),Direction.SOUTH,net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level)))throw new AssertionError("Native crusher structure formation refused");
        var master=(MultiblockBlockEntityMaster<?>)level.getBlockEntity(origin.offset(template.getMasterFromOriginOffset()));
        if(master==null)throw new AssertionError("Formed crusher has no master");
        var helper=master.getHelper();var state=(CrusherLogic.State)helper.getState();var context=helper.getContext();var size=template.getSize(level);
        for(int x=0;x<size.getX();x++)for(int z=0;z<size.getZ();z++)level.setBlock(context.getLevel().toAbsolute(new BlockPos(x,-1,z)),Blocks.IRON_BLOCK.defaultBlockState(),3);
        boolean powered=false,inserted=false,dummyResolved=false;
        for(int x=0;x<size.getX();x++)for(int y=0;y<size.getY();y++)for(int z=0;z<size.getZ();z++){
            var pos=context.getLevel().toAbsolute(new BlockPos(x,y,z));var part=level.getBlockEntity(pos);
            if(part!=null&&part!=master&&IECommissioning.resolve(part)==master)dummyResolved=true;
            for(var side:Direction.values()){
                var energy=level.getCapability(Capabilities.EnergyStorage.BLOCK,pos,side);if(!powered&&energy!=null&&energy.receiveEnergy(100000,false)>0)powered=true;
                var items=level.getCapability(Capabilities.ItemHandler.BLOCK,pos,side);
                if(!inserted&&items!=null)for(int slot=0;slot<items.getSlots();slot++)if(items.insertItem(slot,new ItemStack(Items.COBBLESTONE),true).isEmpty()){
                    if(!items.insertItem(slot,new ItemStack(Items.COBBLESTONE),false).isEmpty())throw new AssertionError("IE simulated input disagreed");inserted=true;break;
                }
            }
        }
        if(!powered||!inserted||!dummyResolved||state.getProcessQueue().isEmpty())throw new AssertionError("Formed crusher missing native ports/dummy/queue: "+powered+"/"+inserted+"/"+dummyResolved);
        var animal=net.minecraft.world.entity.EntityType.PIG.create(level);animal.moveTo(context.getLevel().toAbsolute(new net.minecraft.world.phys.Vec3(2,1,1)));float health=animal.getHealth();
        @SuppressWarnings("unchecked") var typed=(blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext<CrusherLogic.State>)(Object)context;
        new CrusherLogic().onEntityCollision(typed,new BlockPos(2,1,1),animal);
        if(animal.getHealth()!=health)throw new AssertionError("Uncommissioned crusher harmed a living entity");
        int energy=state.getEnergy().getEnergyStored();var saved=new net.minecraft.nbt.CompoundTag();state.writeSaveNBT(saved,level.registryAccess());
        for(int i=0;i<40;i++)helper.tickServer();
        var paused=new net.minecraft.nbt.CompoundTag();state.writeSaveNBT(paused,level.registryAccess());
        if(energy!=state.getEnergy().getEnergyStored()||!saved.equals(paused))throw new AssertionError("Uncommissioned powered crusher mutated its processing state");
        if(!MachineCommissioning.begin(master,new ItemStack(CivitasRegistries.CALIBRATION_KIT.get())))throw new AssertionError("Formed powered crusher calibration refused");
        h.runAfterDelay(500,()->{
            if(!MachineCommissioning.canOperate(master)||state.getEnergy().getEnergyStored()>=energy||!state.getProcessQueue().isEmpty())throw new AssertionError("Native powered crusher did not complete recipe after calibration");
            var area=new net.minecraft.world.phys.AABB(origin).inflate(10);int gravel=0;
            for(var item:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area))if(item.getItem().is(Items.GRAVEL))gravel+=item.getItem().getCount();
            if(gravel!=1)throw new AssertionError("Expected one conserved gravel output, got "+gravel);
            h.succeed();
        });
    }
}
