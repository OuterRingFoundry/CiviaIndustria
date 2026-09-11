package com.outerringfoundry.mapsurvey;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Only compiled with -PsurveyValidation. Requires the disposable localhost test server. */
@EventBusSubscriber(modid = MapSurvey.ID, value = Dist.CLIENT)
public final class SurveyClientValidation {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("survey-validation");
    private static final BlockPos TARGET = new BlockPos(80, -60, 0);
    private static int stage, ticks;
    private static long began = System.nanoTime();
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("aew.surveyValidation")) return;
        var mc = Minecraft.getInstance();
        if (System.nanoTime() - began > 180_000_000_000L) throw new AssertionError("Survey client validation timed out at stage " + stage);
        if (stage == 8) {
            if (mc.level != null || MapSurvey.active()) return;
            require(mc.options.renderDistance().get() == 2, "disconnect restores baseline");
            require(!mc.gameDirectory.toPath().resolve("config/aew-map-survey-recovery.txt").toFile().exists(), "recovery journal removed");
            LOG.info("SURVEY CLIENT PASS: recovered startup, GUI start, server cap, unseen chunk, block update, Xaero screen, timeout, manual edit, disconnect");
            stage = 9; mc.stop(); return;
        }
        if (stage >= 9 || mc.level == null || mc.player == null) return;
        ticks++;
        if (stage == 0 && ticks >= 80) {
            require(mc.getSingleplayerServer() == null, "real dedicated server connection");
            require(mc.options.renderDistance().get() == 2, "startup recovered persisted temporary setting");
            require(MapSurvey.serverLimit() == 8, "server advertised 8 chunks");
            require(!mc.level.getChunkSource().hasChunk(5, 0), "test chunk initially outside client range");
            require(MapSurvey.hasMap(), "AEW Xaero present");
            mc.setScreen(new SurveyScreen(null));
            stage = 1; ticks = 0;
        } else if (stage == 1 && ticks == 10) {
            capture("survey-controls.png");
            Button start = (Button) mc.screen.children().stream().filter(c -> c instanceof Button b && b.getMessage().getString().equals("Start survey")).findFirst().orElseThrow();
            start.onPress();
            require(MapSurvey.active(), "GUI starts survey");
            require(mc.options.renderDistance().get() == 8, "requested 16 capped at server 8");
            require(!MapSurvey.start(6, 5), "repeat start refused");
            mc.setScreen(null);
            LOG.info("SURVEY REQUEST PASS: GUI request 16 capped to 8; baseline 2");
            stage = 2; ticks = 0;
        } else if (stage == 2 && mc.level.getChunkSource().hasChunk(5, 0) && mc.level.getBlockState(TARGET).is(Blocks.OBSIDIAN)) {
            LOG.info("SURVEY UNSEEN CHUNK PASS: chunk (5,0) arrived without moving player: {}", mc.player.blockPosition());
            require(Math.abs(mc.player.getX()) < 16 && Math.abs(mc.player.getZ()) < 16, "player stayed near origin");
            mc.player.connection.sendCommand("setblock 80 -60 0 minecraft:emerald_block");
            stage = 3; ticks = 0;
        } else if (stage == 3 && mc.level.getBlockState(TARGET).is(Blocks.EMERALD_BLOCK)) {
            LOG.info("SURVEY LIVE UPDATE PASS: distant tracked block changed from obsidian to emerald");
            openXaero();
            stage = 4; ticks = 0;
        } else if (stage == 4 && ticks == 100) {
            require(mc.screen != null && mc.screen.getClass().getName().equals("xaero.map.gui.GuiMap"), "actual Xaero map screen");
            capture("xaero-survey-map.png");
            LOG.info("SURVEY XAERO SCREEN PASS: fullscreen map rendered");
            stage = 5;
        } else if (stage == 5 && !MapSurvey.active()) {
            require(mc.options.renderDistance().get() == 2, "timer restored baseline while map screen open");
            LOG.info("SURVEY TIMEOUT PASS: restored 2 chunks");
            mc.setScreen(null);
            require(MapSurvey.start(6, 5), "manual edit survey starts");
            mc.options.renderDistance().set(3);
            stage = 6; ticks = 0;
        } else if (stage == 6 && ticks >= 2) {
            require(!MapSurvey.active() && mc.options.renderDistance().get() == 3, "manual edit preserved");
            mc.options.renderDistance().set(2);
            mc.options.save();
            mc.player.connection.sendCommand("mapsurvey start 6 5");
            require(MapSurvey.active(), "local command starts short survey");
            stage = 7; ticks = 0;
        } else if (stage == 7 && !MapSurvey.active()) {
            require(mc.options.renderDistance().get() == 2, "5 second timeout restores");
            require(MapSurvey.start(6, 5), "disconnect survey starts");
            mc.getConnection().getConnection().disconnect(Component.literal("Survey test finished"));
            stage = 8;
        }
    }
    private static void openXaero() {
        try {
            Class<?> sessionClass = Class.forName("xaero.map.WorldMapSession");
            Object session = sessionClass.getMethod("getCurrentSession").invoke(null);
            Object processor = sessionClass.getMethod("getMapProcessor").invoke(session);
            Screen screen = (Screen) Class.forName("xaero.map.gui.GuiMap").getConstructor(Screen.class, Screen.class,
                    Class.forName("xaero.map.MapProcessor"), net.minecraft.world.entity.Entity.class)
                    .newInstance(null, null, processor, Minecraft.getInstance().player);
            Minecraft.getInstance().setScreen(screen);
        } catch (ReflectiveOperationException e) { throw new AssertionError("Xaero screen failed", e); }
    }
    private static void capture(String name) {
        var mc = Minecraft.getInstance();
        net.minecraft.client.Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), msg -> LOG.info("SCREENSHOT {}: {}", name, msg.getString()));
    }
    private static void require(boolean condition, String label) { if (!condition) throw new AssertionError(label); }
}
