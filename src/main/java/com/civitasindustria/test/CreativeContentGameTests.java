package com.civitasindustria.test;

import com.civitasindustria.common.registry.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class CreativeContentGameTests {
    @GameTest(template="empty")
    public static void registeredContentVisibleWithoutOperatorPermission(GameTestHelper h) {
        var parameters = new CreativeModeTab.ItemDisplayParameters(h.getLevel().enabledFeatures(), false, h.getLevel().registryAccess());
        var industry = CivitasRegistries.INDUSTRY_TAB.get();
        industry.buildContents(parameters);
        if (!industry.hasSearchBar()) throw new AssertionError("Industry tab is not searchable");
        Set<Item> displayed = new HashSet<>();
        for (ItemStack stack : industry.getDisplayItems()) {
            if (stack.getCount() != 1 || !displayed.add(stack.getItem())) throw new AssertionError("Duplicate/invalid creative stack");
        }
        if (displayed.size() != CivitasRegistries.ITEMS.getEntries().size()) throw new AssertionError("Missing creative content");
        for (var key : List.of(CreativeModeTabs.FUNCTIONAL_BLOCKS, CreativeModeTabs.BUILDING_BLOCKS,
                CreativeModeTabs.INGREDIENTS, CreativeModeTabs.TOOLS_AND_UTILITIES))
            BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(key).buildContents(parameters);
        for (var entry : CivitasRegistries.ITEMS.getEntries()) {
            var item = entry.get();
            if (!displayed.contains(item) || !industry.contains(new ItemStack(item)))
                throw new AssertionError("Item missing from industry/search: " + entry.getId());
            var vanilla = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(CreativeContent.category(item));
            if (vanilla.getDisplayItems().stream().noneMatch(stack -> stack.is(item)))
                throw new AssertionError("Item missing from vanilla category: " + entry.getId());
        }
        h.succeed();
    }
}
