package com.civitasindustria.domain;

/** Stable 64-block cell coordinates. Floor division is essential west/south of zero. */
public record CellPos(int x, int z) implements Comparable<CellPos> {
    public static final int BLOCKS = 64;
    public static CellPos fromBlock(int x, int z) { return new CellPos(Math.floorDiv(x, BLOCKS), Math.floorDiv(z, BLOCKS)); }
    public static CellPos fromChunk(int x, int z) { return new CellPos(Math.floorDiv(x, 4), Math.floorDiv(z, 4)); }
    public CellPos offset(int dx, int dz) { return new CellPos(Math.addExact(x, dx), Math.addExact(z, dz)); }
    @Override public int compareTo(CellPos p) { int c = Integer.compare(x,p.x); return c != 0 ? c : Integer.compare(z,p.z); }
}
