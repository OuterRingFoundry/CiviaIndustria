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
}
