package com.civitasindustria.test;

import com.civitasindustria.domain.*;
import com.civitasindustria.platform.*;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import java.nio.file.*;
import java.util.*;

/** Opt-in, isolated process fixture. Never enabled in normal server runs. */
public final class ResidenceRestartChecks {
    private static final String MODE = System.getProperty("civitas.residenceRestart", "");
    private static final Path FIXTURE = Path.of("residence-fixture.json");
    private static final UUID OWNER = UUID.fromString("a1f3aef8-9c7a-48b3-975b-c8e5a862d230");
    private static final UUID PARCEL = UUID.fromString("addf60e0-2c48-4a09-92c9-f74eb64b7914");
    private ResidenceRestartChecks() {}
    public static void register(IEventBus bus) {
        if (!MODE.isEmpty()) bus.addListener(ResidenceRestartChecks::start);
    }
    private static void start(ServerStartedEvent event) {
        try {
            if (!Set.of("write", "read", "verify").contains(MODE)) throw new IllegalArgumentException("Unknown residence fixture phase");
            var server = event.getServer();
            var overworld = server.overworld();
            var nether = Objects.requireNonNull(server.getLevel(Level.NETHER));
            var saved = ResidenceSavedData.get(overworld);
            if (saved != ResidenceSavedData.get(nether)) throw new AssertionError("Multiple residence authorities");
            if (MODE.equals("write")) {
                if (Files.exists(FIXTURE) || saved.registry.size() != 0) throw new IllegalStateException("Residence fixture requires an empty disposable registry");
                for (ServerLevel level : List.of(overworld, nether)) {
                    WorldRuntime.get(level).state().parcels.add(new Parcel(PARCEL, OWNER, -64, 0, -64, -1, 100, -1,
                            "restart-home", Set.of(), Set.of()));
                    WorldRuntime.get(level).dirty();
                }
                declare(overworld);
                declare(nether);
                long now = Math.max(20_000, overworld.getGameTime());
                server.getWorldData().overworldData().setGameTime(now);
                for (long time = now - ResidenceRegistry.QUALIFYING_TICKS; time <= now; time += 20)
                    saved.registry.observe(OWNER, time, true);
                var home = saved.registry.home(OWNER).orElseThrow();
                if (saved.registry.size() != 1 || !home.dimension().equals("minecraft:the_nether")
                        || !home.recentlyQualified(now)) throw new AssertionError("Initial residence qualification/replacement");
            } else {
                JsonObject fixture = JsonParser.parseString(Files.readString(FIXTURE)).getAsJsonObject();
                String previous = MODE.equals("read") ? "write" : "read";
                if (!fixture.get("phase").getAsString().equals(previous)) throw new AssertionError("Wrong fixture phase order");
                if (!Arrays.equals(Base64.getDecoder().decode(fixture.get("snapshot").getAsString()), saved.registry.encode()))
                    throw new AssertionError("Residence identity or activity changed across process restart");
                var home = saved.registry.home(OWNER).orElseThrow();
                if (!ResidenceServices.authorized(overworld, home)) throw new AssertionError("Persisted home lost parcel authority");
                if (MODE.equals("read")) {
                    if (!home.dimension().equals("minecraft:the_nether") || home.qualifyingTicks() != 12_000)
                        throw new AssertionError("Nether home or qualification missing");
                    declare(overworld);
                    if (saved.registry.size() != 1 || saved.registry.parcelCount("minecraft:the_nether", PARCEL) != 0)
                        throw new AssertionError("Post-restart cross-dimensional replacement");
                    long now = overworld.getGameTime();
                    saved.registry.observe(OWNER, now - 40, true);
                    saved.registry.observe(OWNER, now - 20, true);
                    saved.registry.observe(OWNER, now, true);
                    if (saved.registry.home(OWNER).orElseThrow().qualifyingTicks() != 40)
                        throw new AssertionError("Replacement did not reset activity");
                } else {
                    if (!home.dimension().equals("minecraft:overworld") || home.qualifyingTicks() != 40)
                        throw new AssertionError("Post-replacement state did not survive restart");
                    server.getWorldData().overworldData().setGameTime(overworld.getGameTime() + 200);
                    saved.registry.observe(OWNER, overworld.getGameTime(), true);
                    if (saved.registry.home(OWNER).orElseThrow().qualifyingTicks() != 40)
                        throw new AssertionError("Restart accrued an unobserved interval");
                }
            }
            saved.setDirty();
            JsonObject result = new JsonObject();
            result.addProperty("phase", MODE);
            result.addProperty("snapshot", Base64.getEncoder().encodeToString(saved.registry.encode()));
            Files.writeString(FIXTURE, new GsonBuilder().setPrettyPrinting().create().toJson(result));
            org.slf4j.LoggerFactory.getLogger(ResidenceRestartChecks.class).info(
                    "CIVITAS RESIDENCE RESTART PASS: phase={}, homes=1, scope=process persistence and cross-dimensional replacement", MODE);
            server.halt(false);
        } catch (Exception | AssertionError e) { throw new RuntimeException("Residence restart fixture failed in " + MODE, e); }
    }
    private static void declare(ServerLevel level) throws Exception {
        var player = FakePlayerFactory.get(level, new GameProfile(OWNER, "CIHomeRestart"));
        player.setPos(-.5, 64, -.5);
        int result = level.getServer().getCommands().getDispatcher().execute("ci residence declare", player.createCommandSourceStack());
        if (result != 1) throw new AssertionError("Residence declaration command failed");
    }
}
