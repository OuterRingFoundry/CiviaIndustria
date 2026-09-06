package com.civitasindustria.platform;

import com.civitasindustria.domain.Parcel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class ParcelProtection {
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> UTILITY=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.ResourceLocation.parse("civitas_industria:utility"));
    private ParcelProtection(){}
    public static boolean allows(ServerLevel level,BlockPos pos,Player player,Parcel.Flag flag){
        if(player.hasPermissions(2))return true;
        long started=System.nanoTime();var runtime=WorldRuntime.get(level);var parcel=runtime.state().parcels.at(pos.getX(),pos.getY(),pos.getZ());runtime.record("parcel",started,1);
        return parcel.isEmpty()||parcel.get().allows(player.getUUID(),flag);
    }
    public static void register(IEventBus bus){
        bus.addListener(ParcelProtection::place);bus.addListener(ParcelProtection::destroy);
        bus.addListener(ParcelProtection::interact);bus.addListener(ParcelProtection::explosion);bus.addListener(ParcelProtection::piston);
    }
    private static void place(BlockEvent.EntityPlaceEvent event){
        if(!(event.getLevel() instanceof ServerLevel level)||!(event.getEntity() instanceof Player player))return;
        if(!allows(level,event.getPos(),player,Parcel.Flag.BUILD)&&!(event.getPlacedBlock().is(UTILITY)&&allows(level,event.getPos(),player,Parcel.Flag.UTILITY_EASEMENT)))event.setCanceled(true);
        if(event instanceof BlockEvent.EntityMultiPlaceEvent multiple)
            for(var snapshot:multiple.getReplacedBlockSnapshots())if(!allows(level,snapshot.getPos(),player,Parcel.Flag.BUILD))event.setCanceled(true);
    }
    private static void destroy(BlockEvent.BreakEvent event){
        if(event.getLevel() instanceof ServerLevel level&&!allows(level,event.getPos(),event.getPlayer(),Parcel.Flag.BREAK))event.setCanceled(true);
    }
    private static void interact(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getLevel() instanceof ServerLevel level))return;
        var block=level.getBlockState(event.getPos()).getBlock();
        Parcel.Flag action=block instanceof net.minecraft.world.level.block.ButtonBlock||block instanceof net.minecraft.world.level.block.LeverBlock||block instanceof net.minecraft.world.level.block.RepeaterBlock||block instanceof net.minecraft.world.level.block.ComparatorBlock?Parcel.Flag.REDSTONE:Parcel.Flag.INTERACT;
        boolean allowed=allows(level,event.getPos(),event.getEntity(),action);
        if(block instanceof net.minecraft.world.level.block.DoorBlock||block instanceof net.minecraft.world.level.block.TrapDoorBlock||block instanceof net.minecraft.world.level.block.FenceGateBlock)allowed|=allows(level,event.getPos(),event.getEntity(),Parcel.Flag.PUBLIC_ACCESS);
        if(level.getBlockEntity(event.getPos())!=null)allowed&=allows(level,event.getPos(),event.getEntity(),Parcel.Flag.CONTAINER);
        if(!allowed){event.setCanceled(true);event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);}
    }
    private static void piston(PistonEvent.Pre event){
        if(!(event.getLevel() instanceof ServerLevel level))return;
        var resolver=event.getStructureHelper();if(resolver==null||!resolver.resolve())return;
        var index=WorldRuntime.get(level).state().parcels;
        var origin=index.at(event.getPos().getX(),event.getPos().getY(),event.getPos().getZ());
        var direction=event.getPistonMoveType().isExtend?event.getDirection():event.getDirection().getOpposite();
        java.util.List<BlockPos> affected=new java.util.ArrayList<>(resolver.getToDestroy());
        affected.add(event.getFaceOffsetPos());
        for(var pos:resolver.getToPush()){affected.add(pos);affected.add(pos.relative(direction));}
        for(var pos:affected){var destination=index.at(pos.getX(),pos.getY(),pos.getZ());
            if(!origin.equals(destination)&&!(origin.map(p->p.flags().contains(Parcel.Flag.UTILITY_EASEMENT)).orElse(true)&&destination.map(p->p.flags().contains(Parcel.Flag.UTILITY_EASEMENT)).orElse(true))){event.setCanceled(true);return;}
        }
    }
    private static void explosion(ExplosionEvent.Detonate event){
        if(event.getLevel() instanceof ServerLevel level){
            var index=WorldRuntime.get(level).state().parcels;
            event.getAffectedBlocks().removeIf(p->index.at(p.getX(),p.getY(),p.getZ()).isPresent());
        }
    }
}
