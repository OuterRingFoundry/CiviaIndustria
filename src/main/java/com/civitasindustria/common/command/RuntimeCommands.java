package com.civitasindustria.common.command;

import com.civitasindustria.domain.*;
import com.civitasindustria.platform.WorldRuntime;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** Bounded administrative commands; queries never allocate persistent cells. */
public final class RuntimeCommands {
    private RuntimeCommands() {}
    private static WorldRuntime runtime(CommandSourceStack s) { return WorldRuntime.get(s.getLevel()); }
    private static CellPos here(CommandSourceStack s) {
        BlockPos p=BlockPos.containing(s.getPosition());return CellPos.fromBlock(p.getX(),p.getZ());
    }
    private static int reply(CommandSourceStack s,String message) {
        s.sendSuccess(()->Component.literal(message),false);return 1;
    }
    private static int inspect(CommandSourceStack s,CellPos p) {
        CellData c=runtime(s).state().cells.get(p);
        if(c==null)return reply(s,"CI cell "+p+" | unstored pristine wilderness");
        return reply(s,"CI cell "+p+" | "+c.civilization+" | ecology "+c.ecology()+
            " | AQI "+c.aqi()+" | WQI "+c.waterQuality()+" | acid "+c.acidPrecursorLoad+
            " | pollutants "+java.util.Arrays.toString(c.pollutants)+" | degradation "+c.degradation);
    }
    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        var env=Commands.literal("env").requires(s->s.hasPermission(2))
            .then(Commands.literal("here").executes(c->inspect(c.getSource(),here(c.getSource()))))
            .then(Commands.literal("ecology").executes(c->inspect(c.getSource(),here(c.getSource()))))
            .then(Commands.literal("inspect")
                .then(Commands.argument("x",IntegerArgumentType.integer(-468750,468749))
                .then(Commands.argument("z",IntegerArgumentType.integer(-468750,468749))
                .executes(c->inspect(c.getSource(),new CellPos(IntegerArgumentType.getInteger(c,"x"),IntegerArgumentType.getInteger(c,"z")))))))
            .then(Commands.literal("simulate").executes(c->{runtime(c.getSource()).simulate(c.getSource().getLevel());return reply(c.getSource(),"CI simulation step complete");}))
            .then(Commands.literal("acidrain").then(Commands.literal("status").executes(c->{
                var s=c.getSource();var cell=runtime(s).state().cells.get(here(s));
                return reply(s,"CI acid rain | precipitation "+s.getLevel().isRainingAt(BlockPos.containing(s.getPosition()))+
                    " | acid precursor "+(cell==null?0:cell.acidPrecursorLoad));
            })));
        var add=Commands.literal("add");
        var clear=Commands.literal("clear");
        for(Pollutant pollutant:Pollutant.values()) {
            String name=pollutant.name().toLowerCase(java.util.Locale.ROOT);
            add.then(Commands.literal(name).then(Commands.argument("amount",DoubleArgumentType.doubleArg(0,1000000)).executes(c->{
                var s=c.getSource();runtime(s).emit(here(s),pollutant,DoubleArgumentType.getDouble(c,"amount"));
                return reply(s,"CI added "+name);
            })));
            clear.then(Commands.literal(name).executes(c->{
                var s=c.getSource();var r=runtime(s);var cell=r.state().cells.get(here(s));
                if(cell!=null){cell.pollutants[pollutant.ordinal()]=0;cell.acidPrecursorLoad=cell.get(Pollutant.SOX)+cell.get(Pollutant.NOX);r.dirty();}
                return reply(s,"CI cleared "+name);
            }));
        }
        clear.then(Commands.literal("all").executes(c->{
            var s=c.getSource();var r=runtime(s);var cell=r.state().cells.get(here(s));
            if(cell!=null){java.util.Arrays.fill(cell.pollutants,0);cell.acidPrecursorLoad=0;r.dirty();}
            return reply(s,"CI cleared pollutants; ecological recovery remains gradual");
        }));
        root.then(env.then(add).then(clear));
        root.then(Commands.literal("load").requires(s->s.hasPermission(2))
            .then(Commands.literal("here").executes(c->reply(c.getSource(),"CI industrial load "+runtime(c.getSource()).load(BlockPos.containing(c.getSource().getPosition())))))
            .then(Commands.literal("rescan").then(Commands.argument("radius",IntegerArgumentType.integer(0,8)).executes(c->{
                var s=c.getSource();BlockPos pos=BlockPos.containing(s.getPosition());int radius=IntegerArgumentType.getInteger(c,"radius");
                int count=0;
                for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){
                    int cx=(pos.getX()>>4)+x,cz=(pos.getZ()>>4)+z;
                    if(s.getLevel().getChunkSource().hasChunk(cx,cz)){runtime(s).rescanChunk(new net.minecraft.world.level.ChunkPos(cx,cz));count++;}
                }
                return reply(s,"CI queued "+count+" loaded chunks");
            }))));
        root.then(Commands.literal("civilization").requires(s->s.hasPermission(2))
            .then(Commands.literal("status").executes(c->inspect(c.getSource(),here(c.getSource()))))
            .then(Commands.literal("networks").executes(c->reply(c.getSource(),"CI networks "+runtime(c.getSource()).networks().size()+" | nodes "+runtime(c.getSource()).state().nodes.size())))
            .then(Commands.literal("recalculate").executes(c->{runtime(c.getSource()).recalculate();return reply(c.getSource(),"CI topology rebuild queued");})));
        root.then(Commands.literal("perf").requires(s->s.hasPermission(2)).executes(c->{
            var s=c.getSource();var r=runtime(s);long now=System.currentTimeMillis()/1000;
            reply(s,"CI stored cells "+r.state().cells.size()+" | active cells "+r.dirtyEntries());
            r.metrics.forEach((name,m)->reply(s,name+" | avg60ms "+m.average(now,60)+" | avg300ms "+m.average(now,300)+" | maxms "+m.maximum(now)+" | entries "+m.processed()));return 1;
        }).then(Commands.literal("reset").executes(c->{runtime(c.getSource()).metrics.values().forEach(RollingMetrics::reset);return reply(c.getSource(),"CI metrics reset");})));
    }
}
