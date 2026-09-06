package com.civitasindustria.common.config;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class ClientConfig {
    private static final ModConfigSpec.Builder B=new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue TINT=B.define("ecologicalTint",true),HAZE=B.define("environmentalHaze",true);
    public static final ModConfigSpec SPEC=B.build();
    private ClientConfig(){}
}
