package com.civitasindustria.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.fml.ModList;

@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class PollutionGameTests {
    @GameTest(template="empty",batch="native_pollution",timeoutTicks=400)
    public static void paidNativeFiltersAndRegionalDose(GameTestHelper h) {
        if (!ModList.get().isLoaded("adpother")) { h.succeed(); return; }
        com.civitasindustria.compat.pollution.PollutionChecks.filtersAndDose(h);
    }
    @GameTest(template="empty",batch="four_core_recipes")
    public static void crossModRecipesAndMaterials(GameTestHelper h) {
        if (!(ModList.get().isLoaded("adpother") && ModList.get().isLoaded("adchimneys")
                && ModList.get().isLoaded("create") && ModList.get().isLoaded("immersiveengineering"))) {
            h.succeed(); return;
        }
        com.civitasindustria.compat.pollution.PollutionChecks.recipes(h);
    }
}
