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
    /** Reserved schema version; Phase 0 creates no SavedData. */
    public static final int DATA_VERSION = 1;

    public CivitasIndustria(IEventBus modBus, ModContainer container) {
        CivitasRegistries.register(modBus);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(CivitasCommands::register);
    }
}
