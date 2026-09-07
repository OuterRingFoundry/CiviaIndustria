package com.civitasindustria.test;
import com.civitasindustria.common.factory.MachineCommissioning;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.Commissioning;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class CommissioningGameTests {
    @GameTest(template="empty") public static void boundPaidCommissioning(GameTestHelper h){
        var p=new BlockPos(3,2,3);h.setBlock(p,CivitasRegistries.CONTENT.get("factory_controller").get());var machine=h.getBlockEntity(p);
        var kit=new ItemStack(CivitasRegistries.CALIBRATION_KIT.get(),3);
        if(MachineCommissioning.canOperate(machine)||MachineCommissioning.begin(machine,kit)||kit.getCount()!=3)throw new AssertionError("Uncommissioned/foundation bypass");
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.setBlock(p.offset(x,-1,z),Blocks.IRON_BLOCK);
        if(!MachineCommissioning.begin(machine,kit)||kit.getCount()!=2||MachineCommissioning.begin(machine,kit)||MachineCommissioning.movable(machine))throw new AssertionError("Payment or movement bypass");
        var tag=machine.getPersistentData().getCompound(MachineCommissioning.KEY);
        for(int i=0;i<10;i++){tag.putLong("checkedAt",h.getLevel().getGameTime()-20);MachineCommissioning.canOperate(machine);}
        if(MachineCommissioning.stage(machine)!=Commissioning.Stage.READY)throw new AssertionError("Calibration did not finish");
        var saved=machine.saveWithFullMetadata(h.getLevel().registryAccess());machine.loadWithComponents(saved,h.getLevel().registryAccess());
        if(!MachineCommissioning.canOperate(machine))throw new AssertionError("Reload lost valid calibration");
        tag=machine.getPersistentData().getCompound(MachineCommissioning.KEY);tag.putLong("position",p.asLong());
        if(MachineCommissioning.canOperate(machine))throw new AssertionError("Position-bound calibration bypass");
        if(!MachineCommissioning.decommission(machine)||!MachineCommissioning.movable(machine))throw new AssertionError("Decommissioned transport refused");
        tag.putInt("version",99);var original=tag.copy();
        if(MachineCommissioning.begin(machine,kit)||MachineCommissioning.decommission(machine)||MachineCommissioning.canOperate(machine)||MachineCommissioning.movable(machine)||!original.equals(tag))throw new AssertionError("Future payload modified or authorized");
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=300) public static void nativeCreateGate(GameTestHelper h){if(net.neoforged.fml.ModList.get().isLoaded("create"))com.civitasindustria.compat.create.CommissioningChecks.run(h);else h.succeed();}
    @GameTest(template="empty",batch="powered_ie",timeoutTicks=600) public static void poweredIEFactory(GameTestHelper h){if(net.neoforged.fml.ModList.get().isLoaded("immersiveengineering"))com.civitasindustria.compat.immersiveengineering.PoweredCrusherChecks.run(h);else h.succeed();}
    @GameTest(template="empty") public static void nativeIEGate(GameTestHelper h){if(net.neoforged.fml.ModList.get().isLoaded("immersiveengineering"))com.civitasindustria.compat.immersiveengineering.CommissioningChecks.run(h);else h.succeed();}
}
