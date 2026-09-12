package com.civitasindustria.common.threat;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.*;
public final class ThreatDirector {
    private record Member(ResourceLocation dimension,CellPos cell,long expires){}
    private static final Map<UUID,Member> MEMBERS=new HashMap<>();
    private record EventKey(ResourceLocation dimension,CellPos cell){}
    private static final class Event {
        final RaidProgress progress=new RaidProgress();
        final net.minecraft.server.level.ServerBossEvent bar=new net.minecraft.server.level.ServerBossEvent(net.minecraft.network.chat.Component.literal("Industrial raid"),net.minecraft.world.BossEvent.BossBarColor.YELLOW,net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
        ThreatState.Phase previous=ThreatState.Phase.DORMANT;
    }
    private static final Map<EventKey,Event> EVENTS=new HashMap<>();
    private static RaidBudget budget;
    public static int count(ServerLevel level,CellPos cell){return (int)MEMBERS.values().stream().filter(m->m.dimension().equals(level.dimension().location())&&m.cell().equals(cell)).count();}
    private static void fail(Member m){var event=EVENTS.get(new EventKey(m.dimension(),m.cell()));if(event!=null)event.progress.fail();}
    private static void announce(ServerLevel level,CellPos cell,String message){for(var p:level.players())if(CellPos.fromBlock(p.blockPosition().getX(),p.blockPosition().getZ()).equals(cell)){p.displayClientMessage(net.minecraft.network.chat.Component.literal(message),false);p.playNotifySound(net.minecraft.sounds.SoundEvents.RAID_HORN.value(),net.minecraft.sounds.SoundSource.HOSTILE,0.7f,1.0f);}}
    public static void event(ServerLevel level,CellPos cell,ThreatState state){
        EventKey key=new EventKey(level.dimension().location(),cell);
        if(state.phase==ThreatState.Phase.DORMANT){var old=EVENTS.remove(key);if(old!=null)old.bar.removeAllPlayers();return;}
        var event=EVENTS.computeIfAbsent(key,k->new Event());
        var viewers=level.players().stream().filter(p->!p.isSpectator()&&!p.isCreative()&&CellPos.fromBlock(p.blockPosition().getX(),p.blockPosition().getZ()).equals(cell)).toList();
        for(var p:new ArrayList<>(event.bar.getPlayers()))if(!viewers.contains(p))event.bar.removePlayer(p);
        for(var p:viewers)event.bar.addPlayer(p);
        long now=level.getGameTime();int alive=count(level,cell);
        if(state.phase==ThreatState.Phase.WARNING){event.bar.setColor(net.minecraft.world.BossEvent.BossBarColor.YELLOW);event.bar.setName(net.minecraft.network.chat.Component.literal("Industrial raid — prepare: "+Math.max(0,(state.deadline-now+19)/20)+"s"));event.bar.setProgress(Math.clamp((float)(state.deadline-now)/Math.max(1,ServerConfig.WARNING_TICKS.get()),0,1));}
        else if(state.phase==ThreatState.Phase.ACTIVE){
            if(event.previous!=ThreatState.Phase.ACTIVE)announce(level,cell,"Industrial raid has begun. Defend your machinery!");
            if(event.progress.failed()||event.progress.won(alive)){boolean won=event.progress.won(alive);state.phase=ThreatState.Phase.RECOVERY;state.deadline=now+6000;announce(level,cell,won?"Raid defeated — all three waves repelled!":"Raid ended — attackers retreated.");}
            else if(event.progress.ready(now,alive)){wave(level,cell);event.progress.spawned(now,count(level,cell)-alive);}
            alive=count(level,cell);event.bar.setColor(net.minecraft.world.BossEvent.BossBarColor.RED);event.bar.setName(net.minecraft.network.chat.Component.literal("Industrial raid — wave "+event.progress.wave()+"/3 — "+alive+" attackers"));event.bar.setProgress(event.progress.initial()==0?1:Math.clamp((float)alive/event.progress.initial(),0,1));
        }
        if(state.phase==ThreatState.Phase.RECOVERY){
            if(event.previous==ThreatState.Phase.ACTIVE&&!event.progress.won(alive)&&!event.progress.failed())announce(level,cell,"Raid timed out — attackers retreat.");
            for(var id:new ArrayList<>(MEMBERS.keySet())){var m=MEMBERS.get(id);if(m!=null&&m.dimension().equals(key.dimension())&&m.cell().equals(cell)){var entity=level.getEntity(id);if(entity!=null)entity.discard();MEMBERS.remove(id);budget().release(id);}}
            event.bar.setColor(net.minecraft.world.BossEvent.BossBarColor.GREEN);event.bar.setName(net.minecraft.network.chat.Component.literal("Raid recovery — "+Math.max(0,(state.deadline-now+19)/20)+"s"));event.bar.setProgress(Math.clamp((float)(state.deadline-now)/6000,0,1));
        }
        event.previous=state.phase;
    }

    private ThreatDirector(){}
    private static RaidBudget budget(){if(budget==null)budget=new RaidBudget(ServerConfig.GLOBAL_RAID_BUDGET.get(),ServerConfig.RAID_BUDGET.get(),ServerConfig.RAID_CELL_BUDGET.get());return budget;}
    public static boolean online(ServerLevel level,CellPos cell,UUID exclude){
        return level.players().stream().anyMatch(p->!p.getUUID().equals(exclude)&&!p.isSpectator()&&!p.isCreative()&&CellPos.fromBlock(p.blockPosition().getX(),p.blockPosition().getZ()).equals(cell));
    }
    public static void wave(ServerLevel level,CellPos cell){
        if(!online(level,cell,null))return;
        BlockPos target=WorldRuntime.get(level).threatTarget(level,cell);if(target==null)return;
        UUID raid=UUID.nameUUIDFromBytes((level.dimension().location()+":"+cell).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var edges=WorldRuntime.get(level).assaultEdges(cell);if(edges.isEmpty())return;
        int spawned=0;
        // At most 24 loaded candidate probes, independent of town area or edge count.
        for(int attempt=0;attempt<24&&spawned<ServerConfig.RAID_WAVE_SIZE.get();attempt++){
            var edge=edges.get(level.random.nextInt(edges.size()));int along=4+level.random.nextInt(56),distance=4+level.random.nextInt(9);
            int x=edge.blockX(along,distance),z=edge.blockZ(along,distance);
            if(Math.abs((long)x-target.getX())>192||Math.abs((long)z-target.getZ())>192||!level.hasChunk(x>>4,z>>4))continue;
            int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);BlockPos spawn=new BlockPos(x,y,z);
            if(!level.getWorldBorder().isWithinBounds(spawn)||y<=level.getMinBuildHeight()||y+2>=level.getMaxBuildHeight())continue;
            if(!level.getBlockState(spawn.below()).isFaceSturdy(level,spawn.below(),net.minecraft.core.Direction.UP)
                ||!level.getFluidState(spawn.below()).isEmpty()||!level.getBlockState(spawn).isAir()||!level.getBlockState(spawn.above()).isAir())continue;
            var raider=CivitasRegistries.RAIDER.get().create(level);if(raider==null)continue;
            if(!budget().reserve(raider.getUUID(),raid,level.dimension().location().toString(),cell))return;
            raider.configureRole(spawned%3);
            raider.moveTo(x+.5,y,z+.5,level.random.nextFloat()*360,0);
            if(!level.noCollision(raider)){budget().release(raider.getUUID());continue;}
            MEMBERS.put(raider.getUUID(),new Member(level.dimension().location(),cell,level.getGameTime()+ServerConfig.RAID_LIFETIME.get()));
            if(!level.addFreshEntity(raider)){MEMBERS.remove(raider.getUUID());budget().release(raider.getUUID());}else spawned++;
        }
    }
    public static void tick(ServerLevel level){
        for(var id:new ArrayList<>(MEMBERS.keySet())){
            var member=MEMBERS.get(id);if(member==null||!member.dimension().equals(level.dimension().location()))continue;
            var entity=level.getEntity(id);
            // Moving between chunk sections can temporarily hide a living entity from UUID lookup.
            // Keep its bounded reservation until a real leave, expiry or last-player departure.
            if((entity!=null&&!entity.isAlive())||level.getGameTime()>=member.expires()||!online(level,member.cell(),null)){
                if(entity==null||entity.isAlive())fail(member);if(entity!=null)entity.discard();MEMBERS.remove(id);budget().release(id);
            }
        }
    }
    public static void cancelOffline(ServerLevel level){
        var iterator=EVENTS.entrySet().iterator();while(iterator.hasNext()){var entry=iterator.next();if(entry.getKey().dimension().equals(level.dimension().location())&&!online(level,entry.getKey().cell(),null)){entry.getValue().bar.removeAllPlayers();iterator.remove();}}
    }
    public static void register(IEventBus bus){bus.addListener(ThreatDirector::join);bus.addListener(ThreatDirector::leave);bus.addListener(ThreatDirector::logout);}
    private static void join(EntityJoinLevelEvent event){
        if(!event.getLevel().isClientSide&&event.getEntity() instanceof IndustrialRaider&&(event.loadedFromDisk()||!budget().contains(event.getEntity().getUUID())))event.setCanceled(true);
    }
    // NeoForge also fires this on tracking loss, including client tracking loss in an integrated server.
    private static void leave(EntityLeaveLevelEvent event){if(!event.getLevel().isClientSide&&event.getEntity() instanceof IndustrialRaider&&event.getEntity().isRemoved()){var member=MEMBERS.remove(event.getEntity().getUUID());if(member!=null&&event.getEntity().isAlive())fail(member);if(budget!=null)budget.release(event.getEntity().getUUID());}}
    private static void logout(PlayerEvent.PlayerLoggedOutEvent event){
        if(!(event.getEntity().level() instanceof ServerLevel level))return;
        for(var id:new ArrayList<>(MEMBERS.keySet())){
            var member=MEMBERS.get(id);
            if(member!=null&&member.dimension().equals(level.dimension().location())&&!online(level,member.cell(),event.getEntity().getUUID())){
                var entity=level.getEntity(id);if(entity!=null)entity.discard();MEMBERS.remove(id);budget().release(id);
            }
        }
    }
    public static CellPos targetCell(IndustrialRaider raider){
        var member=MEMBERS.get(raider.getUUID());
        return member!=null&&member.dimension().equals(raider.level().dimension().location())?member.cell():null;
    }
    public static int count(){return MEMBERS.size();}
    public static void clear(){EVENTS.values().forEach(e->e.bar.removeAllPlayers());EVENTS.clear();MEMBERS.clear();budget=null;}
}
