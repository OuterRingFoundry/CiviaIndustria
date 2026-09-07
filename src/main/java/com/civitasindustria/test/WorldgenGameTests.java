package com.civitasindustria.test;

import com.civitasindustria.common.worldgen.MineralRegionPlacement;
import com.civitasindustria.domain.MineralRegions;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class WorldgenGameTests {
    @GameTest(template="empty") public static void mineralPlacementRegistry(GameTestHelper h) {
        var level = h.getLevel();
        var context = new PlacementContext(level, level.getChunkSource().getGenerator(), java.util.Optional.empty());
        String[] ores = {"iron", "copper", "gold"}; int[] salts = {101, 202, 303};
        for (int i = 0; i < ores.length; i++) {
            var feature = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                .get(ResourceLocation.parse("civitas_industria:regional_" + ores[i]));
            if (feature == null || !(feature.placement().getFirst() instanceof MineralRegionPlacement filter))
                throw new AssertionError("Data pack lacks mineral filter for " + ores[i]);
            var encoded = PlacementModifier.CODEC.encodeStart(JsonOps.INSTANCE, filter).getOrThrow();
            var restored = PlacementModifier.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
            int selected = 0;
            for (int x = -16; x < 16; x++) for (int z = -16; z < 16; z++) {
                var pos = new BlockPos(x * 256 + 255, -32, z * 256 + 255);
                var random = RandomSource.create(42);
                boolean actual = restored.getPositions(context, random, pos).count() == 1;
                if (actual) selected++;
                if (actual != MineralRegions.contains(level.getSeed(), pos.getX(), pos.getZ(), 16, 25, salts[i]))
                    throw new AssertionError("Registered placement changed seed/coordinates/config");
                if (random.nextLong() != RandomSource.create(42).nextLong()) throw new AssertionError("Region filter consumed feature randomness");
            }
            if (selected < 180 || selected > 330) throw new AssertionError("Implausible regional coverage: " + selected);
        }
        for (String invalid : new String[]{"{\"region_chunks\":0,\"active_percent\":25,\"salt\":1}", "{\"region_chunks\":16,\"active_percent\":101,\"salt\":1}"})
            if (MineralRegionPlacement.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(invalid)).result().isPresent())
                throw new AssertionError("Invalid worldgen settings accepted");
        h.succeed();
    }
}
