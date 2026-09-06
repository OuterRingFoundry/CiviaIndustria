package com.civitasindustria.domain;
public final class ThreatState {
    public enum Phase { DORMANT, WARNING, ACTIVE, RECOVERY }
    public Phase phase=Phase.DORMANT;
    public long deadline;
    public void update(long now,boolean online,double pressure,double threshold,long warning,long recovery) {
        if(!online) { phase=Phase.DORMANT; deadline=0; return; }
        switch(phase) {
            case DORMANT -> { if(pressure>=threshold){phase=Phase.WARNING;deadline=now+warning;} }
            case WARNING -> { if(pressure<threshold){phase=Phase.DORMANT;}else if(now>=deadline){phase=Phase.ACTIVE;deadline=now+recovery;} }
            case ACTIVE -> { if(now>=deadline){phase=Phase.RECOVERY;deadline=now+recovery;} }
            case RECOVERY -> { if(now>=deadline)phase=Phase.DORMANT; }
        }
    }
}
