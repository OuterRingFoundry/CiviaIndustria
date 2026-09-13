package com.civitasindustria.common.command;
import com.civitasindustria.platform.EconomySavedData;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
public final class EconomyCommands {
    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root){root.then(Commands.literal("money").executes(c->{var p=c.getSource().getPlayerOrException();var l=EconomySavedData.get(p.serverLevel()).registry;c.getSource().sendSuccess(()->Component.literal("Crowns: "+l.balance(p.getUUID())+". 100 crowns = 1 reserved diamond."),false);return 1;}).then(Commands.literal("pay").then(Commands.argument("player",EntityArgument.player()).then(Commands.argument("crowns",LongArgumentType.longArg(1,1000000000)).executes(c->{var from=c.getSource().getPlayerOrException();var to=EntityArgument.getPlayer(c,"player");var saved=EconomySavedData.get(from.serverLevel());long amount=LongArgumentType.getLong(c,"crowns");if(!saved.registry.transfer(from.getUUID(),to.getUUID(),amount)){c.getSource().sendFailure(Component.literal("Transfer refused: check balance and recipient."));return 0;}saved.setDirty();c.getSource().sendSuccess(()->Component.literal("Paid "+amount+" crowns to "+to.getScoreboardName()),false);to.displayClientMessage(Component.literal("Received "+amount+" crowns from "+from.getScoreboardName()),false);return 1;})))));}
}
