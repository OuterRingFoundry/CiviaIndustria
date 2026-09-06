package com.civitasindustria.domain;
import java.util.*;
public final class WorldState {
    public final NavigableMap<CellPos,CellData> cells=new TreeMap<>();
    public final NavigableMap<Long,CivicNode> nodes=new TreeMap<>();
    public final ParcelIndex parcels=new ParcelIndex();
    public final Map<IndustrialLoad.Chunk,Double> rawLoad=new HashMap<>();
    public static final class CivicNode {
        public enum Kind { CORE,RELAY,LOGISTICS,DEFENSE,MAINTENANCE }
        public final CellPos cell; public final UUID owner; public final Kind kind;
        public long credits; public int missedPayments;
        public CivicNode(CellPos cell,UUID owner,Kind kind){this.cell=cell;this.owner=owner;this.kind=kind;}
    }
}
