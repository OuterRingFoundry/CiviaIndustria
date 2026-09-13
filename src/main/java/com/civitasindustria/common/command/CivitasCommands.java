package com.civitasindustria.common.command;

import com.civitasindustria.CivitasIndustria;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CivitasCommands {
    private CivitasCommands() {}

    public static void register(RegisterCommandsEvent event) {
        var root=Commands.literal("ci")
                .then(Commands.literal("version").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal(
                            "Civitas Industria " + version(CivitasIndustria.MOD_ID)
                            + " | data " + CivitasIndustria.DATA_VERSION
                            + " | Minecraft " + SharedConstants.getCurrentVersion().getName()
                            + " | NeoForge " + version("neoforge")
                            + " | Java " + System.getProperty("java.version")), false);
                    return 1;
                }));
        RuntimeCommands.attach(root);
        ParcelCommands.attach(root);
        ResidenceCommands.attach(root);
        EconomyCommands.attach(root);
        event.getDispatcher().register(root);
    }

    private static String version(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }
}
