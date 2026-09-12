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
        bus.addListener(RuntimeEvents::breakBlock);
        bus.addListener(RuntimeEvents::placeBlock);
        bus.addListener(RuntimeEvents::save);
        bus.addListener(RuntimeEvents::logout);
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
            WorldRuntime.existing(level).ifPresent(runtime->runtime.chunkUnloaded(event.getChunk().getPos()));
    }

    private static void tick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            WorldRuntime.get(level).tick(level);
            if(level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)){ResidenceServices.tick(level);if(level.getGameTime()%1200==0){var economy=EconomySavedData.get(level);if(economy.registry.day(level.getGameTime()/24000,level.getSeed()))economy.setDirty();}}
        }
    }

    private static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
            ResidenceSavedData.get(player.serverLevel()).registry.stopObserving(player.getUUID());
    }

    private static void breakBlock(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event){
        if(!(event.getLevel() instanceof ServerLevel level))return;
        if(level.getBlockEntity(event.getPos()) instanceof com.civitasindustria.common.warehouse.CargoBlockEntity cargo&&cargo.hasContents()){
            event.setCanceled(true);
            event.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Empty cargo storage before dismantling."),true);return;
        }
        if(level.getBlockEntity(event.getPos()) instanceof com.civitasindustria.common.warehouse.BulkTankBlockEntity tank&&tank.hasContents()){
            event.setCanceled(true);return;
        }
        if(level.getBlockEntity(event.getPos()) instanceof com.civitasindustria.common.factory.FactoryBlockEntity factory&&factory.hasContents()){
            event.setCanceled(true);return;
        }
        if(level.getBlockEntity(event.getPos()) instanceof com.civitasindustria.common.workshop.RetainedMachine machine&&machine.retainsContents()){event.setCanceled(true);event.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("Empty machine contents before dismantling."),true);return;}
        WorldRuntime.get(level).machineChanged(event.getPos());
    }
    private static void placeBlock(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event){
        if(event.getLevel() instanceof ServerLevel level)WorldRuntime.get(level).machineChanged(event.getPos());
    }
    private static void save(LevelEvent.Save event){if(event.getLevel() instanceof ServerLevel level)WorldRuntime.existing(level).ifPresent(WorldRuntime::settleEmissions);}
    private static void stop(ServerStoppedEvent event) { WorldRuntime.clear();CargoMovement.clear();com.civitasindustria.common.threat.ThreatDirector.clear(); }
    private static void reload(AddReloadListenerEvent event) { event.addListener(com.civitasindustria.common.workshop.WorkshopRecipes.INSTANCE);event.addListener(EmissionRegistry.INSTANCE);event.addListener(com.civitasindustria.common.factory.FactoryRecipes.INSTANCE);event.addListener(com.civitasindustria.common.factory.CommissioningRequirements.INSTANCE); }
}
