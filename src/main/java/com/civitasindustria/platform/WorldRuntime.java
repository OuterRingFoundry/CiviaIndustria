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
    private static final Map<ResourceLocation,WorldRuntime> WORLDS=new HashMap<>();
    public final CivitasSavedData saved;
    public final Map<String,RollingMetrics> metrics=new LinkedHashMap<>();
    private final Map<CellPos,Integer> loadedCells=new HashMap<>();
    private final Map<Long,Set<Long>> machinesByChunk=new HashMap<>();
    private final NavigableSet<Long> machines=new TreeSet<>();
    private final Map<Long,Double> machineLoads=new HashMap<>();
    private final NavigableSet<CellPos> active=new TreeSet<>();
    private final Map<CellPos,ThreatState> threats=new HashMap<>();
    private List<CivilizationGraph.Network> networks=List.of();
    private boolean graphDirty=true;
    private long profileGeneration=-1, machineCursor=Long.MIN_VALUE;
    private CellPos cellCursor=new CellPos(Integer.MIN_VALUE,Integer.MIN_VALUE);
    private WorldRuntime(ServerLevel level){
        saved=CivitasSavedData.loadStrict(level);
        for(String key:List.of("civilization","environment","ecology","industrial","threat","cargo","warehouse","parcel","network"))
            metrics.put(key,new RollingMetrics());
    }
    public static WorldRuntime get(ServerLevel level){return WORLDS.computeIfAbsent(level.dimension().location(),key->new WorldRuntime(level));}
    public static void unload(ServerLevel level){WORLDS.remove(level.dimension().location());}
    public static void clear(){WORLDS.clear();}
    public WorldState state(){return saved.state;}
    public void dirty(){saved.setDirty();}
    public CellData cell(CellPos p){
        if(!state().cells.containsKey(p)&&state().cells.size()>=DataMigrationManager.MAX_RECORDS)throw new IllegalStateException("Cell storage limit");
        return state().cells.computeIfAbsent(p,key->new CellData());
    }
    public void emit(CellPos pos,Pollutant pollutant,double amount){cell(pos).add(pollutant,amount);if(loadedCells.containsKey(pos))active.add(pos);dirty();}
    public void chunkLoaded(LevelChunk chunk){
        CellPos cell=CellPos.fromChunk(chunk.getPos().x,chunk.getPos().z);
        loadedCells.merge(cell,1,Integer::sum);if(state().cells.containsKey(cell))active.add(cell);
        Set<Long> positions=new HashSet<>();
        for(BlockEntity entity:chunk.getBlockEntities().values())positions.add(entity.getBlockPos().asLong());
        machinesByChunk.put(chunk.getPos().toLong(),positions);machines.addAll(positions);
        graphDirty=true;
    }
    public void chunkUnloaded(ChunkPos chunk){
        CellPos cell=CellPos.fromChunk(chunk.x,chunk.z);
        loadedCells.computeIfPresent(cell,(p,n)->n<=1?null:n-1);if(!loadedCells.containsKey(cell)){active.remove(cell);threats.remove(cell);}
        Set<Long> positions=machinesByChunk.remove(chunk.toLong());
        if(positions!=null)for(long position:positions){machines.remove(position);setLoad(position,0);}
    }
    public void machineChanged(BlockPos pos){
        long chunk=new ChunkPos(pos).toLong();
        if(machinesByChunk.containsKey(chunk)){machinesByChunk.get(chunk).add(pos.asLong());machines.add(pos.asLong());}
    }
    public void nodePlaced(BlockPos pos,UUID owner,WorldState.CivicNode.Kind kind){
        if(state().nodes.size()>=DataMigrationManager.MAX_RECORDS)throw new IllegalStateException("Node limit");
        state().nodes.put(pos.asLong(),new WorldState.CivicNode(CellPos.fromBlock(pos.getX(),pos.getZ()),owner,kind));graphDirty=true;dirty();
    }
    public void nodeRemoved(BlockPos pos){if(state().nodes.remove(pos.asLong())!=null){graphDirty=true;dirty();}}
    public void recalculate(){graphDirty=true;}
    public List<CivilizationGraph.Network> networks(){return networks;}
    public int dirtyEntries(){return active.size();}
    public double load(BlockPos pos){return IndustrialLoad.effective(state().rawLoad,new IndustrialLoad.Chunk(pos.getX()>>4,pos.getZ()>>4),ServerConfig.CARDINAL.get(),ServerConfig.DIAGONAL.get());}
    private void setLoad(long packed,double value){
        BlockPos pos=BlockPos.of(packed);var chunk=new IndustrialLoad.Chunk(pos.getX()>>4,pos.getZ()>>4);
        double previous=machineLoads.getOrDefault(packed,0.0);
        if(value==0)machineLoads.remove(packed);else machineLoads.put(packed,value);
        double total=Math.max(0,state().rawLoad.getOrDefault(chunk,0.0)+value-previous);
        if(total<.00001)state().rawLoad.remove(chunk);else state().rawLoad.put(chunk,total);
    }
    public void tick(ServerLevel level){
        long time=level.getGameTime();
        if(graphDirty){long start=System.nanoTime();rebuildCivilization();record("network",start,state().nodes.size());}
        if(time%ServerConfig.MAINTENANCE_INTERVAL.get()==0)maintain();
        if(time%ServerConfig.EMISSION_INTERVAL.get()==0)processMachines(level);
        if(time%ServerConfig.ENVIRONMENT_INTERVAL.get()==0)simulate(level);
        if(time%100==0){threat(level);GameplayHooks.environmentEffects(level,this);}
    }
    private void rebuildCivilization(){
        for(var network:networks)for(CellPos pos:network.cells()){CellData c=state().cells.get(pos);if(c!=null)c.civilization=CellData.Civilization.WILDERNESS;}
        Set<CellPos> occupied=new HashSet<>();for(var n:state().nodes.values())occupied.add(n.cell);
        networks=new CivilizationGraph().rebuild(occupied);graphDirty=false;applyCivilization();dirty();
    }
    private void applyCivilization(){
        Map<CellPos,Integer> coreStatus=new HashMap<>();
        for(var node:state().nodes.values())if(node.kind==WorldState.CivicNode.Kind.CORE)coreStatus.merge(node.cell,node.missedPayments,Math::min);
        for(var network:networks){
            int missed=Integer.MAX_VALUE;for(CellPos p:network.cells())missed=Math.min(missed,coreStatus.getOrDefault(p,Integer.MAX_VALUE));
            for(CellPos p:network.cells()){
                boolean edge=List.of(p.offset(1,0),p.offset(-1,0),p.offset(0,1),p.offset(0,-1)).stream().anyMatch(n->!network.cells().contains(n));
                cell(p).civilization=missed==Integer.MAX_VALUE?CellData.Civilization.FRONTIER:
                    missed<2||!edge?CellData.Civilization.CIVILIZED:missed<5?CellData.Civilization.FRONTIER:CellData.Civilization.WILDERNESS;
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
    private void processMachines(ServerLevel level){
        long start=System.nanoTime();
        if(profileGeneration!=EmissionRegistry.INSTANCE.generation()){profileGeneration=EmissionRegistry.INSTANCE.generation();machineLoads.clear();state().rawLoad.clear();}
        int processed=0;
        List<Long> selected=new ArrayList<>(ServerConfig.MACHINE_BUDGET.get());
        for(long p:machines.tailSet(machineCursor,false)){selected.add(p);if(selected.size()>=ServerConfig.MACHINE_BUDGET.get())break;}
        if(selected.isEmpty()){machineCursor=Long.MIN_VALUE;for(long p:machines){selected.add(p);if(selected.size()>=ServerConfig.MACHINE_BUDGET.get())break;}}
        for(long packed:selected){
            machineCursor=packed;BlockPos pos=BlockPos.of(packed);
            if(!level.hasChunkAt(pos))continue;
            var block=level.getBlockState(pos);BlockEntity entity=level.getBlockEntity(pos);
            ResourceLocation id=BuiltInRegistries.BLOCK.getKey(block.getBlock());
            EmissionProfile profile=EmissionRegistry.INSTANCE.get(id);processed++;
            if(profile==null){setLoad(packed,0);continue;}
            setLoad(packed,profile.industrialLoad());
            boolean running=false;
            if(id.getNamespace().equals("create"))running=CreateActivity.active(entity);
            else if(id.getNamespace().equals("immersiveengineering"))running=IEActivity.active(block);
            else if(!profile.activeProperty().isBlank()){
                var property=block.getBlock().getStateDefinition().getProperty(profile.activeProperty());
                running=property!=null&&Boolean.TRUE.equals(block.getValue(property));
            }
            if(running)for(var emission:profile.emissions().entrySet())emit(CellPos.fromBlock(pos.getX(),pos.getZ()),emission.getKey(),emission.getValue());
        }
        record("industrial",start,processed);
    }
    public void simulate(ServerLevel level){
        long start=System.nanoTime();int limit=ServerConfig.MAX_CELLS.get();
        List<CellPos> batch=new ArrayList<>(limit);
        for(CellPos pos:active.tailSet(cellCursor,false)){batch.add(pos);if(batch.size()>=limit)break;}
        if(batch.isEmpty()){cellCursor=new CellPos(Integer.MIN_VALUE,Integer.MIN_VALUE);for(CellPos p:active){batch.add(p);if(batch.size()>=limit)break;}}
        if(!batch.isEmpty())cellCursor=batch.getLast();
        int count=new EnvironmentSimulator().step(state().cells,batch,p->climate(level,p),ServerConfig.simulation(),level.getGameTime());
        // Only changed neighbors join the work set; no historical-world sweep on ordinary ticks.
        for(CellPos p:batch){if(!state().cells.containsKey(p))active.remove(p);
            for(CellPos n:List.of(p,p.offset(1,0),p.offset(-1,0),p.offset(0,1),p.offset(0,-1)))
                if(loadedCells.containsKey(n)&&state().cells.containsKey(n))active.add(n);}
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
        for(var player:level.players())online.add(CellPos.fromBlock(player.blockPosition().getX(),player.blockPosition().getZ()));
        for(CellPos p:online){
            CellData cell=cell(p);double industrial=0;
            for(int x=0;x<4;x++)for(int z=0;z<4;z++)industrial+=state().rawLoad.getOrDefault(new IndustrialLoad.Chunk(p.x()*4+x,p.z()*4+z),0.0);
            cell.threatPressure=Math.max(0,industrial+cell.get(Pollutant.NOISE)*.1-(cell.civilization==CellData.Civilization.CIVILIZED?100:0));
            ThreatState threat=threats.computeIfAbsent(p,k->new ThreatState());var previous=threat.phase;
            threat.update(level.getGameTime(),true,cell.threatPressure,ServerConfig.THREAT_THRESHOLD.get(),ServerConfig.WARNING_TICKS.get(),6000);
            if(previous!=threat.phase&&threat.phase==ThreatState.Phase.WARNING)GameplayHooks.warn(level,p);
            if(previous!=threat.phase&&threat.phase==ThreatState.Phase.ACTIVE)GameplayHooks.raid(level,p);
            cell.lastThreatUpdate=level.getGameTime();
        }
        threats.keySet().removeIf(p->!online.contains(p));record("threat",start,online.size());
    }
    public void record(String name,long start,long entries){metrics.get(name).record(System.currentTimeMillis()/1000,System.nanoTime()-start,entries);}
}
