package com.civitasindustria.common.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/** Inventory construction only; no tick work or cached copies of item stacks. */
public final class CreativeContent {
    private CreativeContent() {}

    public static void displayIndustry(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        // Functional groups stay together, including the parts of the 3x3 warehouse.
        var remaining = new java.util.LinkedHashMap<String, Item>();
        for (var entry : CivitasRegistries.ITEMS.getEntries()) remaining.put(entry.getId().getPath(), entry.get());
        for (String id : java.util.List.of(
                "precision_workbench", "electric_motor", "rotation_dynamo", "factory_controller", "remediation_station", "bulk_tank",
                "cargo_crate", "pallet", "cargo_loader", "cargo_unloader", "freight_terminal",
                "warehouse_controller", "warehouse_casing", "warehouse_port",
                "market_counter", "civic_core", "civic_relay", "logistics_node", "defense_node", "maintenance_depot",
                "gypsum_panel", "decorative_gear", "decorative_fan", "decorative_pump", "decorative_gauge", "decorative_piston", "decorative_vent")) {
            Item item = remaining.remove(id);
            if (item != null) output.accept(item);
        }
        // Every newly registered item remains discoverable, even without an explicit group.
        remaining.values().forEach(output::accept);
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
