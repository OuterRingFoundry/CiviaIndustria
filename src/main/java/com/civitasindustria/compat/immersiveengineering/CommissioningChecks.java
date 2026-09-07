package com.civitasindustria.compat.immersiveengineering;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.civitasindustria.common.factory.MachineCommissioning;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
/** Exact native IE master/helper/state gate, including oriented full-footprint foundations. */
public final class CommissioningChecks {
    public static void run(GameTestHelper h){
        int index=0;
        for(String name:new String[]{"crusher","arc_furnace","diesel_generator"}){
            var p=new BlockPos(3+index++*8,3,3);var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("immersiveengineering:"+name));var bs=block.defaultBlockState();
            for(var prop:bs.getProperties())if(prop instanceof net.minecraft.world.level.block.state.properties.BooleanProperty flag&&prop.getName().contains("slave"))bs=bs.setValue(flag,false);
            h.setBlock(p,bs);var master=(MultiblockBlockEntityMaster<?>)h.getBlockEntity(p);var helper=master.getHelper();var state=helper.getState();
            var active=new CompoundTag();active.putBoolean(name.equals("crusher")?"renderActive":"active",true);state.readSyncNBT(active,h.getLevel().registryAccess());
            helper.tickServer();if(IEActivity.active(master,bs))throw new AssertionError("Native IE suspension left activity set: "+name);
            if(MachineCommissioning.foundation(master))throw new AssertionError("Missing footprint authorized: "+name);
            var size=helper.getMultiblock().size(h.getLevel());var context=helper.getContext();
            for(int x=0;x<size.getX();x++)for(int z=0;z<size.getZ();z++)h.getLevel().setBlock(context.getLevel().toAbsolute(new BlockPos(x,-1,z)),Blocks.IRON_BLOCK.defaultBlockState(),3);
            var kit=new ItemStack(CivitasRegistries.CALIBRATION_KIT.get(),2);
            if(!MachineCommissioning.begin(master,kit)||kit.getCount()!=1)throw new AssertionError("Valid oriented IE foundation refused: "+name);
            for(int i=0;i<10;i++){master.getPersistentData().getCompound(MachineCommissioning.KEY).putLong("checkedAt",h.getLevel().getGameTime()-20);helper.tickServer();}
            if(!MachineCommissioning.canOperate(master))throw new AssertionError("IE native helper did not calibrate: "+name);
            var saved=master.saveWithFullMetadata(h.getLevel().registryAccess());master.loadWithComponents(saved,h.getLevel().registryAccess());if(!MachineCommissioning.canOperate(master))throw new AssertionError("IE reload lost calibration");
            h.getLevel().setBlock(context.getLevel().toAbsolute(new BlockPos(0,-1,0)),Blocks.AIR.defaultBlockState(),3);
            master.getPersistentData().getCompound(MachineCommissioning.KEY).putLong("checkedAt",h.getLevel().getGameTime()-20);helper.tickServer();if(MachineCommissioning.canOperate(master))throw new AssertionError("Missing IE footprint cell retained authority");
        }
        h.succeed();
    }
}
