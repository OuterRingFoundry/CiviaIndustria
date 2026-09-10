package com.civitasindustria.common.config;
import com.civitasindustria.domain.SimulationSettings;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class ServerConfig {
    private static final ModConfigSpec.Builder B=new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue EMISSION_INTERVAL=B.defineInRange("emissionInterval",100,20,12000);
    public static final ModConfigSpec.IntValue ENVIRONMENT_INTERVAL=B.defineInRange("environmentInterval",200,20,12000);
    public static final ModConfigSpec.IntValue MAX_CELLS=B.defineInRange("maxCellsPerStep",512,1,4096);
    public static final ModConfigSpec.IntValue MACHINE_BUDGET=B.defineInRange("maxMachinesPerStep",2048,1,100000);
    public static final ModConfigSpec.DoubleValue AIR_DECAY=rate("airDecay",.01), DIFFUSION=rate("airDiffusion",.12), WET=rate("wetDeposition",.08),
        RUNOFF=rate("waterRunoff",.08), SOIL_RECOVERY=rate("soilRecovery",.001), ECO_RECOVERY=rate("ecologyRecovery",.002), INJURY=rate("ecologyInjury",.025),
        WIND_BIAS=rate("windBias",.3), CARDINAL=rate("loadCardinalWeight",.35), DIAGONAL=rate("loadDiagonalWeight",.15);
    public static final ModConfigSpec.IntValue WIND_X=B.defineInRange("windX",1,-1,1);
    public static final ModConfigSpec.DoubleValue LOAD_HIGH=B.defineInRange("loadHigh",100.0,1,1000000), LOAD_SEVERE=B.defineInRange("loadSevere",150.0,1,1000000),
        LOAD_LIMIT=B.defineInRange("loadCommissioningLimit",180.0,1,1000000);
    public static final ModConfigSpec.IntValue MAINTENANCE_INTERVAL=B.defineInRange("maintenanceInterval",6000,200,72000);
    public static final ModConfigSpec.DoubleValue MAINTENANCE_BASE=B.defineInRange("maintenanceBase",1.0,0,100000),
        MAINTENANCE_EDGE=B.defineInRange("maintenancePerEdge",.25,0,100000), MAINTENANCE_AREA=B.defineInRange("maintenancePerCell",.0,0,100000);
    public static final ModConfigSpec.IntValue CREDIT_PER_INGOT=B.defineInRange("maintenanceCreditsPerIron",100,1,100000);
    public static final ModConfigSpec.LongValue WAREHOUSE_CAPACITY=B.defineInRange("warehouseCapacity",10000000L,1L,Long.MAX_VALUE/2);
    public static final ModConfigSpec.LongValue TANK_CAPACITY=B.defineInRange("tankCapacityMb",10000000L,1000L,Long.MAX_VALUE/2);
    public static final ModConfigSpec.LongValue CRATE_CAPACITY=B.defineInRange("crateCapacity",100000L,1L,1000000000L);
    public static final ModConfigSpec.IntValue TRANSFER_BATCH=B.defineInRange("freightTransferBatch",256,1,4096);
    public static final ModConfigSpec.IntValue CARGO_NORMAL=B.defineInRange("cargoNormalLimit",512,1,1000000);
    public static final ModConfigSpec.IntValue WARNING_TICKS=B.defineInRange("threatWarningTicks",12000,200,72000);
    public static final ModConfigSpec.IntValue RAID_CELL_BUDGET=B.defineInRange("raidCellBudget",24,1,24), RAID_BUDGET=B.defineInRange("raidBudget",40,1,40),
        GLOBAL_RAID_BUDGET=B.defineInRange("globalRaidBudget",80,1,80);
    public static final ModConfigSpec.DoubleValue THREAT_THRESHOLD=B.defineInRange("threatThreshold",100.0,1,1000000);
    public static final ModConfigSpec.BooleanValue THREATS=B.define("enableThreats",true), CORROSION=B.define("sampleAcidCorrosion",true);
    public static final ModConfigSpec.BooleanValue EXPOSURE=B.define("environmentalExposure",true);
    public static final ModConfigSpec.DoubleValue EXPOSURE_AQI=B.defineInRange("severeExposureAqi",500.0,1,10000000),
        EXPOSURE_ACID=B.defineInRange("severeExposureAcid",500.0,1,2000000),
        EXPOSURE_WATER=B.defineInRange("severeExposureWaterToxicity",500.0,1,1000000);
    public static final ModConfigSpec.IntValue COMMISSION_TICKS=B.defineInRange("commissioningTicks",200,20,72000);
    public static final ModConfigSpec.IntValue RAID_WAVE_SIZE=B.defineInRange("raidWaveSize",6,1,24), RAID_LIFETIME=B.defineInRange("raidLifetimeTicks",2400,200,6000);
    public static final ModConfigSpec.DoubleValue NATIVE_EXPOSURE=B.comment("Regional exposure per native airborne pollution unit per 200 ticks; physical filters reduce this input. Existing ecological injury recovers gradually.").defineInRange("nativePollutionExposure",0.01,0.0,1.0);
    public static final ModConfigSpec SPEC=B.build();
    private static ModConfigSpec.DoubleValue rate(String name,double value){return B.defineInRange(name,value,0,1);}
    public static SimulationSettings simulation(){return new SimulationSettings(AIR_DECAY.get(),DIFFUSION.get(),WET.get(),RUNOFF.get(),
        SOIL_RECOVERY.get(),ECO_RECOVERY.get(),INJURY.get(),WIND_BIAS.get(),WIND_X.get(),0,.00001,MAX_CELLS.get());}
}
