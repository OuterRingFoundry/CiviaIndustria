package com.civitasindustria.client;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Explicit opt-in development client smoke gate; never enabled in assembled packs. */
@EventBusSubscriber(modid="civitas_industria",value=Dist.CLIENT)
public final class ClientValidation {
    private static int ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("civitas.validation"))return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        ++ticks;
        if(ticks==20&&mc.getSingleplayerServer()!=null)mc.getSingleplayerServer().execute(()->{
            var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().getFirst();var level=player.serverLevel();int index=0;
            var runtime=com.civitasindustria.platform.WorldRuntime.get(level);
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var cell=runtime.cell(new com.civitasindustria.domain.CellPos(x,z));java.util.Arrays.fill(cell.pollutants,0);cell.vegetationHealth=cell.aquaticHealth=cell.biodiversity=cell.cropSuitability=1;cell.degradation=0;runtime.dirty();}
            for(var block:com.civitasindustria.common.registry.CivitasRegistries.CONTENT.values()){
                var pos=new net.minecraft.core.BlockPos((index%6)*2,-60,(index/6)*2);level.setBlock(pos,block.get().defaultBlockState(),3);index++;
            }
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();player.connection.teleport(15,-52,18,140,30);
        });
        if(ticks==220&&mc.getSingleplayerServer()!=null)mc.getSingleplayerServer().execute(()->{
            var level=mc.getSingleplayerServer().overworld();var runtime=com.civitasindustria.platform.WorldRuntime.get(level);
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var cell=runtime.cell(new com.civitasindustria.domain.CellPos(x,z));runtime.emit(new com.civitasindustria.domain.CellPos(x,z),com.civitasindustria.domain.Pollutant.SOX,1000);cell.vegetationHealth=cell.aquaticHealth=cell.biodiversity=cell.cropSuitability=.1;cell.degradation=.9;runtime.dirty();}
        });
        if(ticks==200||ticks==650){
            org.slf4j.LoggerFactory.getLogger(ClientValidation.class).info("CIVITAS CLIENT WORLD READY: dimension={}, ticks={}",mc.level.dimension().location(),ticks);
            org.slf4j.LoggerFactory.getLogger(ClientValidation.class).info("Client grass tint: {}",Integer.toHexString(net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(mc.level,new net.minecraft.core.BlockPos(0,-61,0))));
            net.minecraft.client.Screenshot.grab(mc.gameDirectory,mc.getMainRenderTarget(),message->org.slf4j.LoggerFactory.getLogger(ClientValidation.class).info("Client capture: {}",message.getString()));
        }
        if(ticks==690)mc.stop();
    }
}
