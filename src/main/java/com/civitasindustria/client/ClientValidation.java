package com.civitasindustria.client;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Explicit opt-in development client smoke gate; never enabled in assembled packs. */
@EventBusSubscriber(modid="civitas_industria",value=Dist.CLIENT)
public final class ClientValidation {
    private static int ticks,cleanTint;
    private static boolean near(int a,int b){for(int shift:new int[]{0,8,16})if(Math.abs(((a>>shift)&255)-((b>>shift)&255))>2)return false;return true;}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("civitas.validation"))return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
        ++ticks;
        if(ticks==1){mc.options.hideGui=true;com.civitasindustria.common.config.ClientConfig.TINT.set(true);com.civitasindustria.common.config.ClientConfig.HAZE.set(true);}
        if(ticks==20&&mc.getSingleplayerServer()!=null)mc.getSingleplayerServer().execute(()->{
            var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().getFirst();var level=player.serverLevel();int index=0;
            var runtime=com.civitasindustria.platform.WorldRuntime.get(level);
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var cell=runtime.cell(new com.civitasindustria.domain.CellPos(x,z));java.util.Arrays.fill(cell.pollutants,0);cell.vegetationHealth=cell.aquaticHealth=cell.biodiversity=cell.cropSuitability=1;cell.degradation=0;runtime.dirty();}
            for(var block:com.civitasindustria.common.registry.CivitasRegistries.CONTENT.values()){
                var pos=new net.minecraft.core.BlockPos((index%6)*2,-60,(index/6)*2);level.setBlock(pos,block.get().defaultBlockState(),3);index++;
            }
            var factory=new net.minecraft.core.BlockPos(12,-60,4);
            level.setBlock(factory,com.civitasindustria.common.registry.CivitasRegistries.CONTENT.get("factory_controller").get().defaultBlockState().setValue(com.civitasindustria.common.factory.FactoryBlock.ACTIVE,true),3);
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.setBlock(factory.offset(x,-1,z),net.minecraft.world.level.block.Blocks.IRON_BLOCK.defaultBlockState(),3);
            var running=(com.civitasindustria.common.factory.FactoryBlockEntity)level.getBlockEntity(factory);running.beginCommissioning();
            running.inventory.insertItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.RAW_IRON,64),false);running.inventory.insertItem(1,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL,64),false);
            int slot=0;for(var item:new net.minecraft.world.item.Item[]{com.civitasindustria.common.registry.CivitasRegistries.CALIBRATION_KIT.get(),com.civitasindustria.common.registry.CivitasRegistries.PRECISION_COMPONENT.get(),com.civitasindustria.common.registry.CivitasRegistries.REMEDIATION_REAGENT.get()})player.getInventory().setItem(slot++,new net.minecraft.world.item.ItemStack(item));
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();player.connection.teleport(15,-52,18,140,30);
        });
        if(ticks==120){
            var manager=mc.getModelManager();
            for(var block:com.civitasindustria.common.registry.CivitasRegistries.CONTENT.values())for(var state:block.get().getStateDefinition().getPossibleStates()){
                var model=mc.getBlockRenderer().getBlockModel(state);if(model==manager.getMissingModel())throw new AssertionError("Missing block model: "+state);
                for(var direction:new net.minecraft.core.Direction[]{null,net.minecraft.core.Direction.UP,net.minecraft.core.Direction.DOWN,net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.WEST})for(var quad:model.getQuads(state,direction,net.minecraft.util.RandomSource.create(0)))if(quad.getSprite().contents().name().equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))throw new AssertionError("Missing material: "+state);
            }
            for(String name:DecorativeRenderer.PARTS)if(manager.getModel(DecorativeRenderer.model(name))==manager.getMissingModel())throw new AssertionError("Missing moving model: "+name);
            org.slf4j.LoggerFactory.getLogger(ClientValidation.class).info("CIVITAS MATERIAL MODELS PASS: all block states and six moving models");
        }
        if(ticks==900&&mc.getSingleplayerServer()!=null)mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst().connection.teleport(12,-55,-9,35,23));
        if(ticks==1000)mc.options.hideGui=false;
        if(ticks==220&&mc.getSingleplayerServer()!=null)mc.getSingleplayerServer().execute(()->{
            var level=mc.getSingleplayerServer().overworld();var runtime=com.civitasindustria.platform.WorldRuntime.get(level);
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var cell=runtime.cell(new com.civitasindustria.domain.CellPos(x,z));runtime.emit(new com.civitasindustria.domain.CellPos(x,z),com.civitasindustria.domain.Pollutant.SOX,1000);cell.vegetationHealth=cell.aquaticHealth=cell.biodiversity=cell.cropSuitability=.1;cell.degradation=.9;runtime.dirty();}
        });
        if(ticks==680)com.civitasindustria.common.config.ClientConfig.TINT.set(false);
        if(ticks==760)com.civitasindustria.common.config.ClientConfig.TINT.set(true);
        if(ticks==800&&mc.getSingleplayerServer()!=null)mc.getSingleplayerServer().execute(()->{
            var runtime=com.civitasindustria.platform.WorldRuntime.get(mc.getSingleplayerServer().overworld());
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var cell=runtime.cell(new com.civitasindustria.domain.CellPos(x,z));java.util.Arrays.fill(cell.pollutants,0);cell.vegetationHealth=cell.aquaticHealth=cell.biodiversity=cell.cropSuitability=1;cell.degradation=0;runtime.dirty();}
        });
        if(ticks==200||ticks==650||ticks==720||ticks==1100){
            int tint=net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(mc.level,new net.minecraft.core.BlockPos(0,-61,0));
            if(ticks==200){
                com.civitasindustria.common.config.ClientConfig.TINT.set(false);
                cleanTint=net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(mc.level,new net.minecraft.core.BlockPos(0,-61,0));
                com.civitasindustria.common.config.ClientConfig.TINT.set(true);
            }
            if(ticks==650&&tint==cleanTint)throw new AssertionError("Pollution did not change client tint");
            if(ticks==720&&tint!=cleanTint||ticks==1100&&!near(tint,cleanTint))throw new AssertionError("Disabled/recovered client tint remained stale: "+Integer.toHexString(tint));
            org.slf4j.LoggerFactory.getLogger(ClientValidation.class).info("CIVITAS CLIENT WORLD READY: dimension={}, ticks={}",mc.level.dimension().location(),ticks);
            org.slf4j.LoggerFactory.getLogger(ClientValidation.class).info("Client grass tint: {}",Integer.toHexString(net.minecraft.client.renderer.BiomeColors.getAverageGrassColor(mc.level,new net.minecraft.core.BlockPos(0,-61,0))));
            net.minecraft.client.Screenshot.grab(mc.gameDirectory,mc.getMainRenderTarget(),message->org.slf4j.LoggerFactory.getLogger(ClientValidation.class).info("Client capture: {}",message.getString()));
        }
        if(ticks==1140)mc.stop();
    }
}
