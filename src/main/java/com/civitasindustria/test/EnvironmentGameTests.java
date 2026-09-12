package com.civitasindustria.test;
import com.civitasindustria.common.network.EnvironmentPayload;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.threat.ThreatDirector;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import io.netty.buffer.Unpooled;
import java.util.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class EnvironmentGameTests {
    @GameTest(template="empty") public static void boundedSnapshotCodec(GameTestHelper h){
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());
        try{
            var sample=new EnvironmentPayload(ResourceLocation.parse("minecraft:the_nether"),List.of(new EnvironmentPayload.Sample(-1,-2,.3f,.5f,1,.8f)));
            EnvironmentPayload.CODEC.encode(buffer,sample);if(buffer.readableBytes()>256)throw new AssertionError("Oversized snapshot");
            if(!sample.equals(EnvironmentPayload.CODEC.decode(buffer)))throw new AssertionError("Snapshot round trip");
            buffer.clear();buffer.writeUtf("minecraft:overworld");buffer.writeByte(255);
            try{EnvironmentPayload.CODEC.decode(buffer);throw new AssertionError("Unbounded snapshot accepted");}catch(IllegalArgumentException expected){}
            try{new EnvironmentPayload.Sample(0,0,Float.NaN,0,0,0);throw new AssertionError("NaN accepted");}catch(IllegalArgumentException expected){}
        }finally{buffer.release();}
        h.succeed();
    }
    @GameTest(template="empty") public static void offlineRaidAndSavedEntityRefusal(GameTestHelper h){
        int before=ThreatDirector.count();BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        ThreatDirector.wave(h.getLevel(),CellPos.fromBlock(pos.getX(),pos.getZ()));
        if(ThreatDirector.count()!=before)throw new AssertionError("Offline raid spawned");
        var raider=CivitasRegistries.RAIDER.get().create(h.getLevel());var event=new EntityJoinLevelEvent(raider,h.getLevel(),true);NeoForge.EVENT_BUS.post(event);
        if(!event.isCanceled())throw new AssertionError("Saved raid bypassed warning and budget");
        raider.tick();if(!raider.isRemoved())throw new AssertionError("Unassigned construct resumed live AI");h.succeed();
    }
    @GameTest(template="empty",batch="raid_online") public static void onlineRaidReservationAndLogout(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(2,1,2));var cell=CellPos.fromBlock(pos.getX(),pos.getZ());
        var runtime=WorldRuntime.get(level);var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CIRaid"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        level.setBlock(pos,CivitasRegistries.CONTENT.get("civic_core").get().defaultBlockState(),3);runtime.nodePlaced(pos,player.getUUID(),WorldState.CivicNode.Kind.CORE);
        int before=ThreatDirector.count();level.addNewPlayer(player);runtime.tick(level);
        var box=new net.minecraft.world.phys.AABB(cell.x()*64,level.getMinBuildHeight(),cell.z()*64,cell.x()*64+64,level.getMaxBuildHeight(),cell.z()*64+64).inflate(200,0,200);
        try{
            if(!ThreatDirector.online(level,cell,null))throw new AssertionError("Survival player not observed");
            for(int attempt=0;attempt<20&&ThreatDirector.count()==before;attempt++)ThreatDirector.wave(level,cell);
            var raiders=level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true);int spawned=ThreatDirector.count()-before;
            if(spawned<=0||spawned>com.civitasindustria.common.config.ServerConfig.RAID_CELL_BUDGET.get()||raiders.size()!=spawned)throw new AssertionError("Physical raid reservation mismatch: "+spawned+" / "+raiders.size());
            for(var raider:raiders){
                if(!cell.equals(ThreatDirector.targetCell(raider)))throw new AssertionError("Approaching raider lost intended target district");
                if(CellPos.fromBlock(raider.blockPosition().getX(),raider.blockPosition().getZ()).equals(cell))throw new AssertionError("Raider spawned inside isolated civic cell");
            }
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent(raiders.getFirst(),level));
            if(ThreatDirector.count()!=before+spawned||!cell.equals(ThreatDirector.targetCell(raiders.getFirst())))throw new AssertionError("Tracking loss released a living raid member");
            raiders.getFirst().discard();if(ThreatDirector.count()!=before+spawned-1)throw new AssertionError("Entity removal leaked reservation");
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
            if(ThreatDirector.count()!=before||!level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true).isEmpty())throw new AssertionError("Logout left physical raiders or reservations");
        }finally{
            level.removePlayerImmediately(player,net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);runtime.nodeRemoved(pos);
            for(var entity:level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true))entity.discard();
        }
        h.succeed();
    }

    @GameTest(template="empty",batch="raid_siege",timeoutTicks=240) public static void crossBorderWindupInterruption(GameTestHelper h){
        var level=h.getLevel();var anchor=h.absolutePos(new BlockPos(2,1,2));
        // Keep this live-AI scene away from other batches' indexed infrastructure.
        var cell=new CellPos(-201318,-33799); // Reproduce the negative-coordinate scene from the failing run.
        var pos=new BlockPos(cell.x()*64+1,anchor.getY(),cell.z()*64+32);
        var runtime=WorldRuntime.get(level);var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CISiege"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.moveTo(pos.getX()+16,pos.getY(),pos.getZ(),0,0);
        var box=new net.minecraft.world.phys.AABB(pos).inflate(220);
        var forced=new ArrayList<net.minecraft.world.level.ChunkPos>();
        for(var chunk:List.of(new net.minecraft.world.level.ChunkPos(pos),new net.minecraft.world.level.ChunkPos(pos.west(2)))){
            if(!level.getForcedChunks().contains(chunk.toLong())){level.setChunkForced(chunk.x,chunk.z,true);forced.add(chunk);}
        }
        Runnable cleanup=()->{
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
            level.removePlayerImmediately(player,net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);runtime.nodeRemoved(pos);
            level.removeBlock(pos,false);
            for(var chunk:forced)level.setChunkForced(chunk.x,chunk.z,false);
            for(var entity:level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true))entity.discard();
        };
        try{
            for(int x=-4;x<=3;x++)for(int z=-4;z<=4;z++){
                level.setBlock(pos.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                for(int y=0;y<4;y++)level.setBlock(pos.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
            }
            level.setBlock(pos,CivitasRegistries.CONTENT.get("civic_core").get().defaultBlockState(),3);
            runtime.nodePlaced(pos,player.getUUID(),WorldState.CivicNode.Kind.CORE);runtime.state().nodes.get(pos.asLong()).credits=1000;
            level.addNewPlayer(player);runtime.tick(level);
            if(!pos.equals(runtime.threatTarget(level,cell)))throw new AssertionError("Siege target is not isolated");
            for(int attempt=0;attempt<20&&level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true).isEmpty();attempt++)ThreatDirector.wave(level,cell);
            var raiders=level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true);
            if(raiders.isEmpty())throw new AssertionError("No perimeter raider for siege fixture");
            var raider=raiders.getFirst();for(var other:raiders)if(other!=raider)other.discard();
            var origin=new net.minecraft.world.level.ChunkPos(raider.blockPosition());
            if(!level.getForcedChunks().contains(origin.toLong())){level.setChunkForced(origin.x,origin.z,true);forced.add(origin);}
            raider.configureRole(2);raider.moveTo(pos.getX()-1.25,pos.getY(),pos.getZ()+.5,0,0);
            raider.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);
            if(CellPos.fromBlock(raider.blockPosition().getX(),raider.blockPosition().getZ()).equals(cell))throw new AssertionError("Fixture did not cross cell border");
            final long[] started={-1};
            h.onEachTick(()->{try{
                if(started[0]<0){
                    if(!raider.windingUp())return;
                    if(raider.tickCount==0)throw new AssertionError("Fixture requires natural entity ticks");
                    started[0]=level.getGameTime();
                    for(int z=-3;z<=3;z++)for(int y=0;y<3;y++)level.setBlock(pos.offset(-1,y,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
                    return;
                }
                long elapsed=level.getGameTime()-started[0];
                if(elapsed==40){
                    if(raider.windingUp()||runtime.state().nodes.get(pos.asLong()).credits!=1000)throw new AssertionError("Occluded windup damaged infrastructure");
                    for(int z=-3;z<=3;z++)for(int y=0;y<3;y++)level.removeBlock(pos.offset(-1,y,z),false);
                }
                if(elapsed==90){
                    if(runtime.state().nodes.get(pos.asLong()).credits>=1000)throw new AssertionError("Removing obstruction did not restore sabotage");
                    cleanup.run();h.succeed();
                }
            }catch(RuntimeException|Error e){cleanup.run();throw e;}});
            h.runAfterDelay(230,()->{String details="Unfinished siege: ticks="+raider.tickCount+" pos="+raider.position()+" assigned="+ThreatDirector.targetCell(raider);cleanup.run();throw new AssertionError(details);});

        }catch(RuntimeException|Error e){cleanup.run();throw e;}
    }

}
