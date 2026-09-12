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
            " | pollution "+PollutionEffects.degree(PollutionEffects.severity(c))+" "+Math.round(PollutionEffects.severity(c)*100)+"%"+
            " | crop rate "+String.format(java.util.Locale.ROOT,"%.2fx",PollutionEffects.cropRate(c,com.civitasindustria.common.config.ServerConfig.CROP_MIN_GROWTH.get()))+
            " | animal health/growth "+String.format(java.util.Locale.ROOT,"%.2fx/%.2fx",com.civitasindustria.common.config.ServerConfig.ANIMAL_EFFECTS.get()?PollutionEffects.rate(PollutionEffects.severity(c),com.civitasindustria.common.config.ServerConfig.ANIMAL_MIN_HEALTH.get()):1,com.civitasindustria.common.config.ServerConfig.ANIMAL_EFFECTS.get()?PollutionEffects.rate(PollutionEffects.severity(c),com.civitasindustria.common.config.ServerConfig.ANIMAL_MIN_GROWTH.get()):1)+
            " | AQI "+c.aqi()+" | WQI "+c.waterQuality()+" | acid "+c.acidPrecursorLoad+
            " | pollutants "+java.util.Arrays.toString(c.pollutants)+" | degradation "+c.degradation);
    }
    private static int economy(CommandSourceStack s){
        var r=runtime(s);var cell=here(s);var network=r.networks().stream().filter(n->n.cells().contains(cell)).findFirst().orElse(null);
        if(network==null)return reply(s,"No civic network here. Quiet wilderness homes remain viable; link civic cells for shared upkeep and defense.");
        double cost=Math.ceil(CivilizationGraph.maintenance(network,com.civitasindustria.common.config.ServerConfig.MAINTENANCE_BASE.get(),com.civitasindustria.common.config.ServerConfig.MAINTENANCE_EDGE.get(),com.civitasindustria.common.config.ServerConfig.MAINTENANCE_AREA.get()));
        double funds=0;int defenses=0;
        for(var n:r.state().nodes.values())if(network.cells().contains(n.cell)){if(n.kind==WorldState.CivicNode.Kind.CORE||n.kind==WorldState.CivicNode.Kind.MAINTENANCE)funds+=n.credits;else if(n.kind==WorldState.CivicNode.Kind.DEFENSE&&n.credits>0)defenses++;}
        var stored=r.state().cells.get(cell);
        return reply(s,String.format(java.util.Locale.ROOT,"Civic economy: %d cells, %d exposed edges | %.0f credits / %d ticks (%.2f per cell) | treasury %.0f | %d funded defense nodes (protection applies locally) | local threat %.1f / %.1f. Compact connected districts share perimeter costs; filter industry away from homes.",network.cells().size(),network.perimeter(),cost,com.civitasindustria.common.config.ServerConfig.MAINTENANCE_INTERVAL.get(),cost/network.cells().size(),funds,defenses,stored==null?0:stored.threatPressure,com.civitasindustria.common.config.ServerConfig.THREAT_THRESHOLD.get()));
    }
    public static void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        var env=Commands.literal("env")
            .then(Commands.literal("here").executes(c->inspect(c.getSource(),here(c.getSource()))))
            .then(Commands.literal("ecology").executes(c->inspect(c.getSource(),here(c.getSource()))))
            .then(Commands.literal("inspect")
                .then(Commands.argument("x",IntegerArgumentType.integer(-468750,468749))
                .then(Commands.argument("z",IntegerArgumentType.integer(-468750,468749))
                .executes(c->inspect(c.getSource(),new CellPos(IntegerArgumentType.getInteger(c,"x"),IntegerArgumentType.getInteger(c,"z")))))))
            .then(Commands.literal("simulate").requires(s->s.hasPermission(2)).executes(c->{runtime(c.getSource()).simulate(c.getSource().getLevel(),true);return reply(c.getSource(),"CI simulation step complete");}))
            .then(Commands.literal("acidrain").then(Commands.literal("status").executes(c->{
                var s=c.getSource();var cell=runtime(s).state().cells.get(here(s));
                return reply(s,"CI acid rain | precipitation "+s.getLevel().isRainingAt(BlockPos.containing(s.getPosition()))+
                    " | acid precursor "+(cell==null?0:cell.acidPrecursorLoad));
            })));
        var add=Commands.literal("add").requires(s->s.hasPermission(2));
        var clear=Commands.literal("clear").requires(s->s.hasPermission(2));
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
        root.then(Commands.literal("load")
            .then(Commands.literal("here").executes(c->reply(c.getSource(),"CI industrial load "+runtime(c.getSource()).load(BlockPos.containing(c.getSource().getPosition())))))
            .then(Commands.literal("rescan").requires(s->s.hasPermission(2)).then(Commands.argument("radius",IntegerArgumentType.integer(0,8)).executes(c->{
                var s=c.getSource();BlockPos pos=BlockPos.containing(s.getPosition());int radius=IntegerArgumentType.getInteger(c,"radius");
                int count=0;
                for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){
                    int cx=(pos.getX()>>4)+x,cz=(pos.getZ()>>4)+z;
                    if(s.getLevel().getChunkSource().hasChunk(cx,cz)){runtime(s).rescanChunk(new net.minecraft.world.level.ChunkPos(cx,cz));count++;}
                }
                return reply(s,"CI queued "+count+" loaded chunks");
            }))));
        root.then(Commands.literal("civilization")
            .then(Commands.literal("economy").executes(c->economy(c.getSource())))
            .then(Commands.literal("status").executes(c->inspect(c.getSource(),here(c.getSource()))))
            .then(Commands.literal("networks").executes(c->reply(c.getSource(),"CI networks "+runtime(c.getSource()).networks().size()+" | nodes "+runtime(c.getSource()).state().nodes.size())))
            .then(Commands.literal("recalculate").requires(s->s.hasPermission(2)).executes(c->{runtime(c.getSource()).recalculate();return reply(c.getSource(),"CI topology rebuild queued");})));
        root.then(Commands.literal("perf").requires(s->s.hasPermission(2)).executes(c->{
            var s=c.getSource();var r=runtime(s);long now=System.currentTimeMillis()/1000;
            reply(s,"CI stored cells "+r.state().cells.size()+" | active cells "+r.dirtyEntries());
            r.metrics.forEach((name,m)->reply(s,name+" | avg60ms "+m.average(now,60)+" | avg300ms "+m.average(now,300)+" | maxms "+m.maximum(now)+" | entries "+m.processed()));return 1;
        }).then(Commands.literal("reset").executes(c->{runtime(c.getSource()).metrics.values().forEach(RollingMetrics::reset);return reply(c.getSource(),"CI metrics reset");})));
    }
}
