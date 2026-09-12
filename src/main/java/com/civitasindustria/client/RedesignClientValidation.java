package com.civitasindustria.client;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.economy.MarketMenu;
import com.civitasindustria.common.warehouse.*;
import com.civitasindustria.common.workshop.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Opt-in real rendering and client/server menu packet fixture. */
@EventBusSubscriber(modid="civitas_industria",value=Dist.CLIENT)
public final class RedesignClientValidation {
    private static int ticks;
    private static final BlockPos MARKET=new BlockPos(32,-60,32),CARGO=MARKET.east(2),WORK=MARKET.west(2);
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(RedesignClientValidation.class);
    private static void capture(Minecraft mc,String name){net.minecraft.client.Screenshot.grab(mc.gameDirectory,name+".png",mc.getMainRenderTarget(),m->LOG.info("Redesign capture: {}",m.getString()));}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("civitas.redesignValidation"))return;
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null)return;ticks++;
        if(ticks==1){mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);mc.options.guiScale().set(3);mc.resizeDisplay();}
        if(ticks==20)mc.getSingleplayerServer().execute(()->{
            var level=mc.getSingleplayerServer().overworld();var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=true;player.onUpdateAbilities();player.connection.teleport(32,-60,30,0,15);
            player.getInventory().clearContent();player.getInventory().setItem(0,new ItemStack(Items.DIAMOND,2));
            for(int x=-5;x<=5;x++)for(int z=-3;z<=3;z++)level.setBlock(MARKET.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),3);
            level.setBlock(MARKET,CivitasRegistries.CONTENT.get("market_counter").get().defaultBlockState(),3);
            level.setBlock(CARGO,CivitasRegistries.CONTENT.get("cargo_crate").get().defaultBlockState(),3);
            level.setBlock(WORK,CivitasRegistries.CONTENT.get("precision_workbench").get().defaultBlockState(),3);
            var box=(CargoBlockEntity)level.getBlockEntity(CARGO);box.handler.insertItem(0,new ItemStack(Items.IRON_INGOT,64),false);
            player.openMenu(new SimpleMenuProvider((id,inv,p)->new MarketMenu(id,inv,MARKET),Component.literal("Market counter")),MARKET);
        });
        if(ticks==100){
            if(!(mc.screen instanceof MarketScreen)||!(mc.player.containerMenu instanceof MarketMenu menu))throw new AssertionError("Market screen missing");
            mc.gameMode.handleInventoryButtonClick(menu.containerId,0);
        }
        if(ticks==140){
            if(!(mc.player.containerMenu instanceof MarketMenu menu)||menu.number(0)!=100||mc.player.getInventory().countItem(Items.DIAMOND)!=1)throw new AssertionError("Market deposit packet/data mismatch");
            capture(mc,"market-counter");LOG.info("CIVITAS REDESIGN MARKET PASS: native screen, deposit packet and synchronized balance");
        }
        if(ticks==160){var menu=(MarketMenu)mc.player.containerMenu;mc.gameMode.handleInventoryButtonClick(menu.containerId,1);}
        if(ticks==200){
            if(!(mc.player.containerMenu instanceof MarketMenu menu)||menu.number(0)!=0||mc.player.getInventory().countItem(Items.DIAMOND)!=2)throw new AssertionError("Market redemption packet/data mismatch");
            mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();CargoMenu.open(p,(CargoBlockEntity)p.level().getBlockEntity(CARGO));});
        }
        if(ticks==260){
            if(!(mc.screen instanceof CargoScreen)||!(mc.player.containerMenu instanceof CargoMenu m)||m.count(0)!=64)throw new AssertionError("Bulk storage menu/count sync");
            capture(mc,"bulk-storage");mc.gameMode.handleInventoryMouseClick(m.containerId,0,0,ClickType.QUICK_MOVE,mc.player);
        }
        if(ticks==300){
            if(!(mc.player.containerMenu instanceof CargoMenu m)||m.count(0)!=0||mc.player.getInventory().countItem(Items.IRON_INGOT)!=64)throw new AssertionError("Bulk shift-transfer packet conservation");
            LOG.info("CIVITAS REDESIGN CARGO PASS: native screen, count sync and shift-transfer packet");
            mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.openMenu((WorkshopEntity)p.level().getBlockEntity(WORK),WORK);});
        }
        if(ticks==360){if(!(mc.screen instanceof WorkshopScreen))throw new AssertionError("Workshop screen missing");capture(mc,"precision-workbench");}
        if(ticks==380){mc.player.closeContainer();mc.getSingleplayerServer().execute(()->mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst().connection.teleport(32,-59,27,0,25));}
        if(ticks==440){
            var manager=mc.getModelManager();for(var block:CivitasRegistries.CONTENT.values())for(var state:block.get().getStateDefinition().getPossibleStates())if(mc.getBlockRenderer().getBlockModel(state)==manager.getMissingModel())throw new AssertionError("Missing model "+state);
            for(var item:CivitasRegistries.ITEMS.getEntries())if(mc.getItemRenderer().getModel(new ItemStack(item.get()),mc.level,mc.player,0)==manager.getMissingModel())throw new AssertionError("Missing item model "+item.getId());
            capture(mc,"workshop-scene");LOG.info("CIVITAS REDESIGN CLIENT PASS: real screens, packets, models and captures");
        }
        if(ticks==480)mc.stop();
    }
}
