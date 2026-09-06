package com.civitasindustria.compat.create;
import com.google.gson.*;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;
import java.util.*;
/** Opt-in disposable API-graph fixture. Tests actual process persistence, not scheduled track operation. */
public final class RailwayRestartChecks {
    private static final Path FIXTURE=Path.of("railway-fixture.json");
    private static final String MODE=System.getProperty("civitas.railwayRestart","");
    private static boolean active;private static int index,warmup;private static JsonArray records;
    public static void register(IEventBus bus){if(MODE.isEmpty())return;bus.addListener(RailwayRestartChecks::start);bus.addListener(RailwayRestartChecks::tick);}
    private static void start(ServerStartedEvent event){
        try{
            if(!Set.of("write","read","verify").contains(MODE))throw new IllegalArgumentException("Unknown railway fixture phase");
            if(MODE.equals("write")){if(Files.exists(FIXTURE))throw new IllegalStateException("Refusing existing fixture");records=new JsonArray();for(int x=-1;x<=21;x++)for(int z=-1;z<=1;z++){event.getServer().overworld().setChunkForced(x,z,true);event.getServer().overworld().getChunk(x,z);}}
            else{records=JsonParser.parseString(Files.readString(FIXTURE)).getAsJsonArray();if(records.size()!=20)throw new IllegalStateException("Incomplete train fixture");}
            active=true;
        }catch(Exception e){throw new RuntimeException("Railway restart fixture setup failed",e);}
    }
    private static void tick(ServerTickEvent.Post event){
        if(!active||warmup++<40)return;var level=event.getServer().overworld();
        try{
            Train train;long quantity=5_000_000_000L+index;
            if(MODE.equals("write")){
                train=CarriageChecks.fixture(level,new BlockPos(index*16,-59,0),quantity);
                var record=new JsonObject();record.addProperty("train",train.id.toString());record.addProperty("quantity",quantity);records.add(record);
            }else{
                var record=records.get(index).getAsJsonObject();train=Create.RAILWAYS.trains.get(UUID.fromString(record.get("train").getAsString()));
                if(train==null)throw new AssertionError("Train missing after restart: "+index);
                if(quantity!=record.get("quantity").getAsLong())throw new AssertionError("Wrong train fixture quantity");
            }
            var carriage=train.carriages.getFirst();var mounted=carriage.storage.getAllItemStorages().values().stream().filter(s->s instanceof MountedCargo).map(s->(MountedCargo)s).findFirst().orElseThrow(()->new AssertionError("Mounted authority missing after restart"));
            long expected=quantity-(MODE.equals("verify")?64:0);
            if(mounted.total()!=expected)throw new AssertionError("Restart count mismatch: "+mounted.total()+" != "+expected);
            double expectedPosition=MODE.equals("write")?5:MODE.equals("read")?1029:2053;
            if(Math.abs(carriage.getLeadingPoint().position-expectedPosition)>.01)throw new AssertionError("Travelling point changed after restart: "+carriage.getLeadingPoint().position);
            if(!MODE.equals("verify"))for(int step=0;step<64;step++)if(Math.abs(carriage.travel(level,train.graph,16,null,null,0)-16)>.01)throw new AssertionError("Restarted train refused travel");
            if(MODE.equals("read")){
                if(mounted.extractItem(0,64,false).getCount()!=64||mounted.total()!=quantity-64)throw new AssertionError("Post-restart cargo extraction lost authority");
            }
            if(++index<20)return;
            if(MODE.equals("write"))Files.writeString(FIXTURE,new GsonBuilder().setPrettyPrinting().create().toJson(records),StandardOpenOption.CREATE_NEW);
            Create.RAILWAYS.markTracksDirty();active=false;
            org.slf4j.LoggerFactory.getLogger(RailwayRestartChecks.class).info("CIVITAS RAILWAY RESTART PASS: phase={}, trains=20, scope=API graph travel and process persistence",MODE);
            event.getServer().halt(false);
        }catch(Exception|AssertionError e){active=false;throw new RuntimeException("Railway restart fixture failed at train "+index+" phase "+MODE,e);}
    }
}
