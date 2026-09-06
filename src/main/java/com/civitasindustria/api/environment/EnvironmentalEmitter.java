package com.civitasindustria.api.environment;
import com.civitasindustria.domain.*;
/** External adapters accumulate emissions; transport remains owned by the simulator. */
@FunctionalInterface
public interface EnvironmentalEmitter { void emit(CellPos cell,Pollutant pollutant,double amount); }
