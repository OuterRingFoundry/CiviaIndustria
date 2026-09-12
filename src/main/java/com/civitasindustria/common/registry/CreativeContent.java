package com.civitasindustria.common.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/** Inventory construction only; no tick work or cached copies of item stacks. */
public final class CreativeContent {
    private CreativeContent() {}

    public static void displayIndustry(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        // Machines first, then materials/tools. Iterate the live registration so new content is included.
        CivitasRegistries.ITEMS.getEntries().stream().filter(item -> item.get() instanceof BlockItem)
                .forEach(item -> output.accept(item.get()));
        CivitasRegistries.ITEMS.getEntries().stream().filter(item -> !(item.get() instanceof BlockItem))
                .forEach(item -> output.accept(item.get()));
    }

    public static ResourceKey<CreativeModeTab> category(Item item) {
        if (item == CivitasRegistries.CONTENT.get("gypsum_panel").get().asItem()) return CreativeModeTabs.BUILDING_BLOCKS;
        if (item instanceof BlockItem) return CreativeModeTabs.FUNCTIONAL_BLOCKS;
        if (item == CivitasRegistries.CUTTING_INSERT.get() || item == CivitasRegistries.CALIBRATION_KIT.get()
                || item == CivitasRegistries.REMEDIATION_REAGENT.get()) return CreativeModeTabs.TOOLS_AND_UTILITIES;
        return CreativeModeTabs.INGREDIENTS;
    }

    public static void vanillaTabs(BuildCreativeModeTabContentsEvent event) {
        var key = event.getTabKey();
        if (!key.equals(CreativeModeTabs.FUNCTIONAL_BLOCKS) && !key.equals(CreativeModeTabs.BUILDING_BLOCKS)
                && !key.equals(CreativeModeTabs.INGREDIENTS) && !key.equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) return;
        for (var entry : CivitasRegistries.ITEMS.getEntries()) {
            Item item = entry.get();
            if (key.equals(category(item))) event.accept(item);
        }
    }
}
