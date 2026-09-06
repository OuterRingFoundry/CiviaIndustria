package com.civitasindustria.domain;
import java.util.*;
public final class ParcelIndex {
    private final Map<UUID,Parcel> parcels=new HashMap<>();
    private final Map<IndustrialLoad.Chunk,Set<UUID>> chunks=new HashMap<>();
    public Collection<Parcel> all(){return Collections.unmodifiableCollection(parcels.values());}
    private Set<IndustrialLoad.Chunk> keys(Parcel p){
        Set<IndustrialLoad.Chunk> out=new HashSet<>();
        for(int x=Math.floorDiv(p.minX(),16);x<=Math.floorDiv(p.maxX(),16);x++)
            for(int z=Math.floorDiv(p.minZ(),16);z<=Math.floorDiv(p.maxZ(),16);z++)out.add(new IndustrialLoad.Chunk(x,z));
        return out;
    }
    public void add(Parcel p) {
        if(parcels.size()>=100_000||parcels.containsKey(p.id()))throw new IllegalArgumentException("Parcel limit or duplicate ID");
        var keys=keys(p);
        for(var key:keys)for(UUID id:chunks.getOrDefault(key,Set.of()))if(parcels.get(id).intersects(p))
            throw new IllegalArgumentException("Parcel overlaps an existing parcel");
        parcels.put(p.id(),p); for(var key:keys)chunks.computeIfAbsent(key,k->new HashSet<>()).add(p.id());
    }
    public void remove(UUID id) {
        Parcel p=parcels.remove(id);if(p==null)return;
        for(var key:keys(p)){Set<UUID> ids=chunks.get(key);ids.remove(id);if(ids.isEmpty())chunks.remove(key);}
    }
    public Optional<Parcel> at(int x,int y,int z) {
        for(UUID id:chunks.getOrDefault(new IndustrialLoad.Chunk(Math.floorDiv(x,16),Math.floorDiv(z,16)),Set.of())){
            Parcel p=parcels.get(id);if(p.contains(x,y,z))return Optional.of(p);
        }return Optional.empty();
    }
}
