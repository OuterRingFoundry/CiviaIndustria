package com.civitasindustria.compat.create;

import com.google.gson.*;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.*;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;

/** Disposable shared-line contention test. Never registers a listener without explicit fixture opt-in. */
public final class SharedRailwayChecks {
    private static final String MODE = System.getProperty("civitas.sharedRailway", "");
    private static final Path RECORD = Path.of("shared-railway.json");
    private static JsonObject record; private static boolean active, released;
    private static int ticks, forced, placed, wait, stoppedTicks;
    public static void register(IEventBus bus) {
        if (MODE.isEmpty()) return;
        bus.addListener(SharedRailwayChecks::start); bus.addListener(SharedRailwayChecks::tick);
    }
    private static void start(ServerStartedEvent event) {
        try {
            if (MODE.equals("write")) {
                if (Files.exists(RECORD)) throw new IllegalStateException("Existing shared railway fixture");
                record = new JsonObject(); record.add("trains", new JsonArray());
            } else if (MODE.equals("read") || MODE.equals("verify")) record = JsonParser.parseString(Files.readString(RECORD)).getAsJsonObject();
            else throw new IllegalArgumentException("Shared railway mode");
            active = true;
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    private static BlockState block(String id) { return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).defaultBlockState(); }
    private static <T extends Comparable<T>> BlockState value(BlockState state, Property<T> property, String value) { return state.setValue(property, property.getValue(value).orElseThrow()); }
    private static BlockState value(BlockState state, String name, String value) { return value(state, state.getBlock().getStateDefinition().getProperty(name), value); }
    private static BlockPos point(int z) { return new BlockPos(2, -60, z); }
    private static void edgePoint(ServerLevel level, int z, boolean signal) {
        var pos = point(z); level.setBlock(pos, block(signal ? "create:track_signal" : "create:track_station"), 3);
        var tag = new CompoundTag(); tag.putIntArray("TargetTrack", new int[]{-2, 0, 0});
        tag.putBoolean("TargetDirection", false); tag.putBoolean("Ortho", true);
        var be = level.getBlockEntity(pos);
        if (signal) ((SignalBlockEntity) be).edgePoint.read(tag, level.registryAccess(), false);
        else ((StationBlockEntity) be).edgePoint.read(tag, level.registryAccess(), false);
        be.setChanged();
    }
    private static void schedule(Train train, String name) {
        var instruction = new DestinationInstruction(); instruction.getData().putString("Text", name);
        var entry = new ScheduleEntry(); entry.instruction = instruction;
        var schedule = new Schedule(); schedule.cyclic = false; schedule.entries.add(entry); train.runtime.setSchedule(schedule, false);
    }
    private static double z(Train train) { return train.carriages.getFirst().getLeadingPoint().getPosition(train.graph).z; }
    private static Train train(int n) { return Create.RAILWAYS.trains.get(UUID.fromString(record.getAsJsonArray("trains").get(n).getAsJsonObject().get("id").getAsString())); }
    private static long cargo(Train train) { return train.carriages.getFirst().storage.getAllItemStorages().values().stream().filter(s -> s instanceof MountedCargo).mapToLong(s -> ((MountedCargo) s).total()).sum(); }
    private static SignalBlockEntity signal(ServerLevel level, int z) { return (SignalBlockEntity) level.getBlockEntity(point(z)); }
    private static void tick(ServerTickEvent.Post event) {
        if (!active) return; var level = event.getServer().overworld();
        try {
            if (++ticks > 6000) throw new AssertionError("Shared railway timed out");
            if (MODE.equals("write")) {
                if (forced < 159) { int x = forced % 3 - 1, z = forced / 3 - 2; level.setChunkForced(x, z, true); level.getChunk(x, z); forced++; return; }
                if (placed < 833) {
                    for (int n = 0; n < 64 && placed < 833; n++, placed++) {
                        var pos = new BlockPos(0, -60, placed - 32); level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
                        level.setBlock(pos, value(block("create:track"), "shape", "zo"), 3);
                    }
                    return;
                }
                if (wait++ == 0) { for (int z : new int[]{128, 320, 512}) edgePoint(level, z, true); edgePoint(level, 400, false); edgePoint(level, 700, false); return; }
                if (wait < 80) return;
                if (record.getAsJsonArray("trains").isEmpty()) {
                    for (int z : new int[]{128, 320, 512}) if (signal(level, z).getSignal() == null) throw new AssertionError("Physical signal absent from graph");
                    ((StationBlockEntity) level.getBlockEntity(point(400))).getStation().name = "CI_SHARED";
                    ((StationBlockEntity) level.getBlockEntity(point(700))).getStation().name = "CI_EXIT";
                    for (int n = 0; n < 2; n++) {
                        long quantity = 5_000_000_000L + n;
                        var train = RailwayRouteChecks.assembleAt(level, new BlockPos(0, -59, n == 0 ? 220 : 20), n, quantity);
                        var details = new JsonObject(); details.addProperty("id", train.id.toString()); details.addProperty("quantity", quantity);
                        record.getAsJsonArray("trains").add(details); schedule(train, "CI_SHARED");
                    }
                    Create.RAILWAYS.markTracksDirty(); return;
                }
            }
            var lead = train(0); var follower = train(1);
            if (lead == null || follower == null || lead.derailed || follower.derailed) throw new AssertionError("Train missing or derailed");
            if (z(lead) - z(follower) < 8) throw new AssertionError("Shared-line trains overlapped");
            for (int n = 0; n < 2; n++) if (cargo(train(n)) != record.getAsJsonArray("trains").get(n).getAsJsonObject().get("quantity").getAsLong()) throw new AssertionError("Shared railway cargo changed");
            if (ticks % 100 == 0) org.slf4j.LoggerFactory.getLogger(SharedRailwayChecks.class).info("SHARED progress mode={} lead={} follower={} speed={} waiting={} red={}", MODE, z(lead), z(follower), follower.speed, follower.navigation.waitingForSignal, signal(level, 320).getState());
            boolean queued = lead.getCurrentStation() != null && z(lead) > 390 && z(follower) <= 321 && z(follower) > 300
                && Math.abs(follower.speed) < .001 && follower.navigation.waitingForSignal != null && signal(level, 320).getState() == SignalBlockEntity.SignalState.RED;
            if (MODE.equals("write")) {
                stoppedTicks = queued ? stoppedTicks + 1 : 0;
                if (stoppedTicks < 60) return;
                record.addProperty("queuedTicksBeforeRestart", stoppedTicks); record.addProperty("queuedPosition", z(follower));
            } else if (MODE.equals("read")) {
                if (!released) {
                    if (!queued) { if (ticks > 200) throw new AssertionError("Signal/station queue did not survive restart"); return; }
                    if (++stoppedTicks < 40) return;
                    record.addProperty("queuedTicksAfterRestart", stoppedTicks); schedule(lead, "CI_EXIT"); released = true;
                    org.slf4j.LoggerFactory.getLogger(SharedRailwayChecks.class).info("SHARED released leading train after persisted red-signal queue"); return;
                }
                if (lead.getCurrentStation() == null || follower.getCurrentStation() == null || z(lead) < 690 || z(follower) < 390) return;
                record.addProperty("resumedFollowerPosition", z(follower));
            } else {
                if (ticks < 60) return;
                if (lead.getCurrentStation() == null || follower.getCurrentStation() == null || z(lead) < 690 || z(follower) < 390) throw new AssertionError("Final station state changed on restart");
            }
            Files.writeString(RECORD, new GsonBuilder().setPrettyPrinting().create().toJson(record));
            active = false; Create.RAILWAYS.markTracksDirty();
            org.slf4j.LoggerFactory.getLogger(SharedRailwayChecks.class).info("CIVITAS SHARED RAILWAY PASS: phase={}, trains=2", MODE); event.getServer().halt(false);
        } catch (Exception | AssertionError e) {
            active = false; org.slf4j.LoggerFactory.getLogger(SharedRailwayChecks.class).error("CIVITAS SHARED RAILWAY FAILED", e); event.getServer().halt(false);
        }
    }
}
