package com.civitasindustria.platform;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
public final class EcologyHooks {
    private static final TagKey<Block> SENSITIVE=TagKey.create(Registries.BLOCK,ResourceLocation.parse("civitas_industria:acid_sensitive"));
    private static final TagKey<Block> RESISTANT=TagKey.create(Registries.BLOCK,ResourceLocation.parse("civitas_industria:acid_resistant"));
    public static void register(IEventBus bus){bus.addListener(EcologyHooks::crop);bus.addListener(EcologyHooks::fish);}
    private static CellData cell(ServerLevel level,BlockPos pos){return WorldRuntime.get(level).state().cells.get(CellPos.fromBlock(pos.getX(),pos.getZ()));}
    private static void crop(CropGrowEvent.Pre event){
        if(event.getLevel() instanceof ServerLevel level){var c=cell(level,event.getPos());
            if(c!=null&&level.random.nextDouble()>Math.max(.1,c.cropSuitability))event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }
    private static void fish(ItemFishedEvent event){
        if(event.getHookEntity().level() instanceof ServerLevel level){var c=cell(level,event.getHookEntity().blockPosition());
            if(c!=null&&level.random.nextDouble()>Math.max(.1,c.aquaticHealth))event.setCanceled(true);
        }
    }
    /** Exactly four random surface samples per processed active cell, and loaded chunks only. */
    public static void sample(ServerLevel level,CellPos region,CellData cell){
        if(!ServerConfig.CORROSION.get()||!level.isRaining()||cell.acidPrecursorLoad<ServerConfig.EXPOSURE_ACID.get())return;
        for(int n=0;n<4;n++){
            int x=region.x()*64+level.random.nextInt(64),z=region.z()*64+level.random.nextInt(64);
            if(!level.hasChunk(x>>4,z>>4))continue;
            BlockPos pos=new BlockPos(x,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1,z);
            if(!level.isRainingAt(pos.above())||level.random.nextInt(100)!=0)continue;
            var block=level.getBlockState(pos);if(!block.is(SENSITIVE)||block.is(RESISTANT)||level.getBlockEntity(pos)!=null)continue;
            // Only progressive copper oxidation: no disappearing construction or item inventories.
            if(block.getBlock() instanceof WeatheringCopper copper)copper.getNext(block).ifPresent(next->level.setBlock(pos,next,3));
        }
    }
}
