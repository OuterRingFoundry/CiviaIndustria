package com.civitasindustria.domain;

/** Biome-preserving palettes, shared with deterministic visual regression checks. */
public final class PollutionVisuals {
    private PollutionVisuals() {}
    public static int tint(int original, double injury, boolean water) {
        double value = Math.clamp(injury, 0, 1);
        int dry = water ? 0x617f68 : 0xaea16b;
        int damaged = water ? 0x73724a : 0x8b7952;
        int wasteland = water ? 0x5a6250 : 0x747269;
        if (value < .4) return blend(original, dry, value / .4);
        if (value < .75) return blend(dry, damaged, (value - .4) / .35);
        return blend(damaged, wasteland, (value - .75) / .25);
    }
    private static int blend(int from, int to, double amount) {
        // Continuous slopes prevent visible palette jumps at degree boundaries.
        double t = amount * amount * (3 - 2 * amount);
        int result = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            int a = (from >> shift) & 255, b = (to >> shift) & 255;
            result |= (int)Math.round(a + (b - a) * t) << shift;
        }
        return result;
    }
}
