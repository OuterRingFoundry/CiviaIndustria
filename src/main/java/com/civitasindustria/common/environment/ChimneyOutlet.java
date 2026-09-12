package com.civitasindustria.common.environment;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import java.util.*;
/** Loaded-only bounded outlet lookup; a chimney transports pollution, never deletes it. */
public final class ChimneyOutlet {
    private static boolean chimney(ServerLevel level,BlockPos p){if(!level.hasChunkAt(p))return false;String id=BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).toString();return id.startsWith("adchimneys:")&&(id.endsWith("chimney")||id.endsWith("vent")||id.endsWith("duct")||id.endsWith("pipe"));}
    public static BlockPos find(ServerLevel level,BlockPos source){BlockPos first=source.above();if(!chimney(level,first))return source;Set<BlockPos> visited=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();queue.add(first);visited.add(first);BlockPos outlet=null;while(!queue.isEmpty()){BlockPos p=queue.remove();BlockPos up=p.above();if(level.hasChunkAt(up)&&level.isEmptyBlock(up)&&level.canSeeSky(up)&&(outlet==null||up.getY()>outlet.getY()))outlet=up;for(Direction d:Direction.values()){BlockPos n=p.relative(d);if(!visited.contains(n)&&chimney(level,n)){if(visited.size()==64)return source;visited.add(n);queue.add(n);}}}return outlet==null?source:outlet;}
}
