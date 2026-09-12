package com.civitasindustria.compat.createaddition;
import com.mrh0.createaddition.config.CommonConfig;
/** Keep mixed-mod conversion loops lossy even when the add-on's ratio is configured. */
public final class PowerRates {
    private static double ratio(){int stress=CommonConfig.MAX_STRESS.get();return stress<=0?0:(double)CommonConfig.FE_RPM.get()/stress;}
    public static double dynamoPerRpm(){return Math.min(8,Math.max(0,8*ratio()));}
    public static int motorCost(){return (int)Math.min(Integer.MAX_VALUE,Math.max(512,Math.ceil(1024*ratio()*Math.max(0,CommonConfig.ALTERNATOR_EFFICIENCY.get()))));}
}
