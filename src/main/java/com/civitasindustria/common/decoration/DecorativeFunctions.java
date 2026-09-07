package com.civitasindustria.common.decoration;

import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.warehouse.*;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Bounded hand-operated utilities. No server ticker, automation loop or retained inventory. */
public final class DecorativeFunctions {
    private DecorativeFunctions(){}
    public static String kind(DecorativeEntity entity){return BuiltInRegistries.BLOCK.getKey(entity.getBlockState().getBlock()).getPath();}
    public static boolean permitted(ServerLevel level,BlockPos pos,Player player){return level.hasChunkAt(pos)&&ParcelProtection.allows(level,pos,player,Parcel.Flag.INTERACT)&&ParcelProtection.allows(level,pos,player,Parcel.Flag.CONTAINER);}
    public static String use(DecorativeEntity entity,Player player,ItemStack held,boolean speed){
        if(!(entity.getLevel() instanceof ServerLevel level)||!entity.usable())return "Utility unavailable: preserved unsupported data";
        var pos=entity.getBlockPos();if(!permitted(level,pos,player))return "Parcel access denied";
        String kind=kind(entity);
        if(kind.equals("decorative_gear")&&!ParcelProtection.allows(level,pos,player,Parcel.Flag.REDSTONE))return "Parcel redstone access denied";
        if(speed){entity.change(true);return "Animation speed: "+entity.rpm+" RPM"+(kind.equals("decorative_gear")?" | redstone "+entity.signal():"");}
        if(kind.equals("decorative_gauge")){
            var runtime=WorldRuntime.get(level);var cell=runtime.state().cells.get(CellPos.fromBlock(pos.getX(),pos.getZ()));
            return String.format(java.util.Locale.ROOT,"Regional load %.1f | AQI %.1f | water quality %.1f | ecology %s",runtime.load(pos),cell==null?0:cell.aqi(),cell==null?100:cell.waterQuality(),cell==null?"PRISTINE":cell.ecology());
        }
        if(kind.equals("decorative_pump"))return "Hand pump: "+pump(level,pos,player)+" mB moved from lower tank to upper tank (max 1000/click)";
        if(kind.equals("decorative_piston"))return "Cargo pusher: "+push(level,pos,entity.getBlockState().getValue(DecorativeBlock.FACING),player)+" items moved from rear crate/pallet to front (max 16/click)";
        if(kind.equals("decorative_fan")&&held.is(Items.CHARCOAL))return "Particulate filter removed "+filter(level,pos,held,true)+" PM; one charcoal is spent only when air is filtered";
        if(kind.equals("decorative_vent")&&held.is(CivitasRegistries.REMEDIATION_REAGENT.get()))return "Gas scrubber removed "+filter(level,pos,held,false)+" SOX/NOX; ecology recovers gradually";
        entity.change(false);
        return switch(kind){
            case "decorative_gear"->"Gear regulator: redstone "+entity.signal()+"/15; sneak-click changes strength";
            case "decorative_fan"->"Fan "+(entity.enabled?"running":"stopped")+"; use charcoal for a manual particulate-filter service";
            case "decorative_vent"->"Vent "+(entity.enabled?"open":"closed")+"; use remediation reagent for a manual gas-scrubber service";
            default->"Utility updated";
        };
    }
    public static double filter(ServerLevel level,BlockPos pos,ItemStack reagent,boolean particulate){
        if(reagent.isEmpty()||!(particulate?reagent.is(Items.CHARCOAL):reagent.is(CivitasRegistries.REMEDIATION_REAGENT.get())))return 0;
        var runtime=WorldRuntime.get(level);var cell=runtime.state().cells.get(CellPos.fromBlock(pos.getX(),pos.getZ()));if(cell==null)return 0;
        double removed=0,budget=25;
        for(var pollutant:particulate?new Pollutant[]{Pollutant.PM}:new Pollutant[]{Pollutant.SOX,Pollutant.NOX}){
            double amount=Math.min(budget,cell.get(pollutant));cell.add(pollutant,-amount);removed+=amount;budget-=amount;
        }
        if(removed>0){reagent.shrink(1);runtime.dirty();}return removed;
    }
    public static int pump(ServerLevel level,BlockPos pos,Player player){
        var from=pos.below();var to=pos.above();if(!permitted(level,pos,player)||!permitted(level,from,player)||!permitted(level,to,player))return 0;
        if(!(level.getBlockEntity(from) instanceof BulkTankBlockEntity source)||!(level.getBlockEntity(to) instanceof BulkTankBlockEntity destination))return 0;
        var offer=source.drain(1000,FluidAction.SIMULATE);int amount=destination.fill(offer,FluidAction.SIMULATE);if(amount==0)return 0;
        var taken=source.drain(amount,FluidAction.EXECUTE);int accepted=destination.fill(taken,FluidAction.EXECUTE);
        if(accepted<taken.getAmount())source.fill(taken.copyWithAmount(taken.getAmount()-accepted),FluidAction.EXECUTE);
        return accepted;
    }
    private static CargoBlockEntity crate(ServerLevel level,BlockPos pos){
        String kind=BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath();
        return (kind.equals("cargo_crate")||kind.equals("pallet"))&&level.getBlockEntity(pos) instanceof CargoBlockEntity cargo&&cargo.available()?cargo:null;
    }
    public static int push(ServerLevel level,BlockPos pos,Direction facing,Player player){
        var from=pos.relative(facing.getOpposite());var to=pos.relative(facing);
        if(!permitted(level,pos,player)||!permitted(level,from,player)||!permitted(level,to,player))return 0;
        var source=crate(level,from);var destination=crate(level,to);if(source==null||destination==null)return 0;
        for(int slot=0;slot<source.handler.getSlots();slot++){
            var offered=source.handler.extractItem(slot,16,true);if(offered.isEmpty())continue;
            int amount=offered.getCount()-ItemHandlerHelper.insertItemStacked(destination.handler,offered,true).getCount();if(amount==0)continue;
            var taken=source.handler.extractItem(slot,amount,false);var remainder=ItemHandlerHelper.insertItemStacked(destination.handler,taken,false);
            if(!remainder.isEmpty())ItemHandlerHelper.insertItemStacked(source.handler,remainder,false);
            return taken.getCount()-remainder.getCount();
        }
        return 0;
    }
}
