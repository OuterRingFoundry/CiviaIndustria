package com.civitasindustria.compat.civil;
import com.civitasindustria.common.registry.CivitasRegistries;
public final class CivilChecks {
    public static void verify(){var type=CivitasRegistries.RAIDER.get();if(!civil.registry.SpawnGateEntityRegistry.isWhitelist(type)||!civil.registry.MobFleeEntityRegistry.isWhitelist(type))throw new AssertionError("Civilis event spawn/flee exemption missing");}
}
