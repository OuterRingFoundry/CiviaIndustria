package com.civitasindustria.compat.pollution;

import com.civitasindustria.api.environment.EmissionProfile;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.CellPos;
import com.civitasindustria.domain.Pollutant;
import com.civitasindustria.platform.WorldRuntime;
import com.endertech.minecraft.mods.adpother.AdPother;
import com.endertech.minecraft.mods.adpother.emissions.Emissions;
import com.endertech.minecraft.mods.adpother.pollution.WorldData;
import com.endertech.minecraft.mods.adpother.sources.Emitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.HashMap;
import java.util.Map;

/** Loaded only with the pinned AdPother API. Native stock is an exposure dose,
 * not another copy of the machine emission rate. Filters reduce that stock upstream.
 * Reads at most 16 native chunk counters per regional step; never scans blocks. */
public final class PollutionBridge {
    private static final Map<EmissionProfile, Emitter> FACTORY_SOURCES = new HashMap<>();
    private PollutionBridge() {}

    public static boolean ownsAir(BlockEntity machine) {
        if (machine == null) return false;
        var id = BuiltInRegistries.BLOCK.getKey(machine.getBlockState().getBlock());
        return id.toString().equals("civitas_industria:factory_controller")
            || id.toString().equals("create:crushing_wheel")
            || AdPother.getInstance().emitters.get(machine.getBlockState()).isPresent();
    }

    public static void factoryEmission(ServerLevel level, BlockEntity machine, EmissionProfile profile, double elapsed) {
        if (machine == null || !profile.target().equals("civitas_industria:factory_controller") || elapsed <= 0) return;
        Emitter source = FACTORY_SOURCES.computeIfAbsent(profile, p -> {
            var emissions = Emissions.of().carbon(p.emissions().getOrDefault(Pollutant.PM, 0.0).floatValue())
                .sulfur(p.emissions().getOrDefault(Pollutant.SOX, 0.0).floatValue());
            return new Emitter(null, Emitter.Properties.tile("").id(p.target()).emissions(emissions));
        });
        // AdPother persists fractional amounts and uses its own delayed exhaust routing,
        // filter payment, pressure and chimney APIs. No immediate unfiltered fallback.
        source.emitAt(level, machine.getBlockPos(), (float) elapsed);
    }

    public static void clearSources() { FACTORY_SOURCES.clear(); }

    public static double[] stock(ServerLevel level, CellPos cell) {
        double particulate = 0, sulfur = 0;
        for (int x = 0; x < 4; x++) for (int z = 0; z < 4; z++) {
            int cx = cell.x() * 4 + x, cz = cell.z() * 4 + z;
            if (level.getChunkSource().getChunkNow(cx, cz) == null) continue;
            var pollution = WorldData.getChunkPollution(level, new BlockPos(cx * 16, 0, cz * 16));
            for (var info : pollution.getInfos().toList()) {
                int quantity = Math.max(0, info.getQuantity());
                switch (BuiltInRegistries.BLOCK.getKey(info.getPollutant()).toString()) {
                    case "adpother:carbon", "adpother:dust" -> particulate += quantity;
                    case "adpother:sulfur" -> sulfur += quantity;
                    default -> { }
                }
            }
        }
        return new double[]{particulate, sulfur};
    }

    public static void expose(ServerLevel level, WorldRuntime runtime, CellPos cell) {
        double[] stock = stock(level, cell);
        double factor = ServerConfig.NATIVE_EXPOSURE.get() * ServerConfig.ENVIRONMENT_INTERVAL.get() / 200.0;
        if (stock[0] > 0) runtime.emit(cell, Pollutant.PM, stock[0] * factor);
        if (stock[1] > 0) runtime.emit(cell, Pollutant.SOX, stock[1] * factor);
    }

    public static void discover(ServerLevel level, WorldRuntime runtime, CellPos cell) {
        double[] stock = stock(level, cell);
        if (stock[0] > 0 || stock[1] > 0) runtime.nativePollutionChanged(cell);
    }
}
