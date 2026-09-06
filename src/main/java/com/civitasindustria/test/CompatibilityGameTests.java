package com.civitasindustria.test;
import com.civitasindustria.compat.create.CreateActivity;
import com.civitasindustria.compat.immersiveengineering.IEActivity;
import com.civitasindustria.common.environment.EmissionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.*;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class CompatibilityGameTests {
    @GameTest(template="empty",timeoutTicks=200) public static void createCarriageStorage(GameTestHelper h) throws Exception {if(ModList.get().isLoaded("create"))com.civitasindustria.compat.create.CarriageChecks.run(h);h.succeed();}
    @GameTest(template="empty",timeoutTicks=200) public static void createAssemblyRestart(GameTestHelper h) throws Exception {if(ModList.get().isLoaded("create"))com.civitasindustria.compat.create.MountedCargoChecks.assembly(h);h.succeed();}
    @GameTest(template="empty") public static void fullPackCargoTags(GameTestHelper h){
        if(ModList.get().isLoaded("kubejs")&&ModList.get().isLoaded("immersiveengineering")){
            var item=BuiltInRegistries.ITEM.get(ResourceLocation.parse("immersiveengineering:ingot_aluminum"));
            if(item==net.minecraft.world.item.Items.AIR||!new net.minecraft.world.item.ItemStack(item).is(com.civitasindustria.common.cargo.Encumbrance.BULK))throw new AssertionError("Full pack metal missed bulk classification");
        }
        h.succeed();
    }
    @GameTest(template="empty") public static void createMountedConservation(GameTestHelper h){if(ModList.get().isLoaded("create"))com.civitasindustria.compat.create.MountedCargoChecks.run(h);h.succeed();}
    @GameTest(template="empty") public static void createKineticActivity(GameTestHelper h) throws Exception {
        if(!ModList.get().isLoaded("create")){h.succeed();return;}
        var id=ResourceLocation.parse("create:mechanical_press");var block=BuiltInRegistries.BLOCK.get(id);
        h.setBlock(new BlockPos(1,1,1),block);var entity=h.getBlockEntity(new BlockPos(1,1,1));
        if(entity==null||EmissionRegistry.INSTANCE.get(id)==null)throw new AssertionError("Create machine/profile missing");
        var speed=entity.getClass().getMethod("setSpeed",float.class);speed.invoke(entity,0f);
        if(CreateActivity.active(entity))throw new AssertionError("Stopped machine active");
        speed.invoke(entity,-32f);if(!CreateActivity.active(entity))throw new AssertionError("Reverse rotation missed");
        speed.invoke(entity,0f);h.succeed();
    }
    @GameTest(template="empty") public static void ieMasterActivity(GameTestHelper h) throws Exception {
        if(!ModList.get().isLoaded("immersiveengineering")){h.succeed();return;}
        int x=1;
        for(String machine:new String[]{"coke_oven","crusher","arc_furnace","diesel_generator"}){
            var id=ResourceLocation.parse("immersiveengineering:"+machine);var block=BuiltInRegistries.BLOCK.get(id);
            var pos=new BlockPos(x++,1,1);var masterState=block.defaultBlockState();
            for(var property:masterState.getProperties())if(property instanceof net.minecraft.world.level.block.state.properties.BooleanProperty flag&&(property.getName().contains("slave")||property.getName().equals("active")))masterState=masterState.setValue(flag,false);
            h.setBlock(pos,masterState);var entity=h.getBlockEntity(pos);
            if(!IEActivity.master(entity)||EmissionRegistry.INSTANCE.get(id)==null)throw new AssertionError("IE master/profile missing "+machine+" state="+masterState+" entity="+(entity==null?"null":entity.getClass().getName()));
            if(IEActivity.active(entity,entity.getBlockState()))throw new AssertionError("Unpowered IE machine active "+machine);
            Object helper=entity.getClass().getMethod("getHelper").invoke(entity);Object state=helper.getClass().getMethod("getState").invoke(helper);
            if(machine.equals("coke_oven")){
                var prop=entity.getBlockState().getBlock().getStateDefinition().getProperty("active");
                if(!(prop instanceof net.minecraft.world.level.block.state.properties.BooleanProperty flag))throw new AssertionError("Missing coke active property");
                if(!IEActivity.active(entity,masterState.setValue(flag,true)))throw new AssertionError("Coke activity transition missed");
            }else{
                String method=machine.equals("crusher")?"shouldRenderActive":machine.equals("arc_furnace")?"isClientActive":"isActive";
                state.getClass().getMethod(method);
                var tag=new net.minecraft.nbt.CompoundTag();tag.putBoolean(machine.equals("crusher")?"renderActive":"active",true);
                state.getClass().getMethod("readSyncNBT",net.minecraft.nbt.CompoundTag.class,net.minecraft.core.HolderLookup.Provider.class).invoke(state,tag,h.getLevel().registryAccess());
                if(!IEActivity.active(entity,masterState))throw new AssertionError("IE activity transition missed "+machine);
            }
        }
        h.succeed();
    }
}
