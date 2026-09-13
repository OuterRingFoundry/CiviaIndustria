package com.outerringfoundry.mapsurvey;

import com.outerringfoundry.mapsurvey.mixin.ServerDistanceAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.minecraft.commands.Commands.*;

@Mod(value = MapSurvey.ID, dist = Dist.CLIENT)
public final class MapSurvey {
    public static final String ID = "aew_map_survey";
    private static final Logger LOG = LoggerFactory.getLogger(ID);
    private static SurveySession session;
    private static RecoveryJournal journal;
    private static boolean recovered;
    private static String message = "Ready. The area follows your player.";

    public MapSurvey(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (ignored, parent) -> new SurveyScreen(parent));
        NeoForge.EVENT_BUS.addListener(MapSurvey::commands);
        NeoForge.EVENT_BUS.addListener(MapSurvey::tick);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> stop("Disconnected"));
        NeoForge.EVENT_BUS.addListener((GameShuttingDownEvent e) -> stop("Game closing"));
    }
    private static void commands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(literal("mapsurvey")
            .executes(c -> { Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new SurveyScreen(null))); return 1; })
            .then(literal("start").then(argument("radius_chunks", integer(2, 32))
                .then(argument("seconds", integer(5, 120)).executes(c -> {
                    boolean ok = start(getInteger(c, "radius_chunks"), getInteger(c, "seconds"));
                    chat(message); return ok ? 1 : 0;
                }))))
            .then(literal("stop").executes(c -> { stop("Stopped"); chat(message); return 1; }))
            .then(literal("status").executes(c -> { chat(status()); return 1; })));
    }
    private static void recover() {
        if (recovered) return;
        var mc = Minecraft.getInstance();
        if (mc.options == null) return;
        recovered = true;
        journal = new RecoveryJournal(mc.gameDirectory.toPath().resolve("config/aew-map-survey-recovery.txt"));
        try {
            if (journal.exists()) {
                int old = mc.options.renderDistance().get();
                int restored = journal.recoveredDistance(old);
                mc.options.renderDistance().set(restored);
                mc.options.save();
                journal.clear();
                message = "Recovered settings from the previous survey.";
                LOG.info("Recovered survey settings: {} -> {}", old, restored);
            }
        } catch (IOException e) {
            message = "Recovery file needs attention; see the client log. Survey disabled.";
            LOG.error("Preserved invalid/unwritable config/aew-map-survey-recovery.txt", e);
        }
    }
    public static boolean start(int radius, int seconds) {
        recover();
        var mc = Minecraft.getInstance();
        if (session != null) { message = "A survey is active. Stop it before starting another."; return false; }
        if (mc.level == null || mc.player == null || mc.getConnection() == null) {
            message = "Join a world before starting a survey."; return false;
        }
        if (journal == null || journal.exists()) {
            message = "Unresolved recovery file; see the client log. Survey disabled."; return false;
        }
        try {
            int previous = mc.options.renderDistance().get();
            int target = SurveySession.target(radius, serverLimit(), clientMaximum());
            var next = new SurveySession(mc.level, previous, target, seconds, System.nanoTime());
            journal.begin(previous, target);
            session = next;
            mc.options.renderDistance().set(target);
            if (mc.options.renderDistance().get() != target) {
                stop("Another mod rejected the requested distance"); return false;
            }
            mc.options.broadcastOptions();
            message = "Mapping at " + target + " chunks (about " + target * 16 + " blocks radius) for " + seconds + "s.";
            LOG.info("Survey started: requested={}, applied={}, serverLimit={}, seconds={}", radius, target, serverLimit(), seconds);
            return true;
        } catch (IOException | IllegalArgumentException e) {
            message = "Could not start survey: " + e.getMessage();
            LOG.error("Survey start refused before changing settings", e);
            return false;
        }
    }
    private static void tick(ClientTickEvent.Post event) {
        recover();
        if (session == null) return;
        var mc = Minecraft.getInstance();
        var reason = session.check(mc.level, mc.options.renderDistance().get(), System.nanoTime());
        if (reason != SurveySession.End.NONE) stop(switch (reason) {
            case EXPIRED -> "Timer finished";
            case CONTEXT_CHANGED -> "World changed";
            case SETTING_CHANGED -> "Your new render distance was kept";
            default -> "Stopped";
        });
    }
    public static void stop(String reason) {
        if (session == null) return;
        var mc = Minecraft.getInstance();
        var previous = session;
        session = null;
        int distance = previous.restore(mc.options.renderDistance().get());
        mc.options.renderDistance().set(distance);
        // Persist the restored/manual setting before removing recovery evidence.
        mc.options.save();
        try {
            // Options.save logs IO failures instead of throwing; retain recovery unless persisted.
            var optionsFile = mc.gameDirectory.toPath().resolve("options.txt");
            boolean persisted;
            try (var lines = java.nio.file.Files.lines(optionsFile)) {
                persisted = lines.anyMatch(line -> line.equals("renderDistance:" + distance));
            }
            if (persisted) journal.clear();
            else LOG.error("Restored options were not saved; retaining survey recovery record");
        }
        catch (IOException e) { LOG.error("Could not clear survey recovery record", e); }
        message = reason + ". Render distance: " + distance + " chunks.";
        LOG.info(message);
    }
    public static int serverLimit() {
        var connection = Minecraft.getInstance().getConnection();
        return connection == null ? 0 : ((ServerDistanceAccessor) connection).aewSurvey$getServerDistance();
    }
    public static int clientMaximum() { return Runtime.getRuntime().maxMemory() >= 1_000_000_000L ? 32 : 16; }
    public static boolean active() { return session != null; }
    public static String status() {
        if (session == null) return message;
        return session.secondsLeft(System.nanoTime()) + "s left | requested " + session.applied()
                + " chunks | effective limit " + Math.min(session.applied(), serverLimit());
    }
    public static boolean hasMap() { return ModList.get().isLoaded("xaeroworldmap"); }
    private static void chat(String text) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.displayClientMessage(Component.literal("[Map Survey] " + text), false);
    }
}
