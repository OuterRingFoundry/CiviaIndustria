package com.civitasindustria.common.command;

import com.civitasindustria.platform.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;

public final class ResidenceCommands {
    private static final DynamicCommandExceptionType INVALID = new DynamicCommandExceptionType(
            message -> Component.literal(String.valueOf(message)));
    private ResidenceCommands() {}
    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("residence")
                .then(Commands.literal("declare").executes(c -> {
                    var player = c.getSource().getPlayerOrException();
                    var level = player.serverLevel();
                    var pos = player.blockPosition();
                    var parcel = WorldRuntime.get(level).state().parcels.at(pos.getX(), pos.getY(), pos.getZ())
                            .orElseThrow(() -> INVALID.create("No parcel here."));
                    try {
                        var saved = ResidenceSavedData.get(level);
                        saved.registry.declare(player.getUUID(), level.dimension().location().toString(), parcel,
                                pos.getX(), pos.getY(), pos.getZ());
                        saved.setDirty();
                    } catch (IllegalArgumentException e) { throw INVALID.create(e.getMessage()); }
                    return reply(c.getSource(), ResidenceServices.status(player));
                }))
                .then(Commands.literal("status").executes(c -> reply(c.getSource(),
                        ResidenceServices.status(c.getSource().getPlayerOrException()))))
                .then(Commands.literal("withdraw").executes(c -> {
                    var player = c.getSource().getPlayerOrException();
                    var saved = ResidenceSavedData.get(player.serverLevel());
                    boolean removed = saved.registry.withdraw(player.getUUID());
                    if (removed) saved.setDirty();
                    return reply(c.getSource(), removed ? "Residence withdrawn." : "No residence declared.");
                })));
    }
    private static int reply(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
        return 1;
    }
}
