package com.civitasindustria.domain;

/** Shared, continuous environmental response. Degrees describe it; they do not add cliffs. */
public final class PollutionEffects {
    public enum Degree { CLEAN, LIGHT, MODERATE, HEAVY, SEVERE, EXTREME }
    private PollutionEffects() {}
    public static Degree degree(double severity) {
        return severity < .05 ? Degree.CLEAN : severity < .2 ? Degree.LIGHT
            : severity < .4 ? Degree.MODERATE : severity < .6 ? Degree.HEAVY
            : severity < .8 ? Degree.SEVERE : Degree.EXTREME;
    }
    public static double severity(CellData cell) {
        if (cell == null) return 0;
        double soil = (cell.get(Pollutant.SOIL_ACIDITY) + cell.get(Pollutant.SOIL_TOXICITY)) / 250;
        return Math.clamp(Math.max(cell.degradation, Math.max(cell.aqi() / 500,
            Math.max(soil, (1 - cell.waterQuality() / 100) * .75))), 0, 1);
    }
    /** A small clean-air allowance keeps ordinary trace pollution harmless. */
    public static double stress(double severity) { return Math.clamp((severity - .05) / .95, 0, 1); }
    public static double rate(double severity, double minimum) { return 1 - (1 - minimum) * stress(severity); }
    public static double cropRate(CellData cell, double minimum) {
        return cell == null ? 1 : Math.max(minimum, Math.min(cell.cropSuitability, rate(severity(cell), minimum)));
    }
    public static double vegetation(CellData cell) {
        return cell == null ? 0 : Math.clamp(Math.max(1 - cell.vegetationHealth, stress(severity(cell)) * .35), 0, 1);
    }
}
