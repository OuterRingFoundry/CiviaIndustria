package com.civitasindustria.platform;

import com.civitasindustria.domain.*;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.common.environment.EmissionRegistry;
import com.civitasindustria.api.environment.EmissionProfile;
import com.civitasindustria.compat.create.CreateActivity;
import com.civitasindustria.compat.immersiveengineering.IEActivity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.*;

/** Transient identifiers only: no retained Level, Chunk, Player or BlockEntity references. */
public final class WorldRuntime {
    private static final boolean NATIVE_POLLUTION=net.neoforged.fml.ModList.get().isLoaded("adpother");
    private static final Map<ResourceLocation,WorldRuntime> WORLDS=new HashMap<>();
    public final CivitasSavedData saved;
    public final ResidenceSavedData residences;
    public final EconomySavedData economy;
    public final Map<String,RollingMetrics> metrics=new LinkedHashMap<>();
    private final Set<Long> pendingChunks=new LinkedHashSet<>();
    private final Set<Long> reloadChunks=new HashSet<>();
    private final Map<CellPos,Integer> loadedCells=new HashMap<>();
    private final Map<Long,Set<Long>> machinesByChunk=new HashMap<>();
    private final EmissionClock emissionClock=new EmissionClock();
    private final Map<CellPos,EnumMap<Pollutant,Double>> emissionBuffers=new HashMap<>();
    private final NavigableSet<Long> machines=new TreeSet<>();
    private final Map<Long,Double> machineLoads=new HashMap<>();
    private final NavigableSet<CellPos> active=new TreeSet<>();
    private final Map<CellPos,Map<Long,Integer>> targets=new HashMap<>();
    private final Map<Long,Double> cargoWealth=new HashMap<>();
    private final Map<Long,Long> jams=new HashMap<>();
    private final Map<CellPos,ThreatState> threats=new HashMap<>();
    private List<CivilizationGraph.Network> networks=List.of();
    private final Map<CellPos,CivilizationGraph.Network> networkByCell=new HashMap<>();
    private boolean graphDirty=true;
    private CivilizationGraph.RebuildJob graphJob;
    private long profileGeneration=-1, machineCursor=Long.MIN_VALUE;
    private CellPos cellCursor=new CellPos(Integer.MIN_VALUE,Integer.MIN_VALUE);
    private WorldRuntime(ServerLevel level){
        residences=level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)?ResidenceSavedData.loadStrict(level):null;
        economy=level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)?EconomySavedData.loadStrict(level):get(level.getServer().overworld()).economy;
        saved=CivitasSavedData.loadStrict(level);
        state().nodes.forEach((pos,node)->indexTarget(BlockPos.of(pos),node.kind==WorldState.CivicNode.Kind.DEFENSE?0:1));
        for(String key:List.of("civilization","environment","ecology","industrial","threat","cargo","warehouse","parcel","network","packets","residence"))
            metrics.put(key,new RollingMetrics());
    }
    public static WorldRuntime get(ServerLevel level){return WORLDS.computeIfAbsent(level.dimension().location(),key->new WorldRuntime(level));}
    /** Shutdown must not retry a failed strict load or initialize new authority. */
    public static Optional<WorldRuntime> existing(ServerLevel level){return Optional.ofNullable(WORLDS.get(level.dimension().location()));}
    public static void unload(ServerLevel level){WORLDS.remove(level.dimension().location());}
    public static void clear(){WORLDS.clear();if(NATIVE_POLLUTION)com.civitasindustria.compat.pollution.PollutionBridge.clearSources();}
    public void nativePollutionChanged(CellPos pos){cell(pos);if(loadedCells.containsKey(pos))active.add(pos);}
    public WorldState state(){return saved.state;}
    public void dirty(){saved.setDirty();}
    public CellData cell(CellPos p){
        if(!state().cells.containsKey(p)&&state().cells.size()>=DataMigrationManager.MAX_RECORDS)throw new IllegalStateException("Cell storage limit");
        return state().cells.computeIfAbsent(p,key->new CellData());
    }
    public void emit(CellPos pos,Pollutant pollutant,double amount){CellData target=cell(pos);target.add(pollutant,amount);target.acidPrecursorLoad=target.get(Pollutant.SOX)+target.get(Pollutant.NOX);if(loadedCells.containsKey(pos))active.add(pos);dirty();}
    public void rescanChunk(ChunkPos pos){chunkUnloaded(pos);queueChunk(pos);}
    public void queueChunk(ChunkPos pos){pendingChunks.add(pos.toLong());}
    public void chunkLoaded(LevelChunk chunk){
        if(machinesByChunk.containsKey(chunk.getPos().toLong()))return;
        CellPos cell=CellPos.fromChunk(chunk.getPos().x,chunk.getPos().z);
        loadedCells.merge(cell,1,Integer::sum);if(state().cells.containsKey(cell))active.add(cell);
        state().rawLoad.remove(new IndustrialLoad.Chunk(chunk.getPos().x,chunk.getPos().z));dirty();
        Set<Long> positions=new HashSet<>();
        for(BlockEntity entity:chunk.getBlockEntities().values()){
            if(entity instanceof com.civitasindustria.common.warehouse.CargoBlockEntity cargo){indexTarget(entity.getBlockPos(),cargo.isFreight()?2:4);cargoSignal(entity.getBlockPos(),cargo.total());}
            var profile=EmissionRegistry.INSTANCE.get(BuiltInRegistries.BLOCK.getKey(entity.getBlockState().getBlock()));
            if(profile!=null||entity instanceof com.civitasindustria.common.warehouse.CargoBlockEntity cargo&&cargo.isFreight())positions.add(entity.getBlockPos().asLong());
        }
        machinesByChunk.put(chunk.getPos().toLong(),positions);machines.addAll(positions);
        for(long id:positions)emissionClock.add(id,chunk.getLevel().getGameTime());
        if(NATIVE_POLLUTION && chunk.getLevel() instanceof ServerLevel level)com.civitasindustria.compat.pollution.PollutionBridge.discover(level,this,cell);
    }
    public void chunkUnloaded(ChunkPos chunk){
        pendingChunks.remove(chunk.toLong());reloadChunks.remove(chunk.toLong());
        if(!machinesByChunk.containsKey(chunk.toLong()))return;
        CellPos cell=CellPos.fromChunk(chunk.x,chunk.z);
        loadedCells.computeIfPresent(cell,(p,n)->n<=1?null:n-1);if(!loadedCells.containsKey(cell)){active.remove(cell);threats.remove(cell);}
        var indexed=targets.get(cell);if(indexed!=null){indexed.keySet().removeIf(id->{BlockPos p=BlockPos.of(id);boolean remove=(p.getX()>>4)==chunk.x&&(p.getZ()>>4)==chunk.z&&!state().nodes.containsKey(id);if(remove)cargoWealth.remove(id);return remove;});if(indexed.isEmpty())targets.remove(cell);}
        Set<Long> positions=machinesByChunk.remove(chunk.toLong());
        if(positions!=null)for(long position:positions){machines.remove(position);emissionClock.remove(position);machineLoads.remove(position);removeTarget(BlockPos.of(position));jams.remove(position);}
    }
    public void machineChanged(BlockPos pos){
        long chunk=new ChunkPos(pos).toLong();
        if(machinesByChunk.containsKey(chunk)){machinesByChunk.get(chunk).add(pos.asLong());machines.add(pos.asLong());}
        else queueChunk(new ChunkPos(pos)); // Placement may precede the chunk's first indexed load.
    }
    public void nodePlaced(BlockPos pos,UUID owner,WorldState.CivicNode.Kind kind){
        if(state().nodes.size()>=DataMigrationManager.MAX_RECORDS)throw new IllegalStateException("Node limit");
        indexTarget(pos,kind==WorldState.CivicNode.Kind.DEFENSE?0:1);
        state().nodes.put(pos.asLong(),new WorldState.CivicNode(CellPos.fromBlock(pos.getX(),pos.getZ()),owner,kind));graphDirty=true;dirty();
    }
    public void nodeRemoved(BlockPos pos){removeTarget(pos);if(state().nodes.remove(pos.asLong())!=null){graphDirty=true;dirty();}}
    public void recalculate(){graphDirty=true;}
    public List<CivilizationGraph.Network> networks(){return networks;}
    /** Empty while topology is stale; no interior fallback through newly built walls. */
    public List<CivilizationGraph.Edge> assaultEdges(CellPos cell){
        if(graphDirty||graphJob!=null)return List.of();
        var network=networkByCell.get(cell);
        return network==null?CivilizationGraph.isolatedEdges(cell):network.edges();
    }
    public int dirtyEntries(){return active.size();}
    public double load(BlockPos pos){return IndustrialLoad.effective(state().rawLoad,new IndustrialLoad.Chunk(pos.getX()>>4,pos.getZ()>>4),ServerConfig.CARDINAL.get(),ServerConfig.DIAGONAL.get());}
    private void setLoad(long packed,double value){
        BlockPos pos=BlockPos.of(packed);var chunk=new IndustrialLoad.Chunk(pos.getX()>>4,pos.getZ()>>4);
        double previous=machineLoads.getOrDefault(packed,0.0);
        if(value==0)machineLoads.remove(packed);else machineLoads.put(packed,value);
        double total=Math.max(0,state().rawLoad.getOrDefault(chunk,0.0)+value-previous);
        if(!state().rawLoad.containsKey(chunk)&&state().rawLoad.size()>=DataMigrationManager.MAX_RECORDS)return;
        if(total!=state().rawLoad.getOrDefault(chunk,0.0))dirty();
        if(total<.00001)state().rawLoad.remove(chunk);else state().rawLoad.put(chunk,total);
    }
    public void tick(ServerLevel level){
        long generation=EmissionRegistry.INSTANCE.generation();
        if(profileGeneration!=generation){
            if(profileGeneration!=-1){
                // Rediscover loaded chunks under the ordinary eight-chunk budget. Preserve
                // last-known aggregates for unloaded chunks until they can be observed again.
                reloadChunks.addAll(machinesByChunk.keySet());pendingChunks.addAll(reloadChunks);
            }
            profileGeneration=generation;
            if(NATIVE_POLLUTION)com.civitasindustria.compat.pollution.PollutionBridge.clearSources();
        }
        int discovered=0;
        var pending=pendingChunks.iterator();
        while(pending.hasNext()&&discovered++<8){
            ChunkPos pos=new ChunkPos(pending.next());pending.remove();
            if(reloadChunks.remove(pos.toLong()))chunkUnloaded(pos);
            LevelChunk chunk=level.getChunkSource().getChunkNow(pos.x,pos.z);
            if(chunk!=null)chunkLoaded(chunk);
        }
        long time=level.getGameTime();
        if(graphDirty||graphJob!=null){long start=System.nanoTime();rebuildCivilization();record("network",start,Math.min(1024,state().nodes.size()));}
        if(time%ServerConfig.MAINTENANCE_INTERVAL.get()==0)maintain();
        processMachines(level);
        if(time%ServerConfig.EMISSION_INTERVAL.get()==0)settleEmissions();
        if(!active.isEmpty())simulate(level);
        if(time%100==0)GameplayHooks.environmentEffects(level,this);
        if(time%20==0){com.civitasindustria.common.threat.ThreatDirector.tick(level);threat(level);jams.values().removeIf(until->until<=time);}
    }
    private void rebuildCivilization(){
        if(graphDirty){Set<CellPos> occupied=new HashSet<>();for(var n:state().nodes.values())occupied.add(n.cell);graphJob=new CivilizationGraph.RebuildJob(occupied);graphDirty=false;}
        if(!graphJob.advance(1024))return;
        for(var network:networks)for(CellPos pos:network.cells()){CellData c=state().cells.get(pos);if(c!=null)c.civilization=CellData.Civilization.WILDERNESS;}
        networks=graphJob.result();graphJob=null;networkByCell.clear();
        for(var network:networks)for(var cell:network.cells())networkByCell.put(cell,network);
        applyCivilization();dirty();
    }
    private void applyCivilization(){
        Map<CellPos,Integer> coreStatus=new HashMap<>();
        for(var node:state().nodes.values())if(node.kind==WorldState.CivicNode.Kind.CORE)coreStatus.merge(node.cell,node.missedPayments,Math::min);
        for(var network:networks){
            int missed=Integer.MAX_VALUE;for(CellPos p:network.cells())missed=Math.min(missed,coreStatus.getOrDefault(p,Integer.MAX_VALUE));
            for(CellPos p:network.cells()){
                cell(p).civilization=CivilizationGraph.maintenanceState(missed,network.boundaryDepth().get(p));
            }
        }
    }
    private void maintain(){
        long start=System.nanoTime();
        Map<CellPos,List<WorldState.CivicNode>> byCell=new HashMap<>();
        for(var node:state().nodes.values())byCell.computeIfAbsent(node.cell,p->new ArrayList<>()).add(node);
        for(var network:networks){
            if(network.cells().stream().noneMatch(loadedCells::containsKey))continue;
            long required=(long)Math.ceil(CivilizationGraph.maintenance(network,ServerConfig.MAINTENANCE_BASE.get(),ServerConfig.MAINTENANCE_EDGE.get(),ServerConfig.MAINTENANCE_AREA.get()));
            List<WorldState.CivicNode> payers=new ArrayList<>();
            for(CellPos p:network.cells())for(var n:byCell.getOrDefault(p,List.of()))if(n.kind==WorldState.CivicNode.Kind.CORE||n.kind==WorldState.CivicNode.Kind.MAINTENANCE)payers.add(n);
            long remaining=required;
            for(var n:payers){long paid=Math.min(n.credits,remaining);n.credits-=paid;remaining-=paid;}
            for(var n:payers)if(n.kind==WorldState.CivicNode.Kind.CORE)n.missedPayments=remaining==0?Math.max(0,n.missedPayments-1):Math.min(10000,n.missedPayments+1);
        }
        applyCivilization();dirty();record("civilization",start,networks.size());
    }
    public void settleEmissions(){
        for(var cell:emissionBuffers.entrySet())for(var emission:cell.getValue().entrySet())emit(cell.getKey(),emission.getKey(),emission.getValue());
        emissionBuffers.clear();
    }
    private void processMachines(ServerLevel level){
        long start=System.nanoTime();
        int processed=0;
        List<Long> selected=new ArrayList<>(ServerConfig.MACHINE_BUDGET.get());
        for(long p:machines.tailSet(machineCursor,false)){selected.add(p);if(selected.size()>=ServerConfig.MACHINE_BUDGET.get())break;}
        if(selected.isEmpty()){machineCursor=Long.MIN_VALUE;for(long p:machines){selected.add(p);if(selected.size()>=ServerConfig.MACHINE_BUDGET.get())break;}}
        for(long packed:selected){
            machineCursor=packed;double elapsed=emissionClock.sample(packed,level.getGameTime(),ServerConfig.EMISSION_INTERVAL.get());BlockPos pos=BlockPos.of(packed);
            if(!level.hasChunkAt(pos))continue;
            var block=level.getBlockState(pos);BlockEntity entity=level.getBlockEntity(pos);
            if(entity instanceof com.civitasindustria.common.warehouse.CargoBlockEntity cargo){indexTarget(pos,cargo.isFreight()?2:4);if(cargo.isFreight()&&!jammed(pos,level.getGameTime())){long cargoStart=System.nanoTime();cargo.transfer(level.getGameTime());record("cargo",cargoStart,1);}}
            else if(entity instanceof com.civitasindustria.common.factory.FactoryBlockEntity)indexTarget(pos,3);
            ResourceLocation id=BuiltInRegistries.BLOCK.getKey(block.getBlock());
            EmissionProfile profile=EmissionRegistry.INSTANCE.get(id);processed++;
            if(profile==null){setLoad(packed,0);
                if(entity==null&&!state().nodes.containsKey(packed))removeTarget(pos);
                if(!(entity instanceof com.civitasindustria.common.warehouse.CargoBlockEntity cargo&&cargo.isFreight())){machines.remove(packed);emissionClock.remove(packed);var positions=machinesByChunk.get(new ChunkPos(pos).toLong());if(positions!=null)positions.remove(packed);}
                continue;
            }
            if(id.getNamespace().equals("immersiveengineering")&&!IEActivity.master(entity)){setLoad(packed,0);continue;}
            indexTarget(pos,3);setLoad(packed,profile.industrialLoad());
            boolean running=false;
            if(id.getNamespace().equals("create"))running=CreateActivity.active(entity);
            else if(id.getNamespace().equals("immersiveengineering"))running=IEActivity.active(entity,block);
            else if(!profile.activeProperty().isBlank()){
                var property=block.getBlock().getStateDefinition().getProperty(profile.activeProperty());
                running=property!=null&&Boolean.TRUE.equals(block.getValue(property));
            }
            if(running&&elapsed>0){
                boolean nativeAir=NATIVE_POLLUTION&&com.civitasindustria.compat.pollution.PollutionBridge.ownsAir(entity);
                if(nativeAir)com.civitasindustria.compat.pollution.PollutionBridge.factoryEmission(level,entity,profile,elapsed);
                BlockPos outlet=!nativeAir&&entity instanceof com.civitasindustria.common.factory.FactoryBlockEntity?com.civitasindustria.common.environment.ChimneyOutlet.find(level,pos):pos;
                for(var emission:profile.emissions().entrySet()){
                    if(nativeAir&&(emission.getKey()==Pollutant.PM||emission.getKey()==Pollutant.SOX))continue;
                    BlockPos emissionPos=emission.getKey()==Pollutant.PM||emission.getKey()==Pollutant.SOX||emission.getKey()==Pollutant.NOX?outlet:pos;
                    emissionBuffers.computeIfAbsent(CellPos.fromBlock(emissionPos.getX(),emissionPos.getZ()),p->new EnumMap<>(Pollutant.class)).merge(emission.getKey(),emission.getValue()*elapsed,Double::sum);
                }
            }
        }
        record("industrial",start,processed);
    }
    public void simulate(ServerLevel level){simulate(level,false);}
    public void simulate(ServerLevel level,boolean force){
        long start=System.nanoTime();int limit=force?ServerConfig.MAX_CELLS.get():Math.min(ServerConfig.MAX_CELLS.get(),Math.max(1,(active.size()+ServerConfig.ENVIRONMENT_INTERVAL.get()-1)/ServerConfig.ENVIRONMENT_INTERVAL.get()));
        List<CellPos> batch=new ArrayList<>(limit);
        for(CellPos pos:active.tailSet(cellCursor,false)){batch.add(pos);if(batch.size()>=limit)break;}
        if(batch.isEmpty()){cellCursor=new CellPos(Integer.MIN_VALUE,Integer.MIN_VALUE);for(CellPos p:active){batch.add(p);if(batch.size()>=limit)break;}}
        if(!batch.isEmpty())cellCursor=batch.getLast();
        batch.removeIf(p->{var data=state().cells.get(p);return data==null||!force&&level.getGameTime()-data.lastEnvironmentUpdate<ServerConfig.ENVIRONMENT_INTERVAL.get();});
        if(NATIVE_POLLUTION)for(CellPos pos:batch)com.civitasindustria.compat.pollution.PollutionBridge.expose(level,this,pos);
        int count=new EnvironmentSimulator().step(state().cells,batch,p->climate(level,p),ServerConfig.simulation(),level.getGameTime());
        // Only changed neighbors join the work set; no historical-world sweep on ordinary ticks.
        for(CellPos p:batch){if(!state().cells.containsKey(p))active.remove(p);
            for(CellPos n:List.of(p,p.offset(1,0),p.offset(-1,0),p.offset(0,1),p.offset(0,-1)))
                if(loadedCells.containsKey(n)&&state().cells.containsKey(n))active.add(n);}
        long ecologyStart=System.nanoTime();for(CellPos p:batch){CellData data=state().cells.get(p);if(data!=null)EcologyHooks.sample(level,p,data);}record("ecology",ecologyStart,batch.size());
        if(count>0)dirty();record("environment",start,count);
    }
    public EnvironmentSimulator.Climate climate(ServerLevel level,CellPos cell){
        BlockPos center=new BlockPos(cell.x()*64+32,64,cell.z()*64+32);
        if(!level.hasChunkAt(center))return new EnvironmentSimulator.Climate(false,1,64,false);
        int height=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,center.getX(),center.getZ());
        BlockPos surface=new BlockPos(center.getX(),height,center.getZ());
        boolean water=level.getFluidState(surface.below()).is(FluidTags.WATER);
        var biome=level.getBiome(surface);
        double recovery=biome.is(GameplayHooks.HIGH_RECOVERY)?1.5:biome.is(GameplayHooks.RESISTANT)?1.2:1;
        return new EnvironmentSimulator.Climate(level.isRainingAt(surface),recovery,height,water);
    }
    private void threat(ServerLevel level){
        if(!ServerConfig.THREATS.get())return;
        long start=System.nanoTime();Set<CellPos> online=new HashSet<>();
        for(var player:level.players())if(!player.isCreative()&&!player.isSpectator())online.add(CellPos.fromBlock(player.blockPosition().getX(),player.blockPosition().getZ()));
        for(CellPos p:online){
            CellData cell=state().cells.get(p);if(cell==null){if(!targets.containsKey(p))continue;cell=cell(p);}double industrial=0;
            for(int x=0;x<4;x++)for(int z=0;z<4;z++)industrial+=state().rawLoad.getOrDefault(new IndustrialLoad.Chunk(p.x()*4+x,p.z()*4+z),0.0);
            double defense=0,wealth=0,logistics=0;
            for(var entry:targets.getOrDefault(p,Map.of()).entrySet()){wealth+=cargoWealth.getOrDefault(entry.getKey(),0.0);if(entry.getValue()==2)logistics+=5;}
            for(var entry:targets.getOrDefault(p,Map.of()).entrySet())if(entry.getValue()==0){var node=state().nodes.get(entry.getKey());if(node!=null&&node.credits>0){defense+=25;if(level.getGameTime()%200==0)node.credits--;}}
            cell.threatPressure=Math.min(1_000_000,Math.max(0,industrial-defense+wealth+logistics+cell.degradation*30+cell.get(Pollutant.NOISE)*.1-(cell.civilization==CellData.Civilization.CIVILIZED?100:0)));
            ThreatState threat=threats.computeIfAbsent(p,k->new ThreatState());var previous=threat.phase;
            threat.update(level.getGameTime(),true,cell.threatPressure,ServerConfig.THREAT_THRESHOLD.get(),ServerConfig.WARNING_TICKS.get(),6000);
            if(previous!=threat.phase&&threat.phase==ThreatState.Phase.WARNING)GameplayHooks.warn(level,p);
            com.civitasindustria.common.threat.ThreatDirector.event(level,p,threat);
            cell.lastThreatUpdate=level.getGameTime();dirty();
        }
        threats.keySet().removeIf(p->!online.contains(p));com.civitasindustria.common.threat.ThreatDirector.cancelOffline(level);record("threat",start,online.size());
    }
    private void indexTarget(BlockPos pos,int priority){targets.computeIfAbsent(CellPos.fromBlock(pos.getX(),pos.getZ()),p->new HashMap<>()).put(pos.asLong(),priority);}
    public void cargoSignal(BlockPos pos,long total){if(total>0)cargoWealth.put(pos.asLong(),Math.log10(1.0+total)*2);else cargoWealth.remove(pos.asLong());}
    private void removeTarget(BlockPos pos){cargoWealth.remove(pos.asLong());var cell=CellPos.fromBlock(pos.getX(),pos.getZ());var entries=targets.get(cell);if(entries!=null){entries.remove(pos.asLong());if(entries.isEmpty())targets.remove(cell);}}
    public BlockPos threatTarget(ServerLevel level,CellPos cell){
        BlockPos best=null;int priority=Integer.MAX_VALUE;
        for(var entry:targets.getOrDefault(cell,Map.of()).entrySet()){
            if(entry.getValue()>=priority)continue;BlockPos pos=BlockPos.of(entry.getKey());
            if(level.hasChunkAt(pos)&&!level.getBlockState(pos).isAir()){best=pos;priority=entry.getValue();}
        }
        return best;
    }
    public boolean jammed(BlockPos pos,long now){return jams.getOrDefault(pos.asLong(),0L)>now;}
    public void sabotage(BlockPos pos,long now){sabotage(pos,now,1,100);}
    public void sabotage(BlockPos pos,long now,int creditDamage,int jamTicks){
        var node=state().nodes.get(pos.asLong());if(node!=null){node.credits=Math.max(0,node.credits-Math.clamp(creditDamage,1,2));dirty();}
        else if(machines.contains(pos.asLong()))jams.put(pos.asLong(),now+Math.clamp(jamTicks,20,200));
    }
    public void record(String name,long start,long entries){metrics.get(name).record(System.currentTimeMillis()/1000,System.nanoTime()-start,entries);}
}
