package com.civitasindustria;

import com.civitasindustria.common.command.CivitasCommands;
import com.civitasindustria.common.config.ClientConfig;
import com.civitasindustria.common.config.CommonConfig;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CivitasIndustria.MOD_ID)
public final class CivitasIndustria {
    public static final String MOD_ID = "civitas_industria";
    /** Shared with the domain snapshot codec. */
    public static final int DATA_VERSION = com.civitasindustria.domain.DataMigrationManager.VERSION;

    public CivitasIndustria(IEventBus modBus, ModContainer container) {
        CivitasRegistries.register(modBus);
        com.civitasindustria.test.staging.StagingHarness.register(NeoForge.EVENT_BUS);
        if(net.neoforged.fml.ModList.get().isLoaded("create")){
            com.civitasindustria.compat.create.CreateCargo.register(modBus);
            com.civitasindustria.compat.create.RailwayRestartChecks.register(NeoForge.EVENT_BUS);
            com.civitasindustria.compat.create.RailwayRouteChecks.register(NeoForge.EVENT_BUS);
        }
        com.civitasindustria.common.factory.MachineCommissioning.register(NeoForge.EVENT_BUS);
        modBus.addListener(com.civitasindustria.common.network.EnvironmentPayload::register);
        com.civitasindustria.platform.EcologyHooks.register(NeoForge.EVENT_BUS);
        com.civitasindustria.common.threat.ThreatDirector.register(NeoForge.EVENT_BUS);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(CivitasCommands::register);
        com.civitasindustria.platform.RuntimeEvents.register(NeoForge.EVENT_BUS);
        com.civitasindustria.platform.ParcelProtection.register(NeoForge.EVENT_BUS);
        com.civitasindustria.platform.CargoMovement.register(NeoForge.EVENT_BUS);
    }
}
