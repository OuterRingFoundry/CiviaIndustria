package com.civitasindustria.test;

import com.civitasindustria.domain.*;
import java.nio.file.*;
import java.util.*;

/** Reproducible chemistry experiment, not server timing or a player-economy acceptance test. */
public final class EcologyBalanceReport {
    private static final SimulationSettings SETTINGS = new SimulationSettings(.01, .12, .08, .08, .001, .002, .025, .3, 1, 0, .00001, 512);
    private static final CellPos ORIGIN = new CellPos(0, 0);
    private static final EnvironmentSimulator.Climate RAIN = new EnvironmentSimulator.Climate(true, 1, 64, true);
    public static void main(String[] args) throws Exception {
        var report = new LinkedHashMap<String, Object>();
        report.put("scope", "Pure domain model; one hour of uninterrupted production, then twelve hours without emissions. Continuous rain, level connected surface water, fixed 17x17 active cells; transport outside the active window remains stored and is included in total mass. No pollutant or injury reset.");
        report.put("environmentIntervalTicks", 200); report.put("settings", SETTINGS.toString());
        report.put("factory", scenario(2, .4)); report.put("sixteenFurnaces", scenario(6.4, 1.6));
        report.put("supplyComparison", Map.of("itemsPer200Ticks", 16, "factoryBlockEntities", 1, "furnaceBlockEntities", 16,
            "factoryRawLoad", 40, "furnaceRawLoad", 160, "coalPer16Items", 2,
            "note", "Equal theoretical iron-smelting throughput and fuel efficiency from current recipes/profiles; excludes transport, capital, IE power networks and player downtime. This does not measure CPU cost."));
        Path output = Path.of(args.length == 0 ? "pack/ecology-balance-results.json" : args[0]);
        Files.writeString(output, json(report) + "\n");
        System.out.println("Ecology balance experiment passed; report=" + output);
    }
    // Only emits this closed experiment's finite numbers, strings, lists and string-keyed maps.
    private static String json(Object value) {
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof String text) return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        if (value instanceof Map<?, ?> map) return "{" + map.entrySet().stream().map(e -> json(e.getKey()) + ":" + json(e.getValue())).collect(java.util.stream.Collectors.joining(",")) + "}";
        if (value instanceof List<?> list) return "[" + list.stream().map(EcologyBalanceReport::json).collect(java.util.stream.Collectors.joining(",")) + "]";
        throw new IllegalArgumentException("Unsupported report value");
    }
    private static Map<String, Object> scenario(double particulate, double sulfur) {
        Map<CellPos, CellData> cells = new TreeMap<>(); List<CellPos> active = new ArrayList<>();
        for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++) active.add(new CellPos(x, z));
        var simulator = new EnvironmentSimulator(); List<Object> samples = new ArrayList<>();
        int recoveryMinutes = -1; double peakInjury = 0, lastMass = Double.MAX_VALUE;
        for (int step = 1; step <= 4680; step++) {
            if (step <= 360) { var source = cells.computeIfAbsent(ORIGIN, ignored -> new CellData()); source.add(Pollutant.PM, particulate); source.add(Pollutant.SOX, sulfur); }
            simulator.step(cells, active, ignored -> RAIN, SETTINGS, step * 200L);
            var center = cells.getOrDefault(ORIGIN, new CellData());
            double mass = cells.values().stream().flatMapToDouble(c -> Arrays.stream(c.pollutants)).sum();
            if (!Double.isFinite(mass) || step > 360 && mass > lastMass + 1e-7) throw new AssertionError("Recovery created pollutant mass");
            lastMass = mass; peakInjury = Math.max(peakInjury, center.degradation);
            if (step > 360 && recoveryMinutes < 0 && Math.min(center.vegetationHealth, Math.min(center.aquaticHealth, center.biodiversity)) >= .95)
                recoveryMinutes = (int) Math.ceil((step - 360) / 6.0);
            if (step == 360 || step > 360 && Set.of(10, 30, 60, 120, 240, 480, 720).contains((step - 360) / 6) && (step - 360) % 6 == 0)
                samples.add(Map.of("minutesAfterShutdown", (step - 360) / 6, "aqi", center.aqi(), "waterQuality", center.waterQuality(),
                    "vegetationHealth", center.vegetationHealth, "aquaticHealth", center.aquaticHealth, "biodiversity", center.biodiversity,
                    "soilAcidity", center.get(Pollutant.SOIL_ACIDITY), "soilToxicity", center.get(Pollutant.SOIL_TOXICITY), "totalStoredPollutantMass", mass));
        }
        if (peakInjury <= .03) throw new AssertionError("Continuous industrial pollution did not injure ecology");
        return Map.of("particulatePer200Ticks", particulate, "sulfurPer200Ticks", sulfur, "peakDegradation", peakInjury,
            "minutesAfterShutdownUntilAllHealthAtLeast95Percent", recoveryMinutes, "samples", samples);
    }
}
