package com.civitasindustria.compat.create;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.warehouse.CargoBlockEntity;
import com.simibubi.create.content.trains.entity.*;
import com.simibubi.create.content.trains.graph.*;
import com.simibubi.create.content.trains.track.TrackMaterial;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;
/** Real carriage authority and travelling-point serialization, separate from scheduled-route acceptance. */
public final class CarriageChecks {
    public static void run(GameTestHelper h) throws Exception {
        var level=h.getLevel();BlockPos anchor=h.absolutePos(new BlockPos(2,1,2));
        var bogeyBlock=(com.simibubi.create.content.trains.bogey.AbstractBogeyBlock<?>)BuiltInRegistries.BLOCK.get(ResourceLocation.parse("create:small_bogey"));
        level.setBlock(anchor,bogeyBlock.defaultBlockState(),3);level.setBlock(anchor.above(),CivitasRegistries.CONTENT.get("cargo_crate").get().defaultBlockState(),3);
        var cargo=(CargoBlockEntity)level.getBlockEntity(anchor.above());String[] keys=new String[16];Arrays.fill(keys,"");keys[0]="minecraft:iron_ingot";long[] counts=new long[16];counts[0]=5_000_000_000L;cargo.restoreMounted(keys,counts);
        var glue=new com.simibubi.create.content.contraptions.glue.SuperGlueEntity(level,new AABB(anchor).expandTowards(0,1,0));level.addFreshEntity(glue);
        var contraption=new CarriageContraption(Direction.SOUTH);if(!contraption.assemble(level,anchor))throw new AssertionError("Carriage assembly failed");
        var graph=new TrackGraph();var start=new TrackNodeLocation(anchor.getX()+.5,anchor.getY(),anchor.getZ()-4).in(level);var end=new TrackNodeLocation(anchor.getX()+.5,anchor.getY(),anchor.getZ()+2200).in(level);
        graph.loadNode(start,TrackGraph.nextNodeId(),new Vec3(0,1,0));graph.loadNode(end,TrackGraph.nextNodeId(),new Vec3(0,1,0));var first=graph.locateNode(start);var last=graph.locateNode(end);
        var edge=new TrackEdge(first,last,null,TrackMaterial.ANDESITE);graph.putConnection(first,last,edge);graph.putConnection(last,first,new TrackEdge(last,first,null,TrackMaterial.ANDESITE));
        var front=new TravellingPoint(first,last,edge,5,false);var rear=new TravellingPoint(first,last,edge,3,false);
        var bogey=new CarriageBogey(bogeyBlock,false,new net.minecraft.nbt.CompoundTag(),front,rear);var carriage=new Carriage(bogey,null,0);
        var train=new Train(UUID.randomUUID(),UUID.randomUUID(),graph,List.of(carriage),List.of(),false,0);
        com.simibubi.create.Create.RAILWAYS.putGraphWithDefaultGroup(graph);com.simibubi.create.Create.RAILWAYS.addTrain(train);
        try{
            carriage.setContraption(level,contraption);contraption.removeBlocksFromWorld(level,BlockPos.ZERO);glue.discard();
            if(carriage.storage.getAllItemStorages().values().stream().noneMatch(s->s instanceof MountedCargo))throw new AssertionError("Carriage lost custom authority");
            for(int n=0;n<128;n++){
                double moved=carriage.travel(level,graph,16,null,null,0);if(Math.abs(moved-16)>.01)throw new AssertionError("Rail travel refused at "+n+": "+moved);
            }
            var palette=new DimensionPalette();var encoded=carriage.write(palette,level.registryAccess());var restored=Carriage.read(encoded,level.registryAccess(),graph,palette);
            long total=restored.storage.getAllItemStorages().values().stream().filter(s->s instanceof MountedCargo).mapToLong(s->((MountedCargo)s).total()).sum();
            if(total!=5_000_000_000L)throw new AssertionError("Carriage save lost cargo");
            var mounted=restored.storage.getAllItemStorages().values().stream().filter(s->s instanceof MountedCargo).map(s->(MountedCargo)s).findFirst().orElseThrow();
            var transferred=mounted.extractItem(0,64,false);
            if(transferred.getCount()!=64||mounted.total()!=5_000_000_000L-64)throw new AssertionError("Post-travel transfer changed long authority");
            if(restored.getLeadingPoint().position<2048)throw new AssertionError("Carriage did not cross 2048 track blocks");
        }finally{
            carriage.forEachPresentEntity(entity->entity.discard());com.simibubi.create.Create.RAILWAYS.removeTrain(train.id);com.simibubi.create.Create.RAILWAYS.removeGraphAndGroup(graph);glue.discard();
        }
    }
}
