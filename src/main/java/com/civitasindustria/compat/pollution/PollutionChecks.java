package com.civitasindustria.compat.pollution;

import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.CellPos;
import com.civitasindustria.domain.Pollutant;
import com.civitasindustria.platform.WorldRuntime;
import com.endertech.minecraft.mods.adpother.blocks.FilterFrame;
import com.endertech.minecraft.mods.adpother.init.Pollutants;
import com.endertech.minecraft.mods.adpother.pollution.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Direct native APIs, real inventories and datapack recipes; no client linkage. */
public final class PollutionChecks {
    private static net.minecraft.world.level.block.Block block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id)).orElseThrow();
    }
    public static void filtersAndDose(GameTestHelper h) {
        var level = h.getLevel();
        var filterPos = h.absolutePos(new BlockPos(3, 2, 3));
        var chimneyPos = filterPos.above();
        h.setBlock(new BlockPos(3, 1, 3), Blocks.FURNACE);
        level.setBlockAndUpdate(filterPos, block("adpother:iron_filter_frame").defaultBlockState());
        level.setBlockAndUpdate(chimneyPos, block("adchimneys:metal_chimney").defaultBlockState());
        var tile = (FilterFrame.BlockTile) level.getBlockEntity(filterPos);
        var carbon = Pollutants.BuiltIn.CARBON.get();
        var filter = (FilterFrame) block("adpother:iron_filter_frame");
        if (filter.fill(tile, carbon, 8) != 0) throw new AssertionError("Empty filter removed pollution");
        tile.getInputInventory().insertItem(0, new ItemStack(Items.OAK_LEAVES, 2), false);
        h.runAfterDelay(5, () -> {
            // Route through the actual filter interface used by native chimney exhaust.
            if (carbon.pumpEntitiesAt(level, filterPos, 8) != 8) throw new AssertionError("Native filter refused exhaust");
            if (tile.getFilterMaterial().getCount() != 1 || !tile.getByproduct().is(Items.BLACK_DYE)
                    || tile.getByproduct().getCount() != 1) throw new AssertionError("Filter failed exact payment/byproduct");
            var saved = tile.saveWithFullMetadata(level.registryAccess());
            tile.loadWithComponents(saved, level.registryAccess());
            if (tile.getFilterMaterial().getCount() != 1 || tile.getByproduct().getCount() != 1)
                throw new AssertionError("Filter inventory changed on reload");
            var cell = CellPos.fromBlock(filterPos.getX(), filterPos.getZ());
            var runtime = WorldRuntime.get(level);
            var info = WorldData.getChunkPollution(level, filterPos).getOrCreateInfoFor(carbon);
            int previous = info.getQuantity();
            double[] before = PollutionBridge.stock(level, cell);
            info.setQuantity(previous + 100);
            double[] after = PollutionBridge.stock(level, cell);
            if (after[0] - before[0] != 100) throw new AssertionError("Native counter sampling mismatch");
            double pm = runtime.cell(cell).get(Pollutant.PM);
            runtime.cell(cell).degradation = .5;
            PollutionBridge.expose(level, runtime, cell);
            double dose = after[0] * ServerConfig.NATIVE_EXPOSURE.get() * ServerConfig.ENVIRONMENT_INTERVAL.get() / 200.0;
            if (Math.abs(runtime.cell(cell).get(Pollutant.PM) - pm - dose) > 1e-6)
                throw new AssertionError("Regional dose accounting mismatch");
            info.setQuantity(previous);
            if (runtime.cell(cell).degradation != .5) throw new AssertionError("Air cleanup erased lasting injury");
            if (!PollutionBridge.ownsAir(level.getBlockEntity(filterPos.below())))
                throw new AssertionError("Furnace emissions would be counted twice");
            System.out.println("CIVITAS NATIVE POLLUTION: paid filter, byproduct, reload and regional dose passed");
            level.setBlockAndUpdate(filterPos.below(), block("civitas_industria:factory_controller").defaultBlockState());
            var factory = level.getBlockEntity(filterPos.below());
            var profile = new com.civitasindustria.api.environment.EmissionProfile("civitas_industria:factory_controller",
                40, java.util.Map.of(Pollutant.PM, 8.0), "active");
            PollutionBridge.factoryEmission(level, factory, profile, 1);
            h.runAfterDelay(240, () -> {
                if (!tile.getFilterMaterial().isEmpty() || tile.getByproduct().getCount() != 2)
                    throw new AssertionError("Factory delayed exhaust did not route into the paid native filter: material="
                        + tile.getFilterMaterial() + " byproduct=" + tile.getByproduct());
                System.out.println("CIVITAS NATIVE EXHAUST: factory queue to physical filter and chimney passed");
                h.succeed();
            });
        });
    }
    public static void recipes(GameTestHelper h) {
        var manager = h.getLevel().getRecipeManager();
        for (String id : new String[]{"civitas_industria:integration/steel_sheet", "civitas_industria:integration/hemp_filter_paper",
                "civitas_industria:integration/sulfur_reagent", "civitas_industria:integration/standard_components",
                "immersiveengineering:metalpress/plate_iron", "adpother:gold_filter_frame", "adchimneys:metal_pump"}) {
            var recipe = manager.byKey(ResourceLocation.parse(id)).orElseThrow(() -> new AssertionError("Missing integration recipe " + id));
            if (recipe.value().getResultItem(h.getLevel().registryAccess()).isEmpty())
                throw new AssertionError("Empty recipe result " + id);
        }
        var press = manager.byKey(ResourceLocation.parse("immersiveengineering:metalpress/plate_iron")).orElseThrow().value();
        if (!press.getResultItem(h.getLevel().registryAccess()).is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:iron_sheet"))))
            throw new AssertionError("IE metal press did not use canonical Create sheet");
        var pump = manager.byKey(ResourceLocation.parse("adchimneys:metal_pump")).orElseThrow().value();
        var mechanicalPump = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:mechanical_pump")));
        if (pump.getIngredients().stream().noneMatch(i -> i.test(mechanicalPump)))
            throw new AssertionError("Native pump recipe bypassed Create progression");
        var reagent = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("civitas_industria:remediation_reagent")));
        if (Pollutants.BuiltIn.SULFUR.get().getFilterMaterials().getCapacityFor(reagent) != 32)
            throw new AssertionError("Native sulfur filter does not accept Civitas reagent");
        System.out.println("CIVITAS FOUR CORE: recipe loading and shared filter material passed");
        h.succeed();
    }
}
