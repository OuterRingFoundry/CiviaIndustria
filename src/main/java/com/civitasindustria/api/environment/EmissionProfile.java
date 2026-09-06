package com.civitasindustria.api.environment;
import com.civitasindustria.domain.*;
import java.util.Map;
public record EmissionProfile(String target,double industrialLoad,Map<Pollutant,Double> emissions,String activeProperty) {
    public EmissionProfile {
        if(!Double.isFinite(industrialLoad)||industrialLoad<0||industrialLoad>100000)throw new IllegalArgumentException("Industrial load");
        emissions=Map.copyOf(emissions);
        for(double v:emissions.values())if(!Double.isFinite(v)||v<0||v>10000)throw new IllegalArgumentException("Emission rate");
    }
}
