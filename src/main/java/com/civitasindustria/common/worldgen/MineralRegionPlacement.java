package com.civitasindustria.common.worldgen;

import com.civitasindustria.CivitasIndustria;
import com.civitasindustria.domain.MineralRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runs only during feature placement; does not keep worlds, scan terrain, or consume random draws. */
public final class MineralRegionPlacement extends PlacementFilter {
    public static final MapCodec<MineralRegionPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.intRange(1, 256).fieldOf("region_chunks").forGetter(p -> p.regionChunks),
        Codec.intRange(0, 100).fieldOf("active_percent").forGetter(p -> p.activePercent),
        Codec.INT.fieldOf("salt").forGetter(p -> p.salt)
    ).apply(instance, MineralRegionPlacement::new));
    private static final DeferredRegister<PlacementModifierType<?>> TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, CivitasIndustria.MOD_ID);
    private static final java.util.function.Supplier<PlacementModifierType<MineralRegionPlacement>> TYPE = TYPES.register("mineral_region", () -> () -> CODEC);
    private final int regionChunks, activePercent, salt;

    public MineralRegionPlacement(int regionChunks, int activePercent, int salt) {
        if (regionChunks < 1 || regionChunks > 256 || activePercent < 0 || activePercent > 100)
            throw new IllegalArgumentException("Invalid mineral region size or percentage");
        this.regionChunks = regionChunks; this.activePercent = activePercent; this.salt = salt;
    }
    public static void register(IEventBus bus) { TYPES.register(bus); }
    @Override protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return MineralRegions.contains(context.getLevel().getSeed(), pos.getX(), pos.getZ(), regionChunks, activePercent, salt);
    }
    @Override public PlacementModifierType<?> type() { return TYPE.get(); }
}
