package com.civitasindustria.test;
import com.civitasindustria.common.factory.FactoryBlockEntity;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.Commissioning;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class FactoryGameTests {
    @GameTest(template="empty")
    public static void commissionedBatchAndReload(GameTestHelper h){
        BlockPos p=new BlockPos(3,2,3);h.setBlock(p,CivitasRegistries.CONTENT.get("factory_controller").get());
        var factory=(FactoryBlockEntity)h.getBlockEntity(p);
        if(factory.beginCommissioning())throw new AssertionError("Foundation bypass");
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.setBlock(p.offset(x,-1,z),Blocks.IRON_BLOCK);
        if(!factory.beginCommissioning())throw new AssertionError("Commissioning refused valid foundation");
        for(int i=0;i<10;i++)factory.step();
        if(factory.commissioningStage()!=Commissioning.Stage.READY)throw new AssertionError("Commissioning duration");
        factory.inventory.insertItem(0,new ItemStack(Items.RAW_IRON,16),false);factory.inventory.insertItem(1,new ItemStack(Items.COAL,2),false);
        for(int i=0;i<5;i++)factory.step();
        var saved=factory.saveWithFullMetadata(h.getLevel().registryAccess());factory.loadWithComponents(saved,h.getLevel().registryAccess());
        for(int i=0;i<5;i++)factory.step();
        if(factory.inventory.getStackInSlot(2).getCount()!=16||!factory.inventory.getStackInSlot(0).isEmpty()||!factory.inventory.getStackInSlot(1).isEmpty())throw new AssertionError("Batch did not conserve inputs/fuel across reload");
        h.setBlock(p.below(),Blocks.AIR);factory.step();
        if(factory.commissioningStage()!=Commissioning.Stage.DEGRADED)throw new AssertionError("Foundation removal retained commissioning");
        h.succeed();
    }
}
