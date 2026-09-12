package com.civitasindustria.client;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.workshop.*;
import com.civitasindustria.common.factory.MachineCommissioning;
import com.civitasindustria.common.threat.*;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Explicit developer fixture; no effect in normal clients or packaged servers. */
@EventBusSubscriber(modid="civitas_industria",value=Dist.CLIENT)
public final class WorkshopClientValidation {
    private static int ticks;
    private static long raidCredits;
    private static volatile boolean raidChecked;
    private static final BlockPos WORK=new BlockPos(32,-60,32);
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(WorkshopClientValidation.class);
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("civitas.workshopValidation"))return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;ticks++;
        if(ticks==1)mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
        if(ticks==20)mc.getSingleplayerServer().execute(()->{
            var server=mc.getSingleplayerServer();var level=server.overworld();var player=server.getPlayerList().getPlayers().getFirst();
            level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.onUpdateAbilities();
            player.connection.teleport(34,-59,27,0,20);
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.setBlock(WORK.offset(x,-1,z),Blocks.IRON_BLOCK.defaultBlockState(),3);
            level.setBlock(WORK,CivitasRegistries.CONTENT.get("precision_workbench").get().defaultBlockState(),3);
            var work=(WorkshopEntity)level.getBlockEntity(WORK);MachineCommissioning.begin(work,new ItemStack(CivitasRegistries.CALIBRATION_KIT.get()));
            work.inventory.insertItem(0,new ItemStack(Items.IRON_INGOT,32),false);work.inventory.insertItem(1,new ItemStack(CivitasRegistries.CUTTING_INSERT.get()),false);for(int i=0;i<16;i++)work.power.receiveEnergy(1000,false);
            if(net.neoforged.fml.ModList.get().isLoaded("create")){
                var motor=WORK.east(3);level.setBlock(motor,CivitasRegistries.CONTENT.get("electric_motor").get().defaultBlockState().setValue(com.civitasindustria.compat.create.PowerBridgeBlock.FACING,net.minecraft.core.Direction.EAST),3);
                level.setBlock(motor.east(),CivitasRegistries.CONTENT.get("rotation_dynamo").get().defaultBlockState().setValue(com.civitasindustria.compat.create.PowerBridgeBlock.FACING,net.minecraft.core.Direction.WEST),3);
            }
            var target=WORK.south(6);var runtime=WorldRuntime.get(level);level.setBlock(target,CivitasRegistries.CONTENT.get("defense_node").get().defaultBlockState(),3);runtime.nodePlaced(target,player.getUUID(),WorldState.CivicNode.Kind.DEFENSE);runtime.state().nodes.get(target.asLong()).credits=1000;
            runtime.tick(level);var cell=CellPos.fromBlock(target.getX(),target.getZ());for(int i=0;i<4&&ThreatDirector.count()<3;i++)ThreatDirector.wave(level,cell);
            var raiders=level.getEntities(CivitasRegistries.RAIDER.get(),new net.minecraft.world.phys.AABB(-200,-64,-200,264,320,264),e->true);int i=0;
            for(var e:raiders){if(i>=3){e.discard();continue;}e.configureRole(i);e.setNoAi(true);e.moveTo(30+i*2,-60,36,180,0);e.setYHeadRot(180);e.setYBodyRot(180);i++;}
            if(i!=3)throw new AssertionError("Three raider role models unavailable");
        });
        if(ticks%20==0)mc.getSingleplayerServer().execute(()->{
            var level=mc.getSingleplayerServer().overworld();if(level.getBlockEntity(WORK) instanceof WorkshopEntity e)for(int i=0;i<2;i++)e.power.receiveEnergy(1024,false);
            if(net.neoforged.fml.ModList.get().isLoaded("create")&&level.getBlockEntity(WORK.east(3)) instanceof com.civitasindustria.compat.create.PowerBridgeEntity e)for(int i=0;i<8;i++)e.port.receiveEnergy(2048,false);
        });
        if(ticks==100){
            var manager=mc.getModelManager();for(var block:CivitasRegistries.CONTENT.values())for(var state:block.get().getStateDefinition().getPossibleStates())if(mc.getBlockRenderer().getBlockModel(state)==manager.getMissingModel())throw new AssertionError("Missing block model "+state);
            for(var item:CivitasRegistries.ITEMS.getEntries())if(mc.getItemRenderer().getModel(new ItemStack(item.get()),mc.level,mc.player,0)==manager.getMissingModel())throw new AssertionError("Missing item model "+item.getId());
            for(String part:java.util.List.of("workshop_chuck","workshop_drill","power_rotor"))if(manager.getModel(WorkshopRenderer.model(part))==manager.getMissingModel())throw new AssertionError("Missing machine animation "+part);
            LOG.info("CIVITAS WORKSHOP MODELS PASS: all states, items, spindle/rotor and three raider rigs loaded");
        }
        if(ticks==180)net.minecraft.client.Screenshot.grab(mc.gameDirectory,mc.getMainRenderTarget(),m->LOG.info("Workshop scene: {}",m.getString()));
        if(ticks==220)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();player.connection.teleport(32,-60,30,0,10);var e=(WorkshopEntity)player.level().getBlockEntity(WORK);player.openMenu(e,WORK);
        });
        if(ticks==280||ticks==360||ticks==440){
            if(!(mc.screen instanceof WorkshopScreen)||!(mc.player.containerMenu instanceof WorkshopMenu m))throw new AssertionError("Workshop GUI did not open");int mode=ticks==280?0:ticks==360?1:2;
            mc.gameMode.handleInventoryButtonClick(m.containerId,mode);
            mc.getSingleplayerServer().execute(()->{
                var level=mc.getSingleplayerServer().overworld();var e=(WorkshopEntity)level.getBlockEntity(WORK);e.inventory.setStackInSlot(2,ItemStack.EMPTY);
                var input=mode==0?Items.IRON_INGOT:net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("create:iron_sheet"));e.inventory.setStackInSlot(0,new ItemStack(input,32));
            });
        }
        if(ticks==320||ticks==400||ticks==480){
            int mode=ticks==320?0:ticks==400?1:2;if(!(mc.player.containerMenu instanceof WorkshopMenu m)||m.data.get(2)!=mode||m.data.get(0)<=0||m.data.get(3)!=0)throw new AssertionError("GUI operation/data sync failed "+mode);
            LOG.info("CIVITAS WORKSHOP GUI PASS: mode={}, energy={}, progress={}",mode,m.data.get(0),m.data.get(1));net.minecraft.client.Screenshot.grab(mc.gameDirectory,mc.getMainRenderTarget(),message->LOG.info("Workshop GUI: {}",message.getString()));
        }
        if(ticks==520){mc.player.closeContainer();mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst().connection.teleport(33,-59,30,0,10));}
        if(ticks==560)net.minecraft.client.Screenshot.grab(mc.gameDirectory,mc.getMainRenderTarget(),m->LOG.info("Raider silhouettes: {}",m.getString()));
        if(ticks==580)mc.getSingleplayerServer().execute(()->{
            var level=mc.getSingleplayerServer().overworld();var target=WORK.south(6);var runtime=WorldRuntime.get(level);raidCredits=runtime.state().nodes.get(target.asLong()).credits;
            var raiders=level.getEntities(CivitasRegistries.RAIDER.get(),new net.minecraft.world.phys.AABB(16,-64,16,63,0,63),e->e.role()==1);
            if(raiders.isEmpty())throw new AssertionError("Missing breaker for live sabotage");var breaker=raiders.getFirst();breaker.moveTo(target.getX()+.5,target.getY(),target.getZ()-1.0,0,0);breaker.setNoAi(false);
        });
        if(ticks==600)net.minecraft.client.Screenshot.grab(mc.gameDirectory,mc.getMainRenderTarget(),m->LOG.info("Live raider action: {}",m.getString()));
        if(ticks==660)mc.getSingleplayerServer().execute(()->{
            var level=mc.getSingleplayerServer().overworld();var target=WORK.south(6);long remaining=WorldRuntime.get(level).state().nodes.get(target.asLong()).credits;
            // At most one passive upkeep payment occurs in this 80-tick window;
            // a reduction of two or more requires the breaker's actual sabotage.
            if(raidCredits-remaining<2)throw new AssertionError("Live breaker did not sabotage indexed defense: "+raidCredits+" -> "+remaining);
            raidChecked=true;LOG.info("CIVITAS RAIDER ACTION PASS: live AI reached defense and paid {} credits",raidCredits-remaining);
        });
        if(ticks==700){if(!raidChecked)throw new AssertionError("Missing live raid action result");LOG.info("CIVITAS WORKSHOP CLIENT PASS: real menu packets, three modes, models, live raid action and captures; no GPU benchmark");mc.stop();}
    }
}
