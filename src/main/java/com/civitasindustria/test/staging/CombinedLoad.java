package com.civitasindustria.test.staging;

import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.threat.ThreatDirector;
import com.civitasindustria.common.warehouse.CargoBlockEntity;
import com.civitasindustria.common.warehouse.FreightBlock;
import com.civitasindustria.compat.create.CarriageChecks;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.WorldRuntime;
import com.google.gson.JsonObject;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import java.util.*;

/** Disposable mixed server workload. Fake players do not measure client/network load. */
final class CombinedLoad {
    private final List<Train> trains = new ArrayList<>();
    private final List<net.minecraft.world.entity.animal.Cow> animals = new ArrayList<>();
    private final List<BlockPos> warehouses = new ArrayList<>();
    private final CellPos raidCell = new CellPos(20,74);
    private int chunks, wait, raidTicks, peakRaid;
    private boolean initialized;
    private double startPosition;

    boolean prepare(ServerLevel level) throws Exception {
        // Actual carriage entities need ticking chunks throughout the measured corridor.
        if(chunks<677){
            int x=chunks<560?127+chunks%14:(chunks-560)%13;
            int z=chunks<560?255+chunks/14:255+(chunks-560)/13;
            level.setChunkForced(x,z,true);level.getChunk(x,z);chunks++;return false;
        }
        if(wait++<40)return false;
        if(initialized)return true;
        initialized=true;var runtime=WorldRuntime.get(level);UUID owner=UUID.randomUUID();
        for(int n=0;n<3;n++)for(int c=0;c<3;c++){
            var pos=new BlockPos(n*320+c*64+32,-60,4384);
            level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);
            String kind=c==0?"civic_core":c==1?"civic_relay":"defense_node";
            level.setBlock(pos,CivitasRegistries.CONTENT.get(kind).get().defaultBlockState(),3);
            runtime.nodePlaced(pos,owner,c==0?WorldState.CivicNode.Kind.CORE:c==1?WorldState.CivicNode.Kind.RELAY:WorldState.CivicNode.Kind.DEFENSE);
            runtime.state().nodes.get(pos.asLong()).credits=1_000_000;
        }
        for(int n=0;n<30;n++){
            var player=FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CIStage"+n));
            int x=n==29?1312:(n/10)*320+32+n%10,z=n==29?4768:4386;
            level.setChunkForced(x>>4,z>>4,true);level.getChunk(x>>4,z>>4);
            player.setGameMode(GameType.SURVIVAL);player.getAbilities().invulnerable=true;
            player.moveTo(x+.5,-59,z+.5,0,0);level.addNewPlayer(player);
        }
        for(int n=0;n<200;n++){
            int x=300+(n%20)*2,z=4358+(n/20)*2;
            level.setChunkForced(x>>4,z>>4,true);level.getChunk(x>>4,z>>4);
            var animal=net.minecraft.world.entity.EntityType.COW.create(level);
            animal.setInvulnerable(true);animal.setPersistenceRequired();animal.setAge(n%2==0?-24000:0);
            animal.moveTo(x+.5,-59,z+.5,0,0);level.addFreshEntity(animal);animals.add(animal);
        }
        for(int n=0;n<20;n++)trains.add(CarriageChecks.fixture(level,new BlockPos(2048+n*8,-59,4100),5_000_000_000L+n));
        startPosition=trains.getFirst().carriages.getFirst().getLeadingPoint().position;
        for(int n=0;n<20;n++){
            var center=new BlockPos(120+n%10*8,-60,4096+n/10*8);warehouses.add(center);
            for(int x=-1;x<=1;x++)for(int z=-3;z<=1;z++){var p=center.offset(x,0,z);level.setChunkForced(p.getX()>>4,p.getZ()>>4,true);}
            level.setBlock(center.north(),CivitasRegistries.CONTENT.get("warehouse_port").get().defaultBlockState(),3);
            level.setBlock(center.north(2),CivitasRegistries.CONTENT.get("cargo_loader").get().defaultBlockState().setValue(FreightBlock.FACING,Direction.SOUTH),3);
            level.setBlock(center.north(3),CivitasRegistries.CONTENT.get("cargo_crate").get().defaultBlockState(),3);
            var source=(CargoBlockEntity)level.getBlockEntity(center.north(3));String[] keys=new String[16];Arrays.fill(keys,"");keys[0]="minecraft:iron_ingot";long[] counts=new long[16];counts[0]=1_000_000;source.restoreMounted(keys,counts);
        }
        var target=new BlockPos(1312,-60,4768);
        level.setBlock(target,CivitasRegistries.CONTENT.get("cargo_crate").get().defaultBlockState(),3);
        ((CargoBlockEntity)level.getBlockEntity(target)).handler.insertItem(0,new ItemStack(Items.IRON_INGOT,64),false);
        return true;
    }

    void tick(ServerLevel level){
        // Native Train.tick performs travel and entity management. Graph-only fixture
        // throttles speed here; physical schedules are tested by RailwayRouteChecks.
        for(var train:trains)train.speed=.4;
        if(ThreatDirector.count()==0)ThreatDirector.wave(level,raidCell);
        int active=ThreatDirector.count();if(active>0)raidTicks++;peakRaid=Math.max(peakRaid,active);
    }

    void report(ServerLevel level,JsonObject report){
        var runtime=WorldRuntime.get(level);long received=0;int serving=0;
        for(var pos:warehouses){
            long total=((CargoBlockEntity)level.getBlockEntity(pos)).total();received+=total;if(total>0)serving++;
            long source=((CargoBlockEntity)level.getBlockEntity(pos.north(3))).total(),buffer=((CargoBlockEntity)level.getBlockEntity(pos.north(2))).total();
            if(total+source+buffer!=1_000_000)throw new AssertionError("Warehouse traffic lost stock at "+pos);
        }
        for(int n=0;n<trains.size();n++){
            long quantity=trains.get(n).carriages.getFirst().storage.getAllItemStorages().values().stream().filter(s->s instanceof com.civitasindustria.compat.create.MountedCargo).mapToLong(s->((com.civitasindustria.compat.create.MountedCargo)s).total()).sum();
            if(quantity!=5_000_000_000L+n)throw new AssertionError("Moving train cargo changed: "+n);
        }
        double travelled=trains.stream().mapToDouble(t->t.carriages.getFirst().getLeadingPoint().position-startPosition).min().orElse(0);
        int networks=runtime.networks().size();
        if(trains.size()!=20||travelled<100||serving!=20||raidTicks<100||level.players().size()!=30||networks<3)throw new AssertionError("Mixed workload incomplete: trains="+trains.size()+" distance="+travelled+" warehouses="+serving+" raidTicks="+raidTicks+" players="+level.players().size()+" networks="+networks);
        long affected=animals.stream().filter(a->a.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getModifier(com.civitasindustria.platform.AnimalPollution.HEALTH)!=null).count();
        int minTicks=animals.stream().mapToInt(a->a.tickCount).min().orElse(0);
        if(animals.size()!=200||animals.stream().anyMatch(a->!a.isAlive()||a.isNoAi())||minTicks<100||affected==0)throw new AssertionError("Animal workload incomplete");
        report.addProperty("animals_with_ai",animals.size());report.addProperty("minimum_animal_ticks",minTicks);report.addProperty("pollution_affected_animals",affected);
        report.addProperty("scope","Combined synthetic server load: 30 fake players, 200 AI-enabled cows, 3 added networks, 20 native moving graph-fixture trains, warehouse traffic and explicitly triggered physical raids; no authenticated clients, client packets, voice or physical railway signals");
        report.addProperty("activity_counters_include_200_tick_warmup",true);report.addProperty("cargo_conserved",true);
        report.addProperty("fake_players",level.players().size());report.addProperty("networks_observed",networks);report.addProperty("moving_trains",20);report.addProperty("minimum_train_travel",travelled);report.addProperty("warehouses_receiving",serving);report.addProperty("warehouse_items_received",received);report.addProperty("raid_active_ticks",raidTicks);report.addProperty("peak_raiders",peakRaid);
    }
}
