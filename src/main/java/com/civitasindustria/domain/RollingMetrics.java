package com.civitasindustria.domain;
public final class RollingMetrics {
    private final long[] seconds=new long[300], nanos=new long[300], calls=new long[300], maxima=new long[300];
    private long processed;
    public void record(long epochSecond,long duration,long entries){
        int slot=Math.floorMod(epochSecond,300);
        if(seconds[slot]!=epochSecond){seconds[slot]=epochSecond;nanos[slot]=calls[slot]=maxima[slot]=0;}
        nanos[slot]+=duration;calls[slot]++;maxima[slot]=Math.max(maxima[slot],duration);processed+=entries;
    }
    public double average(long now,int window){
        long n=0,c=0;
        for(int i=0;i<300;i++)if(seconds[i]>now-window&&seconds[i]<=now){n+=nanos[i];c+=calls[i];}
        return c==0?0:n/(double)c/1_000_000;
    }
    public double maximum(long now){long max=0;for(int i=0;i<300;i++)if(seconds[i]>now-300&&seconds[i]<=now)max=Math.max(max,maxima[i]);return max/1_000_000.0;}
    public long processed(){return processed;}
    public void reset(){java.util.Arrays.fill(seconds,Long.MIN_VALUE);java.util.Arrays.fill(nanos,0);java.util.Arrays.fill(calls,0);java.util.Arrays.fill(maxima,0);processed=0;}
}
