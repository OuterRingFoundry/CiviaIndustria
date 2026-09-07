package com.civitasindustria.test;
import com.civitasindustria.domain.*;
import java.util.*;

public final class DomainTests {
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    private static void rejects(Runnable code){boolean thrown=false;try{code.run();}catch(IllegalArgumentException e){thrown=true;}check(thrown,"Invalid input accepted");}
    private static SimulationSettings settings(){return new SimulationSettings(.01,.12,.08,.1,.002,.003,.025,.3,1,0,.00001,1000);}
    public static void main(String[]args){
        if(Arrays.asList(args).contains("--verify-failure"))throw new AssertionError("Intentional domain failure propagation probe");
        check(CellPos.fromBlock(-1,-64).equals(new CellPos(-1,-1)),"Negative blocks");
        check(CellPos.fromBlock(-65,63).equals(new CellPos(-2,0)),"Cell boundaries");
        check(CellPos.fromChunk(-1,-5).equals(new CellPos(-1,-2)),"Negative chunks");
        boolean coherent=true;int mineralCount=0,changedSeed=0,changedOre=0;
        for(int x=-50;x<50;x++)for(int z=-50;z<50;z++){
            boolean selected=MineralRegions.contains(1234,x*256,z*256,16,25,101);
            if(selected)mineralCount++;
            coherent &= selected==MineralRegions.contains(1234,x*256+255,z*256+255,16,25,101);
            if(selected!=MineralRegions.contains(5678,x*256,z*256,16,25,101))changedSeed++;
            if(selected!=MineralRegions.contains(1234,x*256,z*256,16,25,202))changedOre++;
        }
        check(coherent,"Mineral regions split within a region, including negative coordinates");
        check(mineralCount>2300&&mineralCount<2700,"Mineral region coverage outside 23–27 percent over 10000 regions");
        check(changedSeed>3000&&changedOre>3000,"Mineral regions must depend on world seed and ore salt");
        check(!MineralRegions.contains(Long.MIN_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE,256,0,0)&&MineralRegions.contains(Long.MAX_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE,1,100,0),"Mineral extreme coordinates/coverage");
        rejects(()->MineralRegions.contains(0,0,0,0,25,0));
        rejects(()->MineralRegions.contains(0,0,0,16,101,0));
        Set<CellPos> square=new HashSet<>();for(int x=0;x<4;x++)for(int z=0;z<4;z++)square.add(new CellPos(x,z));
        var graph=new CivilizationGraph();check(graph.rebuild(square).getFirst().perimeter()==16,"Square perimeter");
        check(graph.rebuild(square).getFirst().boundaryDepth().get(new CellPos(1,1))==1,"Interior boundary depth");
        check(CivilizationGraph.maintenanceState(5,1)==CellData.Civilization.FRONTIER,"Maintenance failure moves inward gradually");
        check(CivilizationGraph.maintenanceState(8,1)==CellData.Civilization.WILDERNESS,"Interior eventually degrades");
        check(CivilizationGraph.maintenanceState(0,1)==CellData.Civilization.CIVILIZED,"Maintenance recovery");
        square.remove(new CellPos(1,1));check(graph.rebuild(square).getFirst().perimeter()==20,"Interior holes");
        CivilizationGraph.RebuildJob job=new CivilizationGraph.RebuildJob(square);int graphSteps=0;while(!job.advance(2))graphSteps++;
        check(graphSteps>1&&job.result().getFirst().perimeter()==20,"Incremental graph preserves holes and yields");
        Map<IndustrialLoad.Chunk,Double> loads=Map.of(new IndustrialLoad.Chunk(0,0),100.0,new IndustrialLoad.Chunk(1,0),100.0);
        check(IndustrialLoad.effective(loads,new IndustrialLoad.Chunk(0,0),.35,.15)==135,"Border load");
        BulkInventory source=new BulkInventory(4,10_000_000_000L),target=new BulkInventory(4,10_000_000_000L);
        source.insert("minecraft:iron_ingot",5_000_000_000L,false);
        check(source.transferTo(target,"minecraft:iron_ingot",3_000_000_000L)==3_000_000_000L,"Long transfer");
        check(source.total()+target.total()==5_000_000_000L,"Cargo conservation");
        check(source.transferTo(source,"minecraft:iron_ingot",1)==0,"Self transfer");
        check(source.extract("minecraft:iron_ingot",10,true)==10&&source.total()==2_000_000_000L,"Simulation is read-only");
        rejects(()->source.insert("bad",-1,false));
        check(DataMigrationManager.decodeInventory(DataMigrationManager.encodeInventory(target)).total()==3_000_000_000L,"Cargo roundtrip");
        WorldState state=new WorldState();CellPos origin=new CellPos(-1,-2);CellData c=new CellData();
        c.add(Pollutant.SOX,100);c.add(Pollutant.NOX,80);c.add(Pollutant.PM,40);state.cells.put(origin,c);
        UUID owner=UUID.randomUUID();var node=new WorldState.CivicNode(origin,owner,WorldState.CivicNode.Kind.CORE);node.credits=1234;state.nodes.put(1L,node);
        state.parcels.add(new Parcel(UUID.randomUUID(),owner,-20,0,-20,20,10,20,"Ground",Set.of(),Set.of()));
        state.parcels.add(new Parcel(UUID.randomUUID(),UUID.randomUUID(),-20,11,-20,20,20,20,"Upper",Set.of(),Set.of()));
        check(state.parcels.at(-1,11,-1).isPresent(),"3D parcel index");
        rejects(()->state.parcels.add(new Parcel(UUID.randomUUID(),owner,0,5,0,2,8,2,"Overlap",Set.of(),Set.of())));
        state.rawLoad.put(new IndustrialLoad.Chunk(-99,101),234.5);
        byte[] encoded=DataMigrationManager.encode(state);
        check(DataMigrationManager.decode(encoded).rawLoad.equals(state.rawLoad),"Industrial chunk persistence");
        WorldState old=new WorldState();byte[] upgrade=DataMigrationManager.encode(old);upgrade=Arrays.copyOf(upgrade,upgrade.length-4);java.nio.ByteBuffer.wrap(upgrade).putInt(4,2);
        check(DataMigrationManager.decode(upgrade).rawLoad.isEmpty(),"Explicit v2 migration without industrial records");
        check(Arrays.equals(encoded,DataMigrationManager.encode(DataMigrationManager.decode(encoded))),"Deterministic restart identity");
        byte[] future=encoded.clone();future[7]=99;rejects(()->DataMigrationManager.decode(future));
        rejects(()->DataMigrationManager.decode(Arrays.copyOf(encoded,20)));
        var simulator=new EnvironmentSimulator();var rain=new EnvironmentSimulator.Climate(true,1,64,true);
        simulator.step(state.cells,List.of(origin),p->rain,settings(),200);
        check(c.get(Pollutant.SOX)<100&&c.get(Pollutant.SOIL_ACIDITY)>0&&c.get(Pollutant.W_ACIDITY)>0,"Acid deposition");
        check(c.vegetationHealth<1,"Gradual ecological injury");
        double injured=c.vegetationHealth;Arrays.fill(c.pollutants,0);
        for(int i=0;i<300;i++)simulator.step(state.cells,List.of(origin),p->rain,settings(),400+i*200);
        check(c.vegetationHealth>injured,"Ecological recovery");
        Map<CellPos,CellData> water=new TreeMap<>();CellData polluted=new CellData();polluted.add(Pollutant.W_INDUSTRIAL,100);water.put(new CellPos(0,0),polluted);
        simulator.step(water,List.of(new CellPos(0,0)),p->new EnvironmentSimulator.Climate(false,1,-p.x(),true),settings(),200);
        check(water.containsKey(new CellPos(1,0))&&!water.containsKey(new CellPos(-1,0)),"Downhill water transport");
        ThreatState threat=new ThreatState();threat.update(0,false,100,50,12000,6000);check(threat.phase==ThreatState.Phase.DORMANT,"Offline threat");
        threat.update(1,true,100,50,12000,6000);check(threat.phase==ThreatState.Phase.WARNING,"Return warning");
        threat.update(12000,true,100,50,12000,6000);check(threat.phase==ThreatState.Phase.WARNING,"Warning duration");
        threat.update(12001,true,100,50,12000,6000);check(threat.phase==ThreatState.Phase.ACTIVE,"Threat activation");
        Map<CellPos,CellData> large=new TreeMap<>();List<CellPos> active=new ArrayList<>();
        for(int i=0;i<10000;i++){var pos=new CellPos(i*3,0);CellData cell=new CellData();cell.add(Pollutant.SOX,1);large.put(pos,cell);if(i<500)active.add(pos);}
        long start=System.nanoTime();check(simulator.step(large,active,p->rain,settings(),200)==500,"Active-set bound");
        SimulationSettings conservative=new SimulationSettings(0,1,0,0,0,0,0,0,0,0,.00000001,1000);
        Map<CellPos,CellData> capped=new TreeMap<>();CellPos cp=new CellPos(0,0);CellData cc=new CellData();cc.add(Pollutant.PM,100);capped.put(cp,cc);
        simulator.step(capped,List.of(cp,cp),p->rain,conservative,1,1);
        check(capped.size()==1&&cc.get(Pollutant.PM)==100,"Record cap retains refused transport and deduplicates work");
        Map<CellPos,CellData> saturated=new TreeMap<>();
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){CellData cell=new CellData();cell.add(Pollutant.PM,1_000_000);saturated.put(new CellPos(x,z),cell);}
        double beforeMass=saturated.values().stream().mapToDouble(v->v.get(Pollutant.PM)).sum();
        simulator.step(saturated,List.of(cp),p->rain,conservative,1);
        check(saturated.values().stream().mapToDouble(v->v.get(Pollutant.PM)).sum()==beforeMass,"Saturated neighbors do not delete transported mass");
        Map<CellPos,CellData> cleanWater=new TreeMap<>();cleanWater.put(cp,new CellData());
        simulator.step(cleanWater,List.of(cp),p->rain,conservative,2);
        check(cleanWater.isEmpty(),"Zero runoff creates no pristine neighbor records");
        Map<CellPos,CellData> trace=new TreeMap<>();CellData tiny=new CellData();tiny.add(Pollutant.PM,1e-9);trace.put(cp,tiny);
        simulator.step(trace,List.of(cp),p->rain,conservative,2);
        check(trace.isEmpty(),"Sub-epsilon transport recipients are compacted");
        EmissionClock clock=new EmissionClock();for(int i=0;i<5000;i++)clock.add(i,0);
        double settled=0;for(int i=0;i<5000;i++)settled+=clock.sample(i,100,100);
        check(settled==5000,"Sampling batches preserve steady-state emission totals");
        clock.remove(1);clock.add(1,1000);check(clock.sample(1,1001,100)==.01,"Unloaded machines emit no offline backlog");
        Commissioning commission=new Commissioning();check(!commission.begin(false,true),"Foundation commissioning gate");
        check(commission.begin(true,true),"Commissioning starts");commission.update(true,true,100,200);check(commission.stage==Commissioning.Stage.COMMISSIONING,"Commissioning duration");
        commission.update(true,true,100,200);check(commission.stage==Commissioning.Stage.READY,"Commissioning completes");
        commission.update(false,true,20,200);check(commission.stage==Commissioning.Stage.DEGRADED,"Foundation removal degrades capital");
        RaidBudget raids=new RaidBudget(3,2,1);UUID raid=UUID.randomUUID(),id=UUID.randomUUID();
        check(raids.reserve(id,raid,"overworld",origin),"Raid reservation");
        check(!raids.reserve(UUID.randomUUID(),raid,"overworld",origin),"Cell raid cap");
        check(raids.reserve(UUID.randomUUID(),raid,"nether",origin),"Dimension isolated raid cells");
        check(!raids.reserve(UUID.randomUUID(),raid,"end",origin),"Per raid cap");
        check(raids.reserve(UUID.randomUUID(),UUID.randomUUID(),"end",origin),"Independent raid");
        check(!raids.reserve(UUID.randomUUID(),UUID.randomUUID(),"other",origin),"Global raid cap");
        raids.release(id);check(raids.reserve(UUID.randomUUID(),UUID.randomUUID(),"overworld",origin),"Released capacity reused");
        System.out.println("Domain tests: "+checks+" checks passed; 500-cell rain step "+(System.nanoTime()-start)/1_000_000.0+" ms");
    }
}
