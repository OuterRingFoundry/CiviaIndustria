package com.civitasindustria.domain;

import java.util.*;
/** Authoritative compact storage. Keys must be validated canonical item identifiers by the adapter. */
public final class BulkInventory {
    private final int maxKeys;
    private final long capacity;
    private final NavigableMap<String,Long> contents=new TreeMap<>();
    public BulkInventory(int maxKeys,long capacity) {
        if(maxKeys<1||maxKeys>256||capacity<1||capacity>Long.MAX_VALUE/2) throw new IllegalArgumentException("Storage limits");
        this.maxKeys=maxKeys; this.capacity=capacity;
    }
    public long total() { long n=0; for(long v:contents.values()) n=Math.addExact(n,v); return n; }
    public long count(String key) { return contents.getOrDefault(key,0L); }
    public Map<String,Long> contents() { return Collections.unmodifiableMap(contents); }
    public int maxKeys() { return maxKeys; }
    public long capacity() { return capacity; }
    public long insert(String key,long requested,boolean simulate) {
        validate(key,requested);
        if(!contents.containsKey(key)&&contents.size()>=maxKeys) return 0;
        long accepted=Math.min(requested,capacity-total());
        if(accepted>0&&!simulate) contents.merge(key,accepted,Math::addExact);
        return accepted;
    }
    public long extract(String key,long requested,boolean simulate) {
        validate(key,requested); long removed=Math.min(requested,count(key));
        if(removed>0&&!simulate) { long left=count(key)-removed; if(left==0)contents.remove(key);else contents.put(key,left); }
        return removed;
    }
    public long transferTo(BulkInventory target,String key,long requested) {
        if(target==this) return 0;
        long amount=target.insert(key,extract(key,requested,true),true);
        if(amount>0) { target.insert(key,amount,false); extract(key,amount,false); }
        return amount;
    }
    private static void validate(String key,long amount) {
        if(key==null||!key.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")||key.length()>256||amount<0) throw new IllegalArgumentException("Invalid cargo");
    }
}
