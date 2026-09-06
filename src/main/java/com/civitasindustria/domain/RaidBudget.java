package com.civitasindustria.domain;
import java.util.*;
/** Hard reservations precede entity creation; all bookkeeping uses immutable identifiers. */
public final class RaidBudget {
    public record Reservation(UUID raid,String dimension,CellPos cell){}
    private final int global,perRaid,perCell;
    private final Map<UUID,Reservation> entries=new HashMap<>();
    public RaidBudget(int global,int perRaid,int perCell){
        if(global<1||global>80||perRaid<1||perRaid>40||perCell<1||perCell>24)throw new IllegalArgumentException("Raid budgets");
        this.global=global;this.perRaid=perRaid;this.perCell=perCell;
    }
    public boolean reserve(UUID entity,UUID raid,String dimension,CellPos cell){
        if(entries.containsKey(entity)||entries.size()>=global)return false;
        int raidCount=0,cellCount=0;
        for(var e:entries.values()){if(e.raid().equals(raid))raidCount++;if(e.dimension().equals(dimension)&&e.cell().equals(cell))cellCount++;}
        if(raidCount>=perRaid||cellCount>=perCell)return false;
        entries.put(entity,new Reservation(raid,dimension,cell));return true;
    }
    public void release(UUID entity){entries.remove(entity);}
    public int size(){return entries.size();}
    public boolean contains(UUID id){return entries.containsKey(id);}
}
