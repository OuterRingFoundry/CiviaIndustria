package com.civitasindustria.compat.create;
import com.civitasindustria.common.factory.MachineCommissioning;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
/** Native controller processing, using controlled kinetic speed and actual server ticks. */
public final class CommissioningChecks {
    public static void run(GameTestHelper h){
        var p=new BlockPos(3,4,3);
        for(int x=-2;x<=2;x++)for(int z=-1;z<=1;z++)h.setBlock(p.offset(x,-3,z),Blocks.IRON_BLOCK);
        h.setBlock(p.west(),AllBlocks.CRUSHING_WHEEL.get());h.setBlock(p.east(),AllBlocks.CRUSHING_WHEEL.get());
        h.setBlock(p,AllBlocks.CRUSHING_WHEEL_CONTROLLER.get());
        var controller=(CrushingWheelControllerBlockEntity)h.getBlockEntity(p);
        controller.inventory.setStackInSlot(0,new ItemStack(Items.RAW_IRON));controller.inventory.remainingTime=100;controller.crushingspeed=16;
        controller.tick();if(controller.inventory.remainingTime!=100)throw new AssertionError("Uncommissioned native Create processing ran");
        for(var pos:new BlockPos[]{p.west(),p.east()})if(!MachineCommissioning.begin(h.getBlockEntity(pos),new ItemStack(CivitasRegistries.CALIBRATION_KIT.get())))throw new AssertionError("Wheel foundation/payment refused");
        h.runAfterDelay(220,()->{
            // Natural controller ticks must advance both wheels' calibration.
            if(!MachineCommissioning.crusherCanOperate(controller))throw new AssertionError("Natural wheel calibration never completed");
            controller.inventory.setStackInSlot(0,new ItemStack(Items.RAW_IRON));controller.inventory.remainingTime=100;controller.crushingspeed=16;controller.tick();
            if(controller.inventory.remainingTime>=100)throw new AssertionError("Commissioned native Create processing stayed blocked");
            h.setBlock(p.west().below(3),Blocks.AIR);
            for(var pos:new BlockPos[]{p.west(),p.east()})h.getBlockEntity(pos).getPersistentData().getCompound(MachineCommissioning.KEY).putLong("checkedAt",h.getLevel().getGameTime()-20);
            float remaining=controller.inventory.remainingTime;controller.tick();if(controller.inventory.remainingTime!=remaining)throw new AssertionError("Broken foundation continued processing");
            h.succeed();
        });
    }
}
