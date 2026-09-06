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
    private static RaidBudget budget;
    private ThreatDirector(){}
    private static RaidBudget budget(){if(budget==null)budget=new RaidBudget(ServerConfig.GLOBAL_RAID_BUDGET.get(),ServerConfig.RAID_BUDGET.get(),ServerConfig.RAID_CELL_BUDGET.get());return budget;}
    public static boolean online(ServerLevel level,CellPos cell,UUID exclude){
        return level.players().stream().anyMatch(p->!p.getUUID().equals(exclude)&&!p.isSpectator()&&!p.isCreative()&&CellPos.fromBlock(p.blockPosition().getX(),p.blockPosition().getZ()).equals(cell));
    }
    public static void wave(ServerLevel level,CellPos cell){
        if(!online(level,cell,null))return;
        BlockPos target=WorldRuntime.get(level).threatTarget(level,cell);if(target==null)return;
        UUID raid=UUID.nameUUIDFromBytes((level.dimension().location()+":"+cell).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        for(int attempt=0;attempt<ServerConfig.RAID_WAVE_SIZE.get();attempt++){
            int x=target.getX()+level.random.nextInt(33)-16,z=target.getZ()+level.random.nextInt(33)-16;
            if(!CellPos.fromBlock(x,z).equals(cell)||!level.hasChunk(x>>4,z>>4))continue;
            int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);BlockPos spawn=new BlockPos(x,y,z);
            if(!level.getFluidState(spawn.below()).isEmpty()||!level.getBlockState(spawn).isAir()||!level.getBlockState(spawn.above()).isAir())continue;
            var raider=CivitasRegistries.RAIDER.get().create(level);if(raider==null)continue;
            if(!budget().reserve(raider.getUUID(),raid,level.dimension().location().toString(),cell))return;
            raider.moveTo(x+.5,y,z+.5,level.random.nextFloat()*360,0);
            if(!level.noCollision(raider)){budget().release(raider.getUUID());continue;}
            MEMBERS.put(raider.getUUID(),new Member(level.dimension().location(),cell,level.getGameTime()+ServerConfig.RAID_LIFETIME.get()));
            if(!level.addFreshEntity(raider)){MEMBERS.remove(raider.getUUID());budget().release(raider.getUUID());}
        }
    }
    public static void tick(ServerLevel level){
        for(var id:new ArrayList<>(MEMBERS.keySet())){
            var member=MEMBERS.get(id);if(member==null||!member.dimension().equals(level.dimension().location()))continue;
            var entity=level.getEntity(id);
            if(entity==null||!entity.isAlive()||level.getGameTime()>=member.expires()||!online(level,member.cell(),null)){
                if(entity!=null)entity.discard();MEMBERS.remove(id);budget().release(id);
            }
        }
    }
    public static void register(IEventBus bus){bus.addListener(ThreatDirector::join);bus.addListener(ThreatDirector::leave);bus.addListener(ThreatDirector::logout);}
    private static void join(EntityJoinLevelEvent event){
        if(!event.getLevel().isClientSide&&event.getEntity() instanceof IndustrialRaider&&(event.loadedFromDisk()||!budget().contains(event.getEntity().getUUID())))event.setCanceled(true);
    }
    private static void leave(EntityLeaveLevelEvent event){if(event.getEntity() instanceof IndustrialRaider){MEMBERS.remove(event.getEntity().getUUID());if(budget!=null)budget.release(event.getEntity().getUUID());}}
    private static void logout(PlayerEvent.PlayerLoggedOutEvent event){
        if(!(event.getEntity().level() instanceof ServerLevel level))return;
        for(var id:new ArrayList<>(MEMBERS.keySet())){
            var member=MEMBERS.get(id);
            if(member!=null&&member.dimension().equals(level.dimension().location())&&!online(level,member.cell(),event.getEntity().getUUID())){
                var entity=level.getEntity(id);if(entity!=null)entity.discard();MEMBERS.remove(id);budget().release(id);
            }
        }
    }
    public static int count(){return MEMBERS.size();}
    public static void clear(){MEMBERS.clear();budget=null;}
}
