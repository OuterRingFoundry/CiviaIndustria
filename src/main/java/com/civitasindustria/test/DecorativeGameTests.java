package com.civitasindustria.test;
import com.civitasindustria.common.decoration.*;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.warehouse.*;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.WorldRuntime;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class DecorativeGameTests {
    private static net.minecraft.server.level.ServerPlayer user(GameTestHelper h){return FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"CIUtility"));}
    @GameTest(template="empty") public static void paidAirServiceAndGauge(GameTestHelper h){
        var pos=new BlockPos(2,2,2);h.setBlock(pos,CivitasRegistries.CONTENT.get("decorative_fan").get());var fan=(DecorativeEntity)h.getBlockEntity(pos);var p=h.absolutePos(pos);var player=user(h);var runtime=WorldRuntime.get(h.getLevel());var cell=runtime.cell(CellPos.fromBlock(p.getX(),p.getZ()));Arrays.fill(cell.pollutants,0);
        var charcoal=new ItemStack(Items.CHARCOAL,2);DecorativeFunctions.use(fan,player,charcoal,false);if(charcoal.getCount()!=2)throw new AssertionError("Clean air spent charcoal");
        cell.add(Pollutant.PM,40);cell.add(Pollutant.SOX,15);cell.add(Pollutant.NOX,20);cell.vegetationHealth=.25;
        DecorativeFunctions.use(fan,player,charcoal,false);if(charcoal.getCount()!=1||cell.get(Pollutant.PM)!=15||cell.get(Pollutant.SOX)!=15||cell.vegetationHealth!=.25)throw new AssertionError("Fan payment/filter/ecology bounds");
        h.setBlock(pos,CivitasRegistries.CONTENT.get("decorative_vent").get());var vent=(DecorativeEntity)h.getBlockEntity(pos);var reagent=new ItemStack(CivitasRegistries.REMEDIATION_REAGENT.get(),2);DecorativeFunctions.use(vent,player,reagent,false);
        if(reagent.getCount()!=1||cell.get(Pollutant.SOX)!=0||cell.get(Pollutant.NOX)!=10||cell.get(Pollutant.PM)!=15)throw new AssertionError("Gas scrubber exceeded paid budget");
        h.setBlock(pos,CivitasRegistries.CONTENT.get("decorative_gauge").get());String reading=DecorativeFunctions.use((DecorativeEntity)h.getBlockEntity(pos),player,ItemStack.EMPTY,false);
        if(!reading.contains("AQI 25.0")||!reading.contains("Regional load"))throw new AssertionError("Gauge did not report actual regional state: "+reading);
        h.succeed();
    }
    @GameTest(template="empty") public static void handTransfersAndClaims(GameTestHelper h){
        var level=h.getLevel();var player=user(h);var p=new BlockPos(3,3,3);var absolute=h.absolutePos(p);
        h.setBlock(p,CivitasRegistries.CONTENT.get("decorative_pump").get());h.setBlock(p.below(),CivitasRegistries.CONTENT.get("bulk_tank").get());h.setBlock(p.above(),CivitasRegistries.CONTENT.get("bulk_tank").get());
        var source=(BulkTankBlockEntity)h.getBlockEntity(p.below());var destination=(BulkTankBlockEntity)h.getBlockEntity(p.above());var tag=source.saveWithFullMetadata(level.registryAccess());tag.putString("fluid","minecraft:water");tag.putLong("amount",5_000_000_123L);source.loadWithComponents(tag,level.registryAccess());
        if(DecorativeFunctions.pump(level,absolute,player)!=1000||source.amount()!=4_999_999_123L||destination.amount()!=1000)throw new AssertionError("Manual long tank transfer lost fluid");
        var claimed=absolute.above();var parcel=new Parcel(UUID.randomUUID(),UUID.randomUUID(),claimed.getX(),claimed.getY(),claimed.getZ(),claimed.getX(),claimed.getY(),claimed.getZ(),"private tank",Set.of(),Set.of());var index=WorldRuntime.get(level).state().parcels;index.add(parcel);
        try{if(DecorativeFunctions.pump(level,absolute,player)!=0||destination.amount()!=1000)throw new AssertionError("Pump bypassed destination parcel");}finally{index.remove(parcel.id());}
        destination.drain(1000,FluidAction.EXECUTE);destination.fill(new FluidStack(net.minecraft.world.level.material.Fluids.LAVA,1000),FluidAction.EXECUTE);
        if(DecorativeFunctions.pump(level,absolute,player)!=0||source.amount()!=4_999_999_123L)throw new AssertionError("Mixed-fluid refusal lost source");
        h.setBlock(p,CivitasRegistries.CONTENT.get("decorative_piston").get());h.setBlock(p.south(),CivitasRegistries.CONTENT.get("cargo_crate").get());h.setBlock(p.north(),CivitasRegistries.CONTENT.get("pallet").get());
        var from=(CargoBlockEntity)h.getBlockEntity(p.south());var to=(CargoBlockEntity)h.getBlockEntity(p.north());String[] keys=new String[16];long[] counts=new long[16];Arrays.fill(keys,"");keys[0]="minecraft:iron_ingot";counts[0]=5_000_000_123L;from.restoreMounted(keys,counts);
        if(DecorativeFunctions.push(level,absolute,Direction.NORTH,player)!=16||from.total()!=5_000_000_107L||to.total()!=16)throw new AssertionError("Manual pusher lost long cargo");
        var cargoClaim=absolute.north();var privateCargo=new Parcel(UUID.randomUUID(),UUID.randomUUID(),cargoClaim.getX(),cargoClaim.getY(),cargoClaim.getZ(),cargoClaim.getX(),cargoClaim.getY(),cargoClaim.getZ(),"private cargo",Set.of(),Set.of());index.add(privateCargo);
        try{if(DecorativeFunctions.push(level,absolute,Direction.NORTH,player)!=0||to.total()!=16)throw new AssertionError("Pusher bypassed destination parcel");}finally{index.remove(privateCargo.id());}
        counts[0]=com.civitasindustria.common.config.ServerConfig.CRATE_CAPACITY.get()-6;to.restoreMounted(keys,counts);long before=from.total()+to.total();
        if(DecorativeFunctions.push(level,absolute,Direction.NORTH,player)!=6||from.total()+to.total()!=before)throw new AssertionError("Partial pusher acceptance failed conservation");
        h.succeed();
    }
    @GameTest(template="empty") public static void regulatorPersistenceAndRefusal(GameTestHelper h){
        var p=new BlockPos(2,2,2);h.setBlock(p,CivitasRegistries.CONTENT.get("decorative_gear").get());var gear=(DecorativeEntity)h.getBlockEntity(p);var player=user(h);var level=h.getLevel();
        if(level.getSignal(h.absolutePos(p),Direction.NORTH)!=4)throw new AssertionError("Native variable redstone signal absent");
        DecorativeFunctions.use(gear,player,ItemStack.EMPTY,true);if(gear.signal()!=5)throw new AssertionError("Regulator speed did not adjust strength");
        var saved=gear.saveWithFullMetadata(level.registryAccess());gear.loadWithComponents(saved,level.registryAccess());if(gear.signal()!=5)throw new AssertionError("Regulator signal lost on reload");
        DecorativeFunctions.use(gear,player,ItemStack.EMPTY,false);if(level.getSignal(h.absolutePos(p),Direction.NORTH)!=0)throw new AssertionError("Regulator did not turn off");
        saved.putInt("dataVersion",99);gear.loadWithComponents(saved,level.registryAccess());DecorativeFunctions.use(gear,player,ItemStack.EMPTY,false);
        if(gear.signal()!=0||gear.saveWithFullMetadata(level.registryAccess()).getInt("dataVersion")!=99)throw new AssertionError("Future decoration data operated or was replaced");
        h.succeed();
    }
}
