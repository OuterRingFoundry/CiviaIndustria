package com.civitasindustria.compat.create;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
/** A real adjacent native motor/dynamo network; optional IE test consumes its generated FE. */
public final class PowerBridgeChecks {
    public static void run(GameTestHelper h){
        BlockPos m=new BlockPos(1,2,1),d=m.south();
        h.setBlock(m,CivitasRegistries.CONTENT.get("electric_motor").get().defaultBlockState().setValue(PowerBridgeBlock.FACING,Direction.SOUTH));
        h.setBlock(d,CivitasRegistries.CONTENT.get("rotation_dynamo").get().defaultBlockState().setValue(PowerBridgeBlock.FACING,Direction.NORTH));
        var motor=(PowerBridgeEntity)h.getBlockEntity(m);var dynamo=(PowerBridgeEntity)h.getBlockEntity(d);
        if(h.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,h.absolutePos(m),Direction.SOUTH)!=null)throw new AssertionError("Axle exposed electrical port");
        if(!motor.port.canReceive()||motor.port.canExtract()||!dynamo.port.canExtract()||dynamo.port.canReceive())throw new AssertionError("Converter port direction");
        h.runAfterDelay(30,()->{
            if(net.neoforged.fml.ModList.get().isLoaded("immersiveengineering"))com.civitasindustria.compat.immersiveengineering.WorkshopWireChecks.connect(h,d,new BlockPos(5,2,2));
            for(int i=0;i<8;i++)motor.port.receiveEnergy(2000,false);
        });
        h.runAfterDelay(50,()->{
            int paid=16000-motor.port.getEnergyStored();if(Math.abs(dynamo.getSpeed())!=32||motor.isOverStressed()||paid<=0)throw new AssertionError("Native rotation absent/overstressed: "+motor.getSpeed()+" / "+dynamo.getSpeed());
            int output=dynamo.port.getEnergyStored();if(output>paid/2+256)throw new AssertionError("Conversion gain");
            if(net.neoforged.fml.ModList.get().isLoaded("immersiveengineering"))com.civitasindustria.compat.immersiveengineering.WorkshopWireChecks.verify(h,new BlockPos(5,2,2),paid);
            else if(output<=0)throw new AssertionError("No generated FE");
        });
        h.runAfterDelay(90,()->{
            if(motor.getGeneratedSpeed()!=0||Math.abs(dynamo.getSpeed())>0)throw new AssertionError("Brownout retained rotation");
            var saved=dynamo.saveWithFullMetadata(h.getLevel().registryAccess());int energy=dynamo.port.getEnergyStored();dynamo.loadWithComponents(saved,h.getLevel().registryAccess());
            if(dynamo.port.getEnergyStored()!=energy)throw new AssertionError("Converter buffer persistence");h.succeed();
        });
    }
}
