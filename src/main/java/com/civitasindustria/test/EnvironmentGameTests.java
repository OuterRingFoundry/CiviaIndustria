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
            var sample=new EnvironmentPayload(ResourceLocation.parse("minecraft:the_nether"),List.of(new EnvironmentPayload.Sample(-1,-2,.3f,.5f,1)));
            EnvironmentPayload.CODEC.encode(buffer,sample);if(buffer.readableBytes()>256)throw new AssertionError("Oversized snapshot");
            if(!sample.equals(EnvironmentPayload.CODEC.decode(buffer)))throw new AssertionError("Snapshot round trip");
            buffer.clear();buffer.writeUtf("minecraft:overworld");buffer.writeByte(255);
            try{EnvironmentPayload.CODEC.decode(buffer);throw new AssertionError("Unbounded snapshot accepted");}catch(IllegalArgumentException expected){}
            try{new EnvironmentPayload.Sample(0,0,Float.NaN,0,0);throw new AssertionError("NaN accepted");}catch(IllegalArgumentException expected){}
        }finally{buffer.release();}
        h.succeed();
    }
    @GameTest(template="empty") public static void offlineRaidAndSavedEntityRefusal(GameTestHelper h){
        int before=ThreatDirector.count();BlockPos pos=h.absolutePos(new BlockPos(1,1,1));
        ThreatDirector.wave(h.getLevel(),CellPos.fromBlock(pos.getX(),pos.getZ()));
        if(ThreatDirector.count()!=before)throw new AssertionError("Offline raid spawned");
        var raider=CivitasRegistries.RAIDER.get().create(h.getLevel());var event=new EntityJoinLevelEvent(raider,h.getLevel(),true);NeoForge.EVENT_BUS.post(event);
        if(!event.isCanceled())throw new AssertionError("Saved raid bypassed warning and budget");h.succeed();
    }
    @GameTest(template="empty",batch="raid_online") public static void onlineRaidReservationAndLogout(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(2,1,2));var cell=CellPos.fromBlock(pos.getX(),pos.getZ());
        var runtime=WorldRuntime.get(level);var player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CIRaid"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        level.setBlock(pos,CivitasRegistries.CONTENT.get("civic_core").get().defaultBlockState(),3);runtime.nodePlaced(pos,player.getUUID(),WorldState.CivicNode.Kind.CORE);
        int before=ThreatDirector.count();level.addNewPlayer(player);
        var box=new net.minecraft.world.phys.AABB(cell.x()*64,level.getMinBuildHeight(),cell.z()*64,cell.x()*64+64,level.getMaxBuildHeight(),cell.z()*64+64);
        try{
            if(!ThreatDirector.online(level,cell,null))throw new AssertionError("Survival player not observed");
            for(int attempt=0;attempt<20&&ThreatDirector.count()==before;attempt++)ThreatDirector.wave(level,cell);
            var raiders=level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true);int spawned=ThreatDirector.count()-before;
            if(spawned<=0||spawned>com.civitasindustria.common.config.ServerConfig.RAID_CELL_BUDGET.get()||raiders.size()!=spawned)throw new AssertionError("Physical raid reservation mismatch: "+spawned+" / "+raiders.size());
            raiders.getFirst().discard();if(ThreatDirector.count()!=before+spawned-1)throw new AssertionError("Entity removal leaked reservation");
            NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
            if(ThreatDirector.count()!=before||!level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true).isEmpty())throw new AssertionError("Logout left physical raiders or reservations");
        }finally{
            level.removePlayerImmediately(player,net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);runtime.nodeRemoved(pos);
            for(var entity:level.getEntities(CivitasRegistries.RAIDER.get(),box,e->true))entity.discard();
        }
        h.succeed();
    }

}
