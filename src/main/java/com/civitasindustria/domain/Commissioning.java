package com.civitasindustria.domain;
public final class Commissioning {
    public enum Stage { UNCOMMISSIONED,COMMISSIONING,READY,DEGRADED }
    public Stage stage=Stage.UNCOMMISSIONED;
    public int elapsed;
    public boolean begin(boolean foundation,boolean loadAllowed){
        if(!foundation||!loadAllowed||stage==Stage.READY||stage==Stage.COMMISSIONING)return false;
        stage=Stage.COMMISSIONING;elapsed=0;return true;
    }
    public void update(boolean foundation,boolean loadAllowed,int ticks,int required){
        if(!foundation||!loadAllowed){if(stage!=Stage.UNCOMMISSIONED)stage=Stage.DEGRADED;elapsed=0;return;}
        if(stage==Stage.COMMISSIONING){elapsed=Math.min(required,elapsed+ticks);if(elapsed>=required)stage=Stage.READY;}
    }
}
