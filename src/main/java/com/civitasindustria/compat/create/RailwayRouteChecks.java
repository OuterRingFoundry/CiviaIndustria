package com.civitasindustria.compat.create;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.warehouse.CargoBlockEntity;
import com.google.gson.*;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.*;
import com.simibubi.create.content.trains.graph.*;
import com.simibubi.create.content.trains.schedule.*;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;
/** Disposable physical-track and autonomous-schedule fixture; no production listeners without opt-in. */
public final class RailwayRouteChecks {
    private static final String MODE=System.getProperty("civitas.railwayRoute","");
    private static final Path RECORD=Path.of("physical-route.json");
    private static boolean active;private static int forced,placed,wait,ticks,idle;private static JsonArray trains;
    public static void register(IEventBus bus){if(MODE.isEmpty())return;bus.addListener(RailwayRouteChecks::start);bus.addListener(RailwayRouteChecks::tick);}
    private static void start(ServerStartedEvent event){
        try{if(MODE.equals("write")){if(Files.exists(RECORD))throw new IllegalStateException("Existing route fixture");trains=new JsonArray();}
            else if(MODE.equals("read")||MODE.equals("verify")||MODE.equals("inspect"))trains=JsonParser.parseString(Files.readString(RECORD)).getAsJsonArray();else throw new IllegalArgumentException("Route mode");active=true;
        }catch(Exception e){throw new RuntimeException(e);}
    }
    private static BlockState block(String id){return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).defaultBlockState();}
    private static <T extends Comparable<T>> BlockState value(BlockState state,Property<T> property,String value){return state.setValue(property,property.getValue(value).orElseThrow());}
    private static BlockState value(BlockState state,String name,String value){return value(state,state.getBlock().getStateDefinition().getProperty(name),value);}
    private static Object side(Object pair,String name)throws Exception{return pair.getClass().getMethod(name).invoke(pair);}
    private static void station(ServerLevel level,int x,int z,String name){
        var pos=new BlockPos(x+2,-60,z);level.setBlock(pos,block("create:track_station"),3);var station=(StationBlockEntity)level.getBlockEntity(pos);
        var tag=new CompoundTag();tag.putIntArray("TargetTrack",new int[]{-2,0,0});tag.putBoolean("TargetDirection",false);tag.putBoolean("Ortho",true);
        station.edgePoint.read(tag,level.registryAccess(),false);((net.minecraft.world.level.block.entity.BlockEntity)station).setChanged();
    }
    static Train assembleAt(ServerLevel level,BlockPos anchor,int number,long quantity)throws Exception{
        var bogeyBlock=(com.simibubi.create.content.trains.bogey.AbstractBogeyBlock<?>)block("create:small_bogey").getBlock();
        level.setBlock(anchor,bogeyBlock.defaultBlockState(),3);
        var burner=block("create:blaze_burner").setValue(com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL,com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.SMOULDERING);
        level.setBlock(anchor.above(),burner,3);level.setBlock(anchor.above().north(),value(block("create:controls"),"facing","south"),3);
        var cargoPos=anchor.above().south();level.setBlock(cargoPos,CivitasRegistries.CONTENT.get("cargo_crate").get().defaultBlockState(),3);
        if(quantity>0){var keys=new String[16];Arrays.fill(keys,"");keys[0]="minecraft:iron_ingot";var counts=new long[16];counts[0]=quantity;((CargoBlockEntity)level.getBlockEntity(cargoPos)).restoreMounted(keys,counts);}
        level.setBlock(cargoPos.east(),value(block("create:portable_storage_interface"),"facing","east"),3);
        var glue=new com.simibubi.create.content.contraptions.glue.SuperGlueEntity(level,new AABB(anchor.north()).expandTowards(1,1,2));level.addFreshEntity(glue);
        var contraption=new CarriageContraption(Direction.SOUTH);if(!contraption.assemble(level,anchor))throw new AssertionError("Physical carriage assembly failed");
        var location=TrackGraphHelper.getGraphLocationAt(level,anchor.below(),Direction.AxisDirection.POSITIVE,new Vec3(0,0,1));if(location==null)throw new AssertionError("Physical track graph absent");
        Object edgePair=location.getClass().getField("edge").get(location);var a=(TrackNodeLocation)side(edgePair,"getFirst");var b=(TrackNodeLocation)side(edgePair,"getSecond");double position=location.position;var graph=location.graph;
        var first=graph.locateNode(a);var last=graph.locateNode(b);var edge=graph.getConnectionsFrom(first).get(last);
        if(a.getLocation().z>b.getLocation().z){var swap=first;first=last;last=swap;position=edge.getLength()-position;edge=graph.getConnectionsFrom(first).get(last);}
        var bogey=new CarriageBogey(bogeyBlock,false,new CompoundTag(),new TravellingPoint(first,last,edge,position+1,false),new TravellingPoint(first,last,edge,position-1,false));var carriage=new Carriage(bogey,null,0);
        var train=new Train(UUID.randomUUID(),UUID.randomUUID(),graph,List.of(carriage),List.of(),false,number);Create.RAILWAYS.addTrain(train);carriage.setContraption(level,contraption);contraption.removeBlocksFromWorld(level,BlockPos.ZERO);glue.discard();carriage.updateConductors();
        if(!contraption.blockConductors.getFirst())throw new AssertionError("Assembled blaze conductor or controls missing: "+contraption.blockConductors);
        return train;
    }
    private static Train assemble(ServerLevel level,int x,int number)throws Exception{
        var train=assembleAt(level,new BlockPos(x,-59,20),number,0);
        var destination=(StationBlockEntity)level.getBlockEntity(new BlockPos(x+2,-60,2160));if(destination.getStation()==null)throw new AssertionError("Destination station absent from graph");destination.getStation().name="CI_DEST_"+number;Create.RAILWAYS.markTracksDirty();
        return train;
    }
    private static void depart(Train train,int number){
        var instruction=new DestinationInstruction();instruction.getData().putString("Text","CI_DEST_"+number);var entry=new ScheduleEntry();entry.instruction=instruction;var schedule=new Schedule();schedule.cyclic=false;schedule.entries.add(entry);train.runtime.setSchedule(schedule,false);
    }
    private static void mine(ServerLevel level,int number,JsonObject record){
        // Seed mined raw material in a fixed stockpile. Native interfaces must load
        // the initially empty carriage before its autonomous schedule may begin.
        var fixed=new BlockPos(number*32-3,-58,19);
        level.setBlock(fixed,value(block("create:portable_storage_interface"),"facing","east"),3);
        level.setBlock(fixed.west(),value(CivitasRegistries.CONTENT.get("cargo_loader").get().defaultBlockState(),"facing","east"),3);
        level.setBlock(fixed.west(2),CivitasRegistries.CONTENT.get("cargo_crate").get().defaultBlockState(),3);
        var source=(CargoBlockEntity)level.getBlockEntity(fixed.west(2));String[] keys=new String[16];Arrays.fill(keys,"");keys[0]="minecraft:raw_iron";long[] quantities=new long[16];quantities[0]=record.get("quantity").getAsLong();source.restoreMounted(keys,quantities);
        record.addProperty("mineX",fixed.getX());record.addProperty("number",number);record.addProperty("departed",false);
    }
    private static long mineRemaining(ServerLevel level,JsonObject record){
        if(!record.has("mineX"))return 0;
        var fixed=new BlockPos(record.get("mineX").getAsInt(),-58,19);
        return ((CargoBlockEntity)level.getBlockEntity(fixed.west())).total()+((CargoBlockEntity)level.getBlockEntity(fixed.west(2))).total();
    }
    private static void receiver(ServerLevel level,Train train,JsonObject record){
        var entity=train.carriages.getFirst().anyAvailableEntity();if(entity==null)throw new AssertionError("Arriving carriage not loaded");
        var local=new Vec3(1.5,1.5,1.5);var current=entity.toGlobalVector(local,1);var facingVector=entity.toGlobalVector(local.add(1,0,0),1).subtract(current);var facing=Direction.getNearest(facingVector.x,facingVector.y,facingVector.z);
        // The fixture is a straight southbound line: navigation's remaining distance predicts the stop position.
        var moving=BlockPos.containing(current.add(0,0,Math.max(0,train.navigation.distanceToDestination)));var fixed=moving.relative(facing,2);
        level.setBlock(fixed,value(block("create:portable_storage_interface"),"facing",facing.getOpposite().getName()),3);
        var input=fixed.relative(facing);var factoryPos=fixed.relative(facing,2);var output=fixed.relative(facing,3);var center=fixed.relative(facing,5);
        level.setBlock(input,value(CivitasRegistries.CONTENT.get("cargo_unloader").get().defaultBlockState(),"facing",facing.getName()),3);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)level.setBlock(factoryPos.offset(x,-1,z),Blocks.IRON_BLOCK.defaultBlockState(),3);
        level.setBlock(factoryPos,CivitasRegistries.CONTENT.get("factory_controller").get().defaultBlockState(),3);
        var factory=(com.civitasindustria.common.factory.FactoryBlockEntity)level.getBlockEntity(factoryPos);factory.inventory.insertItem(1,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL,record.get("quantity").getAsInt()/8),false);
        var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CIRouteTech"));var kit=new net.minecraft.world.item.ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("civitas_industria:calibration_kit")));
        factory.interact(player,kit,false);if(!kit.isEmpty())throw new AssertionError("Factory calibration refused");
        level.setBlock(output,value(CivitasRegistries.CONTENT.get("cargo_loader").get().defaultBlockState(),"facing",facing.getName()),3);
        level.setBlock(center,CivitasRegistries.CONTENT.get("warehouse_controller").get().defaultBlockState(),3);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0)level.setBlock(center.offset(x,0,z),CivitasRegistries.CONTENT.get("warehouse_casing").get().defaultBlockState(),3);
        level.setBlock(center.relative(facing.getOpposite()),CivitasRegistries.CONTENT.get("warehouse_port").get().defaultBlockState(),3);
        var coordinates=new JsonArray();coordinates.add(fixed.getX());coordinates.add(fixed.getY());coordinates.add(fixed.getZ());record.add("receiver",coordinates);record.addProperty("facing",facing.getName());
        org.slf4j.LoggerFactory.getLogger(RailwayRouteChecks.class).info("ROUTE RECEIVER fixed={} moving-stop={} remaining={}",fixed,moving,train.navigation.distanceToDestination);
    }
    private static BlockPos fixed(JsonObject record){var a=record.getAsJsonArray("receiver");return new BlockPos(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
    private static CargoBlockEntity warehouse(ServerLevel level,JsonObject record){return (CargoBlockEntity)level.getBlockEntity(fixed(record).relative(Direction.byName(record.get("facing").getAsString()),5));}
    private static long received(ServerLevel level,JsonObject record){
        var fixed=fixed(record);var facing=Direction.byName(record.get("facing").getAsString());var input=(CargoBlockEntity)level.getBlockEntity(fixed.relative(facing));var output=(CargoBlockEntity)level.getBlockEntity(fixed.relative(facing,3));var factory=(com.civitasindustria.common.factory.FactoryBlockEntity)level.getBlockEntity(fixed.relative(facing,2));var warehouse=warehouse(level,record);
        for(int slot=0;slot<16;slot++){var stack=warehouse.handler.getStackInSlot(slot);if(!stack.isEmpty()&&!stack.is(net.minecraft.world.item.Items.IRON_INGOT))throw new AssertionError("Factory output bypass: warehouse received "+stack);}
        return input.total()+output.total()+factory.inventory.getStackInSlot(0).getCount()+factory.inventory.getStackInSlot(2).getCount()+warehouse.total();
    }
    private static void tick(ServerTickEvent.Post event){
        if(!active)return;var level=event.getServer().overworld();
        try{
            if(++ticks>10000)throw new AssertionError("Physical railway timed out");
            if(MODE.equals("write")){
                if(forced<725){int x=forced%5-1,z=forced/5-3;level.setChunkForced(x,z,true);level.getChunk(x,z);forced++;return;}
                if(placed<4482){for(int n=0;n<64&&placed<4482;n++,placed++){int line=placed/2241,z=placed%2241-32;var p=new BlockPos(line*32,-60,z);level.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(p,value(block("create:track"),"shape","zo"),3);}return;}
                if(wait++==0){station(level,0,2160,"CI_DEST_0");station(level,32,2160,"CI_DEST_1");return;}
                if(wait<80)return;
                if(trains.isEmpty()){
                    for(int n=0;n<2;n++){var train=assemble(level,n*32,n);var record=new JsonObject();record.addProperty("id",train.id.toString());record.addProperty("quantity",64*(n+1));mine(level,n,record);trains.add(record);}
                    Files.writeString(RECORD,new GsonBuilder().setPrettyPrinting().create().toJson(trains),StandardOpenOption.CREATE_NEW);return;
                }
            }
            boolean done=true,moving=false;
            for(var record:trains){var train=Create.RAILWAYS.trains.get(UUID.fromString(record.getAsJsonObject().get("id").getAsString()));if(train==null)throw new AssertionError("Scheduled train missing after restart");
                var carriage=train.carriages.getFirst();double z=carriage.getLeadingPoint().getPosition(train.graph).z;long total=carriage.storage.getAllItemStorages().values().stream().filter(s->s instanceof MountedCargo).mapToLong(s->((MountedCargo)s).total()).sum();
                var details=record.getAsJsonObject();
                if(MODE.equals("write")&&details.has("departed")&&!details.get("departed").getAsBoolean()&&total==details.get("quantity").getAsLong()){
                    depart(train,details.get("number").getAsInt());details.addProperty("departed",true);
                    org.slf4j.LoggerFactory.getLogger(RailwayRouteChecks.class).info("ROUTE MINE LOADED train={} cargo={}",train.id,total);
                }
                total+=mineRemaining(level,details);
                if(!MODE.equals("write")&&z>2050&&!details.has("receiver"))receiver(level,train,details);
                if(details.has("receiver")&&ticks%200==0){
                    var f=fixed(details);var dir=Direction.byName(details.get("facing").getAsString());var factory=(com.civitasindustria.common.factory.FactoryBlockEntity)level.getBlockEntity(f.relative(dir,2));var psi=(com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity)level.getBlockEntity(f);var entity=carriage.anyAvailableEntity();
                    org.slf4j.LoggerFactory.getLogger(RailwayRouteChecks.class).info("ROUTE STOCK onboard={} unloader={} factory={} / {} / {} stage={} progress={} loader={} warehouse={} psi={} transfer={} actual-moving={}",total,((CargoBlockEntity)level.getBlockEntity(f.relative(dir))).total(),factory.inventory.getStackInSlot(0),factory.inventory.getStackInSlot(1),factory.inventory.getStackInSlot(2),factory.commissioningStage(),factory.operationProgress(),((CargoBlockEntity)level.getBlockEntity(f.relative(dir,3))).total(),warehouse(level,details).total(),level.getBlockState(f),psi.canTransfer(),entity==null?null:entity.toGlobalVector(new Vec3(1.5,1.5,1.5),1));
                    if(entity!=null)org.slf4j.LoggerFactory.getLogger(RailwayRouteChecks.class).info("ROUTE ACTORS {}",entity.getContraption().getActors().stream().map(a->a.getLeft().state()+" "+a.getLeft().pos()+" "+a.getRight().position+" "+a.getRight().data).toList());
                }
                if(details.has("receiver"))total+=received(level,details);
                if(total!=details.get("quantity").getAsLong())throw new AssertionError("Scheduled cargo conservation failed: "+total+" / "+details);
                if(ticks%200==0)org.slf4j.LoggerFactory.getLogger(RailwayRouteChecks.class).info("ROUTE PROGRESS train={} z={} speed={} target={} conductor={} destination={} derailed={}",train.id,z,train.speed,train.targetSpeed,train.hasForwardConductor(),train.navigation.destination,train.derailed);
                if(z>30)moving=true;
                if(MODE.equals("write")){if(z<1040)done=false;}else if(z<2100||train.getCurrentStation()==null||!details.has("receiver")||warehouse(level,details).total()!=details.get("quantity").getAsLong())done=false;
            }
            if(MODE.equals("inspect")&&ticks>=300){active=false;event.getServer().halt(false);return;}
            if(!moving&&++idle>240)throw new AssertionError("No scheduled movement after repeated navigation attempts");
            if(!done)return;Files.writeString(RECORD,new GsonBuilder().setPrettyPrinting().create().toJson(trains));active=false;Create.RAILWAYS.markTracksDirty();org.slf4j.LoggerFactory.getLogger(RailwayRouteChecks.class).info("CIVITAS PHYSICAL ROUTE PASS: phase={}, trains=2",MODE);event.getServer().halt(false);
        }catch(Exception|AssertionError e){active=false;org.slf4j.LoggerFactory.getLogger(RailwayRouteChecks.class).error("CIVITAS PHYSICAL ROUTE FAILED",e);event.getServer().halt(false);}
    }
}
