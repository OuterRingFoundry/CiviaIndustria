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
            // Reproduce the native join-time permanent health reduction, then verify single ownership.
            boolean enabled=ServerConfig.ANIMAL_EFFECTS.get();
            try {
                var oldCow=net.minecraft.world.entity.EntityType.COW.create(level);
                oldCow.moveTo(filterPos.getX(),filterPos.getY(),filterPos.getZ(),0,0);
                WorldData.getEntityPollution(oldCow).getOrCreateInfoFor(carbon).setQuantity(100000);
                ServerConfig.ANIMAL_EFFECTS.set(false);
                com.endertech.minecraft.mods.adpother.events.EntityEvents.onCreatureJoinWorld(new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(oldCow,level));
                if(oldCow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue()>=10)throw new AssertionError("Native health reduction fixture was not active");
                var newCow=net.minecraft.world.entity.EntityType.COW.create(level);
                newCow.moveTo(filterPos.getX(),filterPos.getY(),filterPos.getZ(),0,0);
                WorldData.getEntityPollution(newCow).getOrCreateInfoFor(carbon).setQuantity(100000);
                ServerConfig.ANIMAL_EFFECTS.set(true);
                for(int join=0;join<2;join++)com.endertech.minecraft.mods.adpother.events.EntityEvents.onCreatureJoinWorld(new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(newCow,level));
                if(newCow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getBaseValue()!=10)throw new AssertionError("Native join stacked a permanent base-health penalty");
                oldCow.discard();newCow.discard();
                System.out.println("CIVITAS NATIVE ANIMAL HEALTH: permanent native reduction replaced by reversible regional modifier");
            } finally { ServerConfig.ANIMAL_EFFECTS.set(enabled); }
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
    private static net.minecraft.world.item.Item item(String path){return BuiltInRegistries.ITEM.get(ResourceLocation.parse(path.contains(":")?path:"civitas_industria:"+path));}
    private static void processRecipes(GameTestHelper h){
        var level=h.getLevel();var manager=level.getRecipeManager();
        var assembly=(com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe)manager.byKey(ResourceLocation.parse("civitas_industria:integration/standard_components")).orElseThrow().value();
        if(assembly.getLoops()!=1||assembly.getSequence().size()!=5||assembly.getOutputChance()!=1)throw new AssertionError("Assembly loop/output cost changed");
        ItemStack work=new ItemStack(item("create:iron_sheet"));
        String[] parts={"create:cogwheel","immersiveengineering:wire_copper","minecraft:iron_nugget","minecraft:iron_nugget"};
        for(String part:parts){
            var stage=com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe.getRecipe(level,work,
                com.simibubi.create.AllRecipeTypes.DEPLOYING.getType(),com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe.class).orElseThrow(()->new AssertionError("Assembly lost deployer progress"));
            if(!stage.value().getIngredients().get(1).test(new ItemStack(item(part))))throw new AssertionError("Wrong assembly ingredient: "+part);
            var output=stage.value().rollResults(level.random);
            if(output.size()!=1||output.getFirst().getCount()!=1)throw new AssertionError("Assembly duplicated material");
            work=output.getFirst();
            if(!work.is(item("incomplete_precision_component")))throw new AssertionError("Assembly completed before fastening/pressing");
        }
        var finalStage=com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe.getRecipe(level,work,
            com.simibubi.create.AllRecipeTypes.PRESSING.getType(),com.simibubi.create.content.kinetics.press.PressingRecipe.class).orElseThrow();
        var output=finalStage.value().rollResults(level.random);
        if(output.size()!=1||output.getFirst().getCount()!=1||!output.getFirst().is(item("precision_component")))throw new AssertionError("Assembly did not yield one finished component");
        var slaking=(com.simibubi.create.content.kinetics.mixer.MixingRecipe)manager.byKey(ResourceLocation.parse("civitas_industria:integration/lime_slaking")).orElseThrow().value();
        if(slaking.getFluidIngredients().size()!=1||slaking.getFluidIngredients().getFirst().amount()!=250
                ||!slaking.getFluidIngredients().getFirst().test(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,250)))throw new AssertionError("Slaking lost water cost");
        var manual=(net.minecraft.world.item.crafting.ShapelessRecipe)manager.byKey(ResourceLocation.parse("civitas_industria:integration/manual_slaking")).orElseThrow().value();
        var stacks=new java.util.ArrayList<ItemStack>();
        for(int i=0;i<4;i++)stacks.add(new ItemStack(item("quicklime")));
        stacks.add(new ItemStack(Items.WATER_BUCKET));while(stacks.size()<9)stacks.add(ItemStack.EMPTY);
        var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,stacks);
        if(!manual.matches(input,level)||manual.assemble(input,level.registryAccess()).getCount()!=4)throw new AssertionError("Manual slaking is not craftable");
        var remaining=manual.getRemainingItems(input);
        if(remaining.stream().filter(i->i.is(Items.BUCKET)).mapToInt(ItemStack::getCount).sum()!=1)throw new AssertionError("Manual slaking did not return exactly one bucket");
    }
    public static void recipes(GameTestHelper h) {
        var manager = h.getLevel().getRecipeManager();
        for (String id : new String[]{"civitas_industria:integration/steel_sheet", "civitas_industria:integration/hemp_filter_paper",
                "civitas_industria:integration/sulfur_reagent", "civitas_industria:integration/standard_components",
                "immersiveengineering:metalpress/plate_iron", "adpother:gold_filter_frame", "adchimneys:metal_pump",
                "civitas_industria:integration/limestone_dust", "civitas_industria:integration/quicklime",
                "civitas_industria:integration/quicklime_firing", "civitas_industria:integration/lime_slaking",
                "civitas_industria:integration/manual_slaking", "civitas_industria:integration/gypsum_binder",
                "civitas_industria:integration/gypsum_panels"}) {
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
        if(pump.getResultItem(h.getLevel().registryAccess()).getCount()!=1)throw new AssertionError("One mechanical pump multiplied into multiple native pumps");
        processRecipes(h);
        var pos=h.absolutePos(new BlockPos(3,2,3));
        h.getLevel().setBlockAndUpdate(pos,block("adpother:iron_filter_frame").defaultBlockState());
        var filter=(FilterFrame)block("adpother:iron_filter_frame");
        var tile=(FilterFrame.BlockTile)h.getLevel().getBlockEntity(pos);
        tile.getInputInventory().insertItem(0,reagent.copy(),false);
        h.runAfterDelay(5,()->{
        int captured=filter.fill(tile,Pollutants.BuiltIn.SULFUR.get(),32);
        if(captured!=32||!tile.getFilterMaterial().isEmpty()
                ||!tile.getByproduct().is(item("sulfate_filter_cake"))||tile.getByproduct().getCount()!=1)
            throw new AssertionError("Sulfur capture mismatch: captured="+captured+" material="+tile.getFilterMaterial()+" byproduct="+tile.getByproduct());
        if(manager.byKey(ResourceLocation.parse("civitas_industria:integration/sulfur_fertilizer")).isPresent())
            throw new AssertionError("Obsolete sulfur-to-fertilizer shortcut remains loaded");
        System.out.println("CIVITAS FOUR CORE: recipe loading, assembly, water costs and paid sulfate recovery passed");
        h.succeed();
        });
    }
}
