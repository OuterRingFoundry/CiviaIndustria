package com.civitasindustria.common.config;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class ClientConfig {
    private static final ModConfigSpec.Builder B=new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue TINT=B.define("ecologicalTint",true),HAZE=B.define("environmentalHaze",true);
    public static final ModConfigSpec.BooleanValue POLLUTION_HUD=B.define("pollutionIndicator",true);
    public static final ModConfigSpec.DoubleValue HAZE_STRENGTH=B.defineInRange("hazeStrength",.7,0,1);
    public static final ModConfigSpec.IntValue TINT_COLUMNS=B.defineInRange("tintColumnsPerTick",2,1,16), TINT_SECTIONS=B.defineInRange("tintSectionsPerTick",8,1,64);
    public static final ModConfigSpec SPEC=B.build();
    private ClientConfig(){}
}
