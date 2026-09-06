package com.civitasindustria.common.command;

import com.civitasindustria.domain.Parcel;
import com.civitasindustria.platform.WorldRuntime;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.*;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class ParcelCommands {
    private static final SimpleCommandExceptionType NO_PARCEL=new SimpleCommandExceptionType(Component.literal("No parcel here, or you are not its owner."));
    private static final DynamicCommandExceptionType INVALID=new DynamicCommandExceptionType(message->Component.literal(String.valueOf(message)));
    private ParcelCommands(){}
    private static Parcel here(CommandSourceStack s,boolean owner)throws CommandSyntaxException{
        BlockPos pos=BlockPos.containing(s.getPosition());
        var parcel=WorldRuntime.get(s.getLevel()).state().parcels.at(pos.getX(),pos.getY(),pos.getZ()).orElseThrow(NO_PARCEL::create);
        if(owner&&!s.hasPermission(2)&&!parcel.owner().equals(s.getPlayerOrException().getUUID()))throw NO_PARCEL.create();
        return parcel;
    }
    private static int reply(CommandSourceStack s,String value){s.sendSuccess(()->Component.literal(value),false);return 1;}
    private static void replace(CommandSourceStack s,Parcel before,Parcel after){
        var runtime=WorldRuntime.get(s.getLevel());var index=runtime.state().parcels;
        index.remove(before.id());try{index.add(after);}catch(RuntimeException e){index.add(before);throw e;}runtime.dirty();
    }
    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root){
        var parcel=Commands.literal("parcel")
            .then(Commands.literal("info").executes(c->{var p=here(c.getSource(),false);return reply(c.getSource(),p.name()+" | owner "+p.owner()+" | trusted "+p.trusted()+" | public flags "+p.flags());}))
            .then(Commands.literal("create").then(Commands.argument("name",StringArgumentType.string())
                .then(Commands.argument("from",BlockPosArgument.blockPos()).then(Commands.argument("to",BlockPosArgument.blockPos()).executes(c->{
                    var s=c.getSource();var a=BlockPosArgument.getBlockPos(c,"from");var b=BlockPosArgument.getBlockPos(c,"to");
                    try{
                        var p=new Parcel(UUID.randomUUID(),s.getPlayerOrException().getUUID(),Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()),Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()),StringArgumentType.getString(c,"name"),Set.of(),Set.of());
                        WorldRuntime.get(s.getLevel()).state().parcels.add(p);WorldRuntime.get(s.getLevel()).dirty();return reply(s,"Created parcel "+p.name());
                    }catch(IllegalArgumentException e){throw INVALID.create(e.getMessage());}
                })))))
            .then(Commands.literal("remove").executes(c->{var s=c.getSource();var p=here(s,true);WorldRuntime.get(s.getLevel()).state().parcels.remove(p.id());WorldRuntime.get(s.getLevel()).dirty();return reply(s,"Removed parcel "+p.name());}));
        for(boolean trust:new boolean[]{true,false})parcel.then(Commands.literal(trust?"trust":"untrust").then(Commands.argument("player",UuidArgument.uuid()).executes(c->{
            var s=c.getSource();var p=here(s,true);var trusted=new HashSet<>(p.trusted());var id=UuidArgument.getUuid(c,"player");if(trust)trusted.add(id);else trusted.remove(id);
            if(trusted.size()>64)throw INVALID.create("Trust limit 64");
            replace(s,p,new Parcel(p.id(),p.owner(),p.minX(),p.minY(),p.minZ(),p.maxX(),p.maxY(),p.maxZ(),p.name(),trusted,p.flags()));return reply(s,"Parcel trust updated");
        })));
        var flags=Commands.literal("flag");
        for(var flag:Parcel.Flag.values())flags.then(Commands.literal(flag.name().toLowerCase(Locale.ROOT)).then(Commands.argument("public",BoolArgumentType.bool()).executes(c->{
            var s=c.getSource();var p=here(s,true);var updated=EnumSet.noneOf(Parcel.Flag.class);updated.addAll(p.flags());if(BoolArgumentType.getBool(c,"public"))updated.add(flag);else updated.remove(flag);
            replace(s,p,new Parcel(p.id(),p.owner(),p.minX(),p.minY(),p.minZ(),p.maxX(),p.maxY(),p.maxZ(),p.name(),p.trusted(),updated));return reply(s,"Parcel public permission updated");
        })));
        root.then(parcel.then(flags));
    }
}
