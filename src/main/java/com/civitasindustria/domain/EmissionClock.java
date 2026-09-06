package com.civitasindustria.domain;
import java.util.HashMap;
import java.util.Map;
/** Elapsed loaded ticks preserve steady-state emission totals across bounded sampling batches. */
public final class EmissionClock {
    private final Map<Long,Long> last=new HashMap<>();
    public void add(long id,long now){last.putIfAbsent(id,now);}
    public void remove(long id){last.remove(id);}
    public double sample(long id,long now,int interval){
        Long before=last.put(id,now);
        return before==null?0:Math.max(0,now-before)/(double)interval;
    }
}
