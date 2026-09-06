package com.civitasindustria.test;

import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.warehouse.CargoBlockEntity;
import com.civitasindustria.domain.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("civitas_industria")
@PrefixGameTestTemplate(false)
public final class CargoGameTests {
    private static CargoBlockEntity crate(GameTestHelper h,String id,BlockPos pos){
        h.setBlock(pos,CivitasRegistries.CONTENT.get(id).get());
        return (CargoBlockEntity)h.getBlockEntity(pos);
    }
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    @GameTest(template="empty")
    public static void stableSlotsAndSimulation(GameTestHelper h){
        var c=crate(h,"cargo_crate",new BlockPos(1,1,1));
        var cap=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,c.getBlockPos(),null);
        check(cap!=null,"Capability registered");
        check(cap.insertItem(7,new ItemStack(Items.IRON_INGOT,64),true).isEmpty()&&c.total()==0,"Simulated insertion mutated storage");
        cap.insertItem(7,new ItemStack(Items.IRON_INGOT,64),false);
        cap.insertItem(2,new ItemStack(Items.COAL,32),false);
        check(cap.getStackInSlot(7).is(Items.IRON_INGOT),"Slot changed after alphabetically earlier insertion");
        check(cap.extractItem(7,100,true).getCount()==64&&c.total()==96,"Simulated extraction");
        ItemStack named=new ItemStack(Items.IRON_INGOT);named.set(DataComponents.CUSTOM_NAME,Component.literal("retain me"));
        check(cap.insertItem(7,named,false).getCount()==1&&c.total()==96,"Component-rich cargo must be rejected before mutation");
        var tag=c.saveWithFullMetadata(h.getLevel().registryAccess());
        c.loadWithComponents(tag,h.getLevel().registryAccess());
        check(cap.getStackInSlot(7).is(Items.IRON_INGOT)&&c.total()==96,"Stable slots across NBT reload");
        check(cap.extractItem(7,Integer.MAX_VALUE,false).getCount()==64&&c.total()==32,"Extraction int boundary");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void missingItemsAndFutureData(GameTestHelper h){
        var c=crate(h,"cargo_crate",new BlockPos(1,1,1));
        BulkInventory old=new BulkInventory(16,100000);old.insert("missing_mod:metal",90000,false);
        CompoundTag tag=new CompoundTag();tag.putInt("dataVersion",2);tag.putByteArray("cargo",DataMigrationManager.encodeInventory(old));tag.putString("unknownFutureField","keep");
        c.loadWithComponents(tag,h.getLevel().registryAccess());
        check(c.isQuarantined()&&c.hasContents(),"Missing item must quarantine");
        check(c.handler.extractItem(0,64,false).isEmpty(),"Unknown item was extracted");
        CompoundTag preserved=c.saveWithFullMetadata(h.getLevel().registryAccess());
        check(java.util.Arrays.equals(tag.getByteArray("cargo"),preserved.getByteArray("cargo"))&&preserved.getString("unknownFutureField").equals("keep"),"Quarantine lost original fields");
        tag.putInt("dataVersion",99);c.loadWithComponents(tag,h.getLevel().registryAccess());
        check(c.saveWithFullMetadata(h.getLevel().registryAccess()).getInt("dataVersion")==99,"Future schema overwritten");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void warehouseAuthorityAndDiagonalInvalidation(GameTestHelper h){
        BlockPos center=new BlockPos(3,1,3);
        var controller=crate(h,"warehouse_controller",center);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)h.setBlock(center.offset(x,0,z),CivitasRegistries.CONTENT.get("warehouse_casing").get());
        var port=crate(h,"warehouse_port",center.offset(1,0,0));
        check(controller.available()&&port.authority()==controller,"Valid ring authority");
        port.handler.insertItem(3,new ItemStack(Items.IRON_INGOT,32),false);
        check(controller.total()==32&&port.total()==0,"Port owns inventory");
        h.setBlock(center.offset(-1,0,-1),Blocks.AIR);
        check(!controller.available()&&port.handler.extractItem(3,64,false).isEmpty(),"Diagonal removal failed to invalidate structure");
        check(controller.total()==32,"Breaking ring deleted contents");
        h.setBlock(center.offset(-1,0,-1),CivitasRegistries.CONTENT.get("warehouse_casing").get());
        crate(h,"warehouse_controller",center.offset(2,0,0));
        check(port.authority()==null,"Ambiguous controller must refuse capability transfer");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void longCountsAndCapacityReduction(GameTestHelper h){
        var c=crate(h,"cargo_crate",new BlockPos(1,1,1));
        BulkInventory old=new BulkInventory(16,10_000_000_000L);old.insert("minecraft:iron_ingot",5_000_000_000L,false);
        CompoundTag tag=new CompoundTag();tag.putInt("dataVersion",2);tag.putByteArray("cargo",DataMigrationManager.encodeInventory(old));
        c.loadWithComponents(tag,h.getLevel().registryAccess());
        check(!c.isQuarantined()&&c.total()==5_000_000_000L,"Grandfather oversized persisted counts");
        check(c.handler.insertItem(0,new ItemStack(Items.IRON_INGOT),false).getCount()==1,"Capacity reduction allows new deposits");
        check(c.handler.extractItem(0,Integer.MAX_VALUE,false).getCount()==64&&c.total()==4_999_999_936L,"Long count truncated by capability");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void fluidConservation(GameTestHelper h){
        BlockPos p=new BlockPos(1,1,1);h.setBlock(p,CivitasRegistries.CONTENT.get("bulk_tank").get());
        var tank=(com.civitasindustria.common.warehouse.BulkTankBlockEntity)h.getBlockEntity(p);
        var water=new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,1000);
        var simulate=net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;
        var execute=net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
        check(tank.fill(water,simulate)==1000&&tank.amount()==0,"Fluid simulation mutated state");
        check(tank.fill(water,execute)==1000&&tank.amount()==1000,"Fluid fill");
        check(tank.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.LAVA,1000),execute)==0,"Mixed fluids accepted");
        var saved=tank.saveWithFullMetadata(h.getLevel().registryAccess());tank.loadWithComponents(saved,h.getLevel().registryAccess());
        check(tank.drain(400,simulate).getAmount()==400&&tank.amount()==1000,"Fluid simulated drain");
        check(tank.drain(400,execute).getAmount()==400&&tank.amount()==600,"Fluid reload/conservation");
        saved.putString("fluid","missing:fluid");tank.loadWithComponents(saved,h.getLevel().registryAccess());
        check(tank.hasContents()&&tank.drain(1000,execute).isEmpty()&&tank.saveWithFullMetadata(h.getLevel().registryAccess()).getString("fluid").equals("missing:fluid"),"Missing fluid quarantine");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void freightBatchConservation(GameTestHelper h){
        var loader=crate(h,"cargo_loader",new BlockPos(3,1,3));
        h.setBlock(new BlockPos(3,1,4),Blocks.CHEST);
        var source=(net.minecraft.world.level.block.entity.ChestBlockEntity)h.getBlockEntity(new BlockPos(3,1,4));
        source.setItem(0,new ItemStack(Items.IRON_INGOT,64));
        var destination=crate(h,"cargo_crate",new BlockPos(3,1,2));
        loader.transfer(10);check(loader.total()==64&&source.getItem(0).isEmpty(),"Freight loading");
        var saved=loader.saveWithFullMetadata(h.getLevel().registryAccess());loader.loadWithComponents(saved,h.getLevel().registryAccess());
        loader.transfer(20);check(loader.total()==0&&destination.total()==64,"Freight unload after NBT reload conserves cargo");
        loader.transfer(30);check(destination.total()==64,"Repeated transfer duplicated cargo");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void enderChestDepositAndWithdrawal(GameTestHelper h){
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CICargo"));
        var menu=net.minecraft.world.inventory.ChestMenu.threeRows(1,player.getInventory(),player.getEnderChestInventory());player.containerMenu=menu;
        player.getInventory().setItem(9,new ItemStack(Items.IRON_INGOT,64));
        menu.clicked(27,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,player);
        check(player.getInventory().getItem(9).getCount()==64&&player.getEnderChestInventory().isEmpty(),"Shift-click bypassed Ender cargo rule");
        menu.setCarried(new ItemStack(Items.IRON_INGOT,1));menu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,player);
        check(menu.getCarried().getCount()==1&&player.getEnderChestInventory().isEmpty(),"Cursor deposit bypassed Ender rule");
        menu.setCarried(ItemStack.EMPTY);player.getEnderChestInventory().setItem(0,new ItemStack(Items.IRON_INGOT,10));
        menu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,player);menu.clicked(28,0,net.minecraft.world.inventory.ClickType.PICKUP,player);
        check(player.getInventory().getItem(10).getCount()==10&&menu.getCarried().isEmpty(),"Existing Ender cargo cannot be recovered");
        player.getInventory().setItem(0,new ItemStack(Items.COAL,5));menu.clicked(0,0,net.minecraft.world.inventory.ClickType.SWAP,player);
        check(player.getEnderChestInventory().isEmpty()&&player.getInventory().getItem(0).getCount()==5,"Hotbar swap bypass");
        player.containerMenu=player.inventoryMenu;h.succeed();
    }
    @GameTest(template="empty")
    public static void nestedCargoWeight(GameTestHelper h){
        ItemStack box=new ItemStack(Items.SHULKER_BOX);
        box.set(DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(new ItemStack(Items.IRON_INGOT,64))));
        check(com.civitasindustria.common.cargo.Encumbrance.mass(box)==512,"Nested bulk cargo became weightless");
        check(com.civitasindustria.common.cargo.Encumbrance.mass(new ItemStack(Items.APPLE,64))==0,"Ordinary items lost inventory convenience");
        h.succeed();
    }
}
