package com.civitasindustria.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Reserved configuration scope; gameplay settings arrive with their owning systems. */
public final class CommonConfig {
    public static final ModConfigSpec SPEC = new ModConfigSpec.Builder().build();
    private CommonConfig() {}
}
