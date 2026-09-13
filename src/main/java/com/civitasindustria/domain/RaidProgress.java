package com.civitasindustria.domain;
/** Three separated waves. A failed spawn never counts as a defeated wave. */
public final class RaidProgress {
    public static final int WAVES=3;
    private int wave,initial;
    private long next;
    private boolean engaged,failed;
    public int wave(){return wave;}
    public int initial(){return initial;}
    public boolean failed(){return failed;}
    public void fail(){failed=true;}
    public boolean won(int alive){return !failed&&engaged&&wave==WAVES&&alive==0;}
    public boolean ready(long now,int alive){return !failed&&wave<WAVES&&alive==0&&now>=next;}
    public void spawned(long now,int count){next=now+100;if(count>0){wave++;initial=count;engaged=true;}}
}
