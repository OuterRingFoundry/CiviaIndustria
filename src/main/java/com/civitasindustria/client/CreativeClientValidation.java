package com.civitasindustria.client;

import com.civitasindustria.common.registry.CivitasRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in real creative screen, search, item model and item acquisition checks. */
@EventBusSubscriber(modid="civitas_industria", value=Dist.CLIENT)
public final class CreativeClientValidation {
    private static int ticks;
    private static volatile boolean acquired;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CreativeClientValidation.class);
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("civitas.creativeValidation")) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
        ticks++;
        if (ticks == 1) {
            mc.options.hideGui = false;
            mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
        }
        if (ticks == 20) mc.getSingleplayerServer().execute(() -> {
            var player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
            player.onUpdateAbilities();
        });
        if (ticks == 80) {
            if (!mc.gameMode.hasInfiniteItems()) throw new AssertionError("Creative game mode did not synchronize");
            mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.level.enabledFeatures(), false));
            select(CivitasRegistries.INDUSTRY_TAB.get());
            var screen = screen();
            for (var item : CivitasRegistries.ITEMS.getEntries()) {
                if (screen.getMenu().items.stream().noneMatch(stack -> stack.is(item.get())))
                    throw new AssertionError("Registered item absent from creative screen: " + item.getId());
                var model = mc.getItemRenderer().getModel(new ItemStack(item.get()), mc.level, mc.player, 0);
                if (model == mc.getModelManager().getMissingModel()) throw new AssertionError("Missing creative item model: " + item.getId());
                for (var direction : new net.minecraft.core.Direction[]{null, net.minecraft.core.Direction.UP,
                        net.minecraft.core.Direction.DOWN, net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.SOUTH,
                        net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.WEST})
                    for (var quad : model.getQuads(null, direction, net.minecraft.util.RandomSource.create(0)))
                        if (quad.getSprite().contents().name().equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))
                            throw new AssertionError("Missing creative item material: " + item.getId());
            }
            LOG.info("CIVITAS CREATIVE CONTENT PASS: registered={}, screen={}, all models/materials present",
                    CivitasRegistries.ITEMS.getEntries().size(), screen.getMenu().items.size());
        }
        if (ticks == 100) {
            org.lwjgl.glfw.GLFW.glfwFocusWindow(mc.getWindow().getWindow());
            org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), 1200, 40);
        }
        if (ticks == 110) capture("industry-inventory");
        if (ticks == 140) search("precision");
        if (ticks == 180) {
            if (screen().getMenu().items.stream().noneMatch(s -> s.is(CivitasRegistries.CONTENT.get("precision_workbench").get().asItem())))
                throw new AssertionError("Industry search did not find the workbench");
            if (screen().getMenu().items.size() >= CivitasRegistries.ITEMS.getEntries().size()
                    || screen().getMenu().items.stream().anyMatch(s -> s.is(CivitasRegistries.CONTENT.get("civic_core").get().asItem())))
                throw new AssertionError("Industry search did not filter unrelated items");
            LOG.info("CIVITAS CREATIVE SEARCH PASS: industry precision search contains workbench, filtered={}", screen().getMenu().items.size());
            capture("industry-search");
        }
        if (ticks == 210) { select(CreativeModeTabs.searchTab()); search("rotation"); }
        if (ticks == 250) {
            if (net.neoforged.fml.ModList.get().isLoaded("create") && screen().getMenu().items.stream()
                    .noneMatch(s -> s.is(CivitasRegistries.CONTENT.get("rotation_dynamo").get().asItem())))
                throw new AssertionError("Global search did not find the dynamo");
            if (screen().getMenu().items.size() >= CreativeModeTabs.searchTab().getDisplayItems().size()
                    || screen().getMenu().items.stream().anyMatch(s -> s.is(Items.OAK_LOG)))
                throw new AssertionError("Global search did not filter unrelated items");
            LOG.info("CIVITAS CREATIVE SEARCH PASS: global rotation search, filtered={}", screen().getMenu().items.size());
            capture("global-search");
            mc.gameMode.handleCreativeModeItemAdd(new ItemStack(CivitasRegistries.CONTENT.get("precision_workbench").get()), 36);
        }
        if (ticks == 280) mc.getSingleplayerServer().execute(() -> {
            var player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            if (!player.getInventory().getItem(0).is(CivitasRegistries.CONTENT.get("precision_workbench").get().asItem()))
                throw new AssertionError("Creative acquisition packet did not reach the server inventory");
            acquired = true;
        });
        if (ticks == 300) {
            mc.player.closeContainer();
            mc.getSingleplayerServer().execute(() -> {
                var level = mc.getSingleplayerServer().overworld();
                level.setDayTime(6000); level.setWeatherParameters(6000, 0, false, false);
                var origin = new net.minecraft.core.BlockPos(32, -60, 32);
                for (int x=-9; x<=9; x++) for (int z=-6; z<=14; z++) {
                    level.setBlock(origin.offset(x,-1,z), net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(), 3);
                    for (int y=0; y<=5; y++) level.setBlock(origin.offset(x,y,z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
                }
                String[] gallery = {"electric_motor", "rotation_dynamo", "precision_workbench", "factory_controller", "remediation_station",
                        "civic_core", "civic_relay", "logistics_node", "defense_node", "maintenance_depot",
                        "cargo_crate", "market_counter", "cargo_loader", "cargo_unloader", "bulk_tank"};
                for (int i=0; i<gallery.length; i++) {
                    var block = CivitasRegistries.CONTENT.get(gallery[i]);
                    if (block != null) level.setBlock(origin.offset((i%5-2)*3, 0, (i/5)*3), block.get().defaultBlockState(), 3);
                }
                var center=origin.offset(0,0,11);
                for (int x=-1;x<=1;x++) for (int z=-1;z<=1;z++) level.setBlock(center.offset(x,0,z),
                        CivitasRegistries.CONTENT.get(x==0&&z==0?"warehouse_controller":x==0&&z==-1?"warehouse_port":"warehouse_casing").get().defaultBlockState(),3);
                var warehouse=(com.civitasindustria.common.warehouse.CargoBlockEntity)level.getBlockEntity(center);
                if (!warehouse.available()) throw new AssertionError("Gallery warehouse did not form");
                var port=(com.civitasindustria.common.warehouse.CargoBlockEntity)level.getBlockEntity(center.north());
                if (!port.handler.insertItem(0,new ItemStack(Items.IRON_INGOT,32),false).isEmpty() || warehouse.total()!=32)
                    throw new AssertionError("Gallery warehouse port did not share controller storage");
                LOG.info("CIVITAS CREATIVE WAREHOUSE PASS: 3x3 ring formed; port inserted 32 items into controller");
                var observer=level.getServer().getPlayerList().getPlayers().getFirst();
                observer.getAbilities().flying=true;
                observer.onUpdateAbilities();
                observer.connection.teleport(32,-53,22,0,30);
            });
        }
        if (ticks == 350) { mc.options.hideGui=true; mc.getToasts().clear(); }
        if (ticks == 390) capture("machine-gallery");
        if (ticks == 410) mc.getSingleplayerServer().execute(() ->
                mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst().connection.teleport(37,-57,36,30,30));
        if (ticks == 480) capture("warehouse-multiblock");
        if (ticks == 520) {
            if (!acquired) throw new AssertionError("No creative acquisition confirmation");
            LOG.info("CIVITAS CREATIVE CLIENT PASS: actual inventory screen, category/global searches and server acquisition; no GPU benchmark");
            mc.stop();
        }
    }
    private static CreativeModeInventoryScreen screen() {
        if (!(Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen screen))
            throw new AssertionError("Creative inventory closed unexpectedly");
        return screen;
    }
    private static void select(CreativeModeTab tab) {
        var screen = screen();
        for (int page = 0; page < 32 && !screen.getCurrentPage().getVisibleTabs().contains(tab); page++) {
            var next = screen.children().stream().filter(c -> c instanceof Button b && b.getMessage().getString().equals(">")).findFirst().orElseThrow();
            ((Button) next).onPress();
        }
        var page = screen.getCurrentPage();
        if (!page.getVisibleTabs().contains(tab)) throw new AssertionError("Creative tab not reachable");
        int column = page.getColumn(tab);
        double x = (screen.width - 195) / 2 + (tab.isAlignedRight() ? 195 - 27 * (7 - column) + 1 : column * 27) + 12;
        double y = (screen.height - 136) / 2 + (page.isTop(tab) ? -32 : 136) + 12;
        screen.mouseClicked(x, y, 0);
        screen.mouseReleased(x, y, 0);
    }
    private static void search(String query) {
        var field = screen().children().stream().filter(c -> c instanceof EditBox box && box.isVisible()).findFirst().orElseThrow();
        var box = (EditBox) field;
        box.setValue("");
        box.setFocused(true);
        // The real screen refreshes searches from charTyped, not EditBox.setValue.
        for (char character : query.toCharArray())
            if (!screen().charTyped(character, 0)) throw new AssertionError("Creative search refused typed input");
    }
    private static void capture(String label) {
        var mc = Minecraft.getInstance();
        org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), 1200, 40);
        net.minecraft.client.Screenshot.grab(mc.gameDirectory, label + ".png", mc.getMainRenderTarget(), message -> LOG.info("{}: {}", label, message.getString()));
    }
}
