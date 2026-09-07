package com.civitasindustria.domain;

/** Stateless generation-time region selection. Coordinates use floor division across zero. */
public final class MineralRegions {
    private MineralRegions() {}

    public static boolean contains(long seed, int blockX, int blockZ, int regionChunks, int activePercent, int salt) {
        if (regionChunks < 1 || regionChunks > 256 || activePercent < 0 || activePercent > 100)
            throw new IllegalArgumentException("Invalid mineral region size or percentage");
        int width = regionChunks * 16;
        long x = Math.floorDiv(blockX, width), z = Math.floorDiv(blockZ, width);
        long hash = mix(seed ^ mix(salt) ^ mix(x + 0x632be59bd9b4e019L) ^ mix(z + 0x9e3779b97f4a7c15L));
        return Long.remainderUnsigned(hash, 100) < activePercent;
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }
}
