package com.civitasindustria.domain;

/** Rates apply per environmental interval. Validated at the configuration boundary. */
public record SimulationSettings(double airDecay,double diffusion,double wetDeposition,
        double waterRunoff,double soilRecovery,double ecologyRecovery,double injury,
        double windBias,int windX,int windZ,double epsilon,int maxCells) {
    public SimulationSettings {
        for(double v:new double[]{airDecay,diffusion,wetDeposition,waterRunoff,soilRecovery,ecologyRecovery,injury,windBias})
            if(!Double.isFinite(v)||v<0||v>1) throw new IllegalArgumentException("Rate outside [0,1]");
        if(Math.abs(windX)+Math.abs(windZ)>1||epsilon<=0||!Double.isFinite(epsilon)||maxCells<1||maxCells>100_000)
            throw new IllegalArgumentException("Invalid simulation limits");
    }
}
