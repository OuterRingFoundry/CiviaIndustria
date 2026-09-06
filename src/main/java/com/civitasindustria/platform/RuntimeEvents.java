package com.civitasindustria.platform;

import com.civitasindustria.common.environment.EmissionRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Server lifecycle glue. Chunk callbacks only record positions; world reads wait for a tick. */
public final class RuntimeEvents {
    private RuntimeEvents() {}

    public static void register(IEventBus bus) {
        bus.addListener(RuntimeEvents::load);
        bus.addListener(RuntimeEvents::unload);
        bus.addListener(RuntimeEvents::chunkLoad);
        bus.addListener(RuntimeEvents::chunkUnload);
        bus.addListener(RuntimeEvents::tick);
        bus.addListener(RuntimeEvents::stop);
        bus.addListener(RuntimeEvents::reload);
    }

    private static void load(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) WorldRuntime.get(level);
    }

    private static void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) WorldRuntime.unload(level);
    }

    private static void chunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk)
            WorldRuntime.get(level).queueChunk(chunk.getPos());
    }

    private static void chunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level)
            WorldRuntime.get(level).chunkUnloaded(event.getChunk().getPos());
    }

    private static void tick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) WorldRuntime.get(level).tick(level);
    }

    private static void stop(ServerStoppedEvent event) { WorldRuntime.clear(); }
    private static void reload(AddReloadListenerEvent event) { event.addListener(EmissionRegistry.INSTANCE); }
}
