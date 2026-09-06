package com.civitasindustria.domain;

import java.util.Arrays;
public final class CellData {
    public enum Civilization { WILDERNESS, FRONTIER, CIVILIZED }
    public enum Ecology { PRISTINE, HEALTHY, STRESSED, POLLUTED, SEVERELY_DEGRADED, INDUSTRIAL_WASTELAND, RECOVERING }
    public final double[] pollutants = new double[Pollutant.values().length];
    public double vegetationHealth=1, biodiversity=1, cropSuitability=1, aquaticHealth=1, degradation;
    public double threatPressure, acidPrecursorLoad;
    public long lastEnvironmentUpdate, lastEcologyUpdate, lastThreatUpdate;
    public Civilization civilization = Civilization.WILDERNESS;
    public double get(Pollutant p) { return pollutants[p.ordinal()]; }
    public void add(Pollutant p,double value) { pollutants[p.ordinal()]=bounded(get(p)+value); }
    public static double bounded(double value) {
        if(!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite environmental value");
        return Math.clamp(value,0,1_000_000);
    }
    public double aqi() { return get(Pollutant.PM)+get(Pollutant.TOX)*2+get(Pollutant.SOX)+get(Pollutant.NOX); }
    public double waterQuality() { return 100/(1+(get(Pollutant.W_ORGANIC)+get(Pollutant.W_INDUSTRIAL)+get(Pollutant.W_ACIDITY)+get(Pollutant.W_TOXICITY))/40); }
    public double smogPotential() { return Math.sqrt(get(Pollutant.PM)*get(Pollutant.NOX)); }
    public Ecology ecology() {
        if(degradation<.03) return aqi()<1?Ecology.PRISTINE:Ecology.HEALTHY;
        if(aqi()<5 && waterQuality()>90) return Ecology.RECOVERING;
        if(degradation<.2) return Ecology.STRESSED;
        if(degradation<.45) return Ecology.POLLUTED;
        if(degradation<.7) return Ecology.SEVERELY_DEGRADED;
        return Ecology.INDUSTRIAL_WASTELAND;
    }
    public boolean pristine() {
        return civilization==Civilization.WILDERNESS && threatPressure==0 && degradation==0
            && Arrays.stream(pollutants).allMatch(v->v==0) && vegetationHealth==1 && aquaticHealth==1 && biodiversity==1 && cropSuitability==1;
    }
}
