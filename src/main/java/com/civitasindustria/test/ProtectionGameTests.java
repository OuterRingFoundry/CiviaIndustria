package com.civitasindustria.test;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class ProtectionGameTests {
    @GameTest(template="empty") public static void pistonBoundaryAndBulkTeleport(GameTestHelper h){
        var level=h.getLevel();var piston=h.absolutePos(new BlockPos(1,1,1));level.setBlock(piston,Blocks.PISTON.defaultBlockState().setValue(net.minecraft.world.level.block.piston.PistonBaseBlock.FACING,Direction.EAST),3);level.setBlock(piston.east(),Blocks.STONE.defaultBlockState(),3);
        var index=WorldRuntime.get(level).state().parcels;var target=piston.east(2);var parcel=new Parcel(UUID.randomUUID(),UUID.randomUUID(),target.getX(),target.getY(),target.getZ(),target.getX()+1,target.getY()+1,target.getZ()+1,"Piston target",Set.of(),Set.of());index.add(parcel);
        try{var event=new PistonEvent.Pre(level,piston,Direction.EAST,PistonEvent.PistonMoveType.EXTEND);NeoForge.EVENT_BUS.post(event);if(!event.isCanceled())throw new AssertionError("Piston crossed claim boundary");}finally{index.remove(parcel.id());}
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"CITeleport"));player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getInventory().setItem(0,new ItemStack(Items.IRON_INGOT));
        var travel=new EntityTeleportEvent.TeleportCommand(player,100,64,100);NeoForge.EVENT_BUS.post(travel);if(!travel.isCanceled())throw new AssertionError("Bulk teleport bypass");player.getInventory().clearContent();
        var empty=new EntityTeleportEvent.TeleportCommand(player,100,64,100);NeoForge.EVENT_BUS.post(empty);if(empty.isCanceled())throw new AssertionError("Light travel blocked");h.succeed();
    }
    @GameTest(template="empty") public static void decorationHasNoTicker(GameTestHelper h){
        for(var entry:CivitasRegistries.CONTENT.entrySet())if(entry.getKey().startsWith("decorative_")){
            h.setBlock(new BlockPos(1,1,1),entry.getValue().get());var state=h.getBlockState(new BlockPos(1,1,1));
            if(state.getTicker(h.getLevel(),CivitasRegistries.DECORATIVE_ENTITY.get())!=null)throw new AssertionError("Server decoration ticker");
            var entity=(com.civitasindustria.common.decoration.DecorativeEntity)h.getBlockEntity(new BlockPos(1,1,1));entity.change(true);
            var saved=entity.saveWithoutMetadata(h.getLevel().registryAccess());var copy=new com.civitasindustria.common.decoration.DecorativeEntity(entity.getBlockPos(),state);copy.loadWithComponents(saved,h.getLevel().registryAccess());
            if(copy.rpm!=entity.rpm||copy.startTime!=entity.startTime||copy.enabled!=entity.enabled)throw new AssertionError("Animation parameter persistence");
        }
        h.succeed();
    }
}
