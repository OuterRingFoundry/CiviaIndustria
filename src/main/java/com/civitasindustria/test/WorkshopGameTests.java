package com.civitasindustria.test;
import com.civitasindustria.common.workshop.*;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.factory.MachineCommissioning;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class WorkshopGameTests {
    @GameTest(template="empty",batch="workshop",timeoutTicks=400)
    public static void paidMachiningMenuAndRecovery(GameTestHelper h){
        var p=new BlockPos(3,2,3);for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.setBlock(p.offset(x,-1,z),Blocks.IRON_BLOCK);
        h.setBlock(p,CivitasRegistries.CONTENT.get("precision_workbench").get());var e=(WorkshopEntity)h.getBlockEntity(p);
        var kit=new ItemStack(CivitasRegistries.CALIBRATION_KIT.get());if(!MachineCommissioning.begin(e,kit)||!kit.isEmpty())throw new AssertionError("Paid workshop calibration");
        h.runAfterDelay(220,()->{
            var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CIWorkshop"));var absolute=h.absolutePos(p);player.setPos(absolute.getX()+1,absolute.getY(),absolute.getZ());
            var menu=new WorkshopMenu(1,player.getInventory(),e,e.data);if(!menu.stillValid(player)||menu.clickMenuButton(player,99))throw new AssertionError("Menu validation");
            e.inventory.insertItem(0,new ItemStack(Items.IRON_INGOT,2),false);e.inventory.insertItem(1,new ItemStack(CivitasRegistries.CUTTING_INSERT.get()),false);
            if(e.power.receiveEnergy(1000,true)!=1000||e.power.getEnergyStored()!=0)throw new AssertionError("Energy simulation mutated state");
            for(int i=0;i<20;i++)e.tick();if(e.data.get(1)!=0)throw new AssertionError("Unpaid processing");
            for(int i=0;i<4;i++)e.power.receiveEnergy(800,false);
            for(int i=0;i<40;i++)e.tick();int savedProgress=e.data.get(1);var saved=e.saveWithFullMetadata(h.getLevel().registryAccess());e.loadWithComponents(saved,h.getLevel().registryAccess());if(e.data.get(1)!=savedProgress)throw new AssertionError("Progress lost on NBT reload");
            for(int i=0;i<60;i++)e.tick();
            if(e.power.getEnergyStored()!=0||e.inventory.getStackInSlot(2).getCount()!=1||e.inventory.getStackInSlot(0).getCount()!=1||e.inventory.getStackInSlot(1).getDamageValue()!=1)throw new AssertionError("Machining payment/output/tool wear mismatch");
            if(!e.automation.extractItem(0,1,false).isEmpty()||!e.automation.extractItem(1,1,false).isEmpty())throw new AssertionError("Automation extracted stock/tool");
            // Trust revocation while a menu is open must close access, not just block opening.
            var runtime=com.civitasindustria.platform.WorldRuntime.get(h.getLevel());var claim=new com.civitasindustria.domain.Parcel(java.util.UUID.randomUUID(),java.util.UUID.randomUUID(),absolute.getX(),absolute.getY(),absolute.getZ(),absolute.getX(),absolute.getY(),absolute.getZ(),"workshop-check",java.util.Set.of(),java.util.Set.of());
            runtime.state().parcels.add(claim);try{if(menu.stillValid(player)||menu.clickMenuButton(player,1))throw new AssertionError("Revoked menu remained usable");}finally{runtime.state().parcels.remove(claim.id());}
            var future=e.saveWithFullMetadata(h.getLevel().registryAccess());future.putInt("version",9);e.loadWithComponents(future,h.getLevel().registryAccess());e.tick();
            if(!e.retainsContents()||e.power.receiveEnergy(100,false)!=0||!e.automation.extractItem(2,1,false).isEmpty()||e.saveWithFullMetadata(h.getLevel().registryAccess()).getInt("version")!=9)throw new AssertionError("Future machine data not preserved/refused");
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="power_bridge",timeoutTicks=180)
    public static void nativeConversionAndIEWire(GameTestHelper h){
        if(!net.neoforged.fml.ModList.get().isLoaded("create")){h.succeed();return;}
        com.civitasindustria.compat.create.PowerBridgeChecks.run(h);
    }
}
