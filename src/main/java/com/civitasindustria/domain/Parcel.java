package com.civitasindustria.domain;
import java.util.*;
public record Parcel(UUID id,UUID owner,int minX,int minY,int minZ,int maxX,int maxY,int maxZ,
                     String name,Set<UUID> trusted,Set<Flag> flags) {
    public enum Flag { BUILD,BREAK,INTERACT,CONTAINER,REDSTONE,PUBLIC_ACCESS,UTILITY_EASEMENT }
    public Parcel {
        if(minX>maxX||minY>maxY||minZ>maxZ||name==null||name.length()>64||trusted.size()>64)
            throw new IllegalArgumentException("Invalid parcel");
        if((long)maxX-minX>512||(long)maxZ-minZ>512||(long)maxY-minY>1024) throw new IllegalArgumentException("Parcel too large");
        trusted=Set.copyOf(trusted); flags=Set.copyOf(flags);
    }
    public boolean contains(int x,int y,int z) { return x>=minX&&x<=maxX&&y>=minY&&y<=maxY&&z>=minZ&&z<=maxZ; }
    public boolean intersects(Parcel p) { return minX<=p.maxX&&maxX>=p.minX&&minY<=p.maxY&&maxY>=p.minY&&minZ<=p.maxZ&&maxZ>=p.minZ; }
    public boolean allows(UUID user,Flag action) { return owner.equals(user)||trusted.contains(user)||flags.contains(action); }
}
