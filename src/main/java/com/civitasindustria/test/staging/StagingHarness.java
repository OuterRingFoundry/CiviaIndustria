package com.civitasindustria.test.staging;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;
/** Explicit disposable-server workload; excludes authenticated players and running trains. */
public final class StagingHarness {
    private static boolean started;private static int placed,cells,warmup,sampled;private static long begin,window;
    private static final double[] TIMES=new double[1200];
    public static void register(IEventBus bus){if(!Boolean.getBoolean("civitas.staging"))return;bus.addListener(StagingHarness::start);bus.addListener(StagingHarness::pre);bus.addListener(StagingHarness::post);}
    private static void start(ServerStartedEvent event){started=true;org.slf4j.LoggerFactory.getLogger(StagingHarness.class).info("CIVITAS SYNTHETIC STAGING: disposable opt-in workload begins");}
    private static void pre(ServerTickEvent.Pre event){begin=System.nanoTime();}
    private static void post(ServerTickEvent.Post event){
        if(!started)return;ServerLevel level=event.getServer().overworld();var runtime=WorldRuntime.get(level);
        if(placed<11000){
            for(int n=0;n<128&&placed<11000;n++,placed++){
                BlockPos pos=new BlockPos(placed%100,-60,placed/100);
                level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);
                if(placed<10000)level.setBlock(pos,CivitasRegistries.CONTENT.get("decorative_gear").get().defaultBlockState(),3);
                else{
                    level.setBlock(pos,Blocks.FURNACE.defaultBlockState(),3);
                    if(placed<10200&&level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace){furnace.setItem(0,new ItemStack(Items.RAW_IRON,64));furnace.setItem(1,new ItemStack(Items.COAL,64));}
                    runtime.machineChanged(pos);
                }
            }
            return;
        }
        if(cells<500){
            int x=cells%25,z=cells/25;level.setChunkForced(x*4+2,z*4+2,true);level.getChunk(x*4+2,z*4+2);
            var pos=new CellPos(x,z);runtime.emit(pos,Pollutant.SOX,500);runtime.emit(pos,Pollutant.W_INDUSTRIAL,500);runtime.emit(pos,Pollutant.W_TOXICITY,100);
            if(cells<20){
                var center=new BlockPos(120+(cells%10)*4,-60,(cells/10)*4);
                level.setBlock(center,CivitasRegistries.CONTENT.get("warehouse_controller").get().defaultBlockState(),3);
                for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++)if(a!=0||b!=0)level.setBlock(center.offset(a,0,b),CivitasRegistries.CONTENT.get("warehouse_casing").get().defaultBlockState(),3);
            }
            cells++;return;
        }
        if(warmup++<200){level.setWeatherParameters(0,12000,true,false);return;}
        if(sampled==0)window=System.nanoTime();
        TIMES[sampled++]=(System.nanoTime()-begin)/1_000_000.0;
        if(sampled<TIMES.length)return;
        started=false;Arrays.sort(TIMES);double average=Arrays.stream(TIMES).average().orElseThrow(),p95=TIMES[(int)(TIMES.length*.95)-1];
        var report=new com.google.gson.JsonObject();report.addProperty("scope","Synthetic Civitas workload; no authenticated clients or running trains");report.addProperty("decorations",10000);report.addProperty("machines",1000);report.addProperty("active_furnaces",200);report.addProperty("warehouse_controllers",20);report.addProperty("seeded_rain_cells",500);report.addProperty("measured_ticks",sampled);report.addProperty("mean_ms",average);report.addProperty("observed_tps",sampled/((System.nanoTime()-window)/1_000_000_000.0));report.addProperty("p95_ms",p95);report.addProperty("max_ms",TIMES[TIMES.length-1]);report.addProperty("runtime_active_cells",runtime.dirtyEntries());
        try{java.nio.file.Files.writeString(java.nio.file.Path.of("staging-report.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(java.io.IOException e){throw new RuntimeException(e);}
        org.slf4j.LoggerFactory.getLogger(StagingHarness.class).info("CIVITAS SYNTHETIC STAGING COMPLETE: {}",report);
        event.getServer().halt(false);
    }
}
