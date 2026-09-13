package com.civitasindustria.test;
import com.civitasindustria.platform.EconomySavedData;
import java.nio.file.*;
import java.util.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
/** Opt-in process restart fixture, using the real SavedData lifecycle. */
public final class EconomyRestartChecks {
    private static final String MODE=System.getProperty("civitas.economyRestart", "");
    private static final Path SNAPSHOT=Path.of("economy-fixture.bin");
    private static final UUID OWNER=UUID.fromString("37c2cb30-d0fa-4e69-bf12-4c8499e16f18"),OTHER=UUID.fromString("bc5e1605-f9ad-4f3c-ae51-87853ad53b9c");
    public static void register(IEventBus bus){if(!MODE.isEmpty())bus.addListener(EconomyRestartChecks::start);}
    private static void start(ServerStartedEvent event){try{
        var server=event.getServer();var saved=EconomySavedData.get(server.overworld());var ledger=saved.registry;
        if(saved!=EconomySavedData.get(server.getLevel(Level.NETHER))||saved!=EconomySavedData.get(server.getLevel(Level.END)))throw new AssertionError("Multiple currency authorities");
        if(MODE.equals("write")){
            if(Files.exists(SNAPSHOT)||ledger.issued()!=0)throw new AssertionError("Fixture requires an empty disposable economy");
            for(int i=0;i<3;i++)if(!ledger.deposit(OWNER))throw new AssertionError("Deposit");
            if(!ledger.fund(OWNER)||!ledger.sell(OWNER,0)||!ledger.transfer(OWNER,OTHER,47))throw new AssertionError("Trade and transfer fixture");
            ledger.day(100,server.overworld().getSeed());ledger.day(101,server.overworld().getSeed());
        }else{
            if(!Set.of("read","verify").contains(MODE)||!Arrays.equals(Files.readAllBytes(SNAPSHOT),ledger.encode()))throw new AssertionError("Ledger changed across process restart");
            if(MODE.equals("read")&&(!ledger.redeem(OWNER)||!ledger.buy(OWNER,0)))throw new AssertionError("Restored ledger cannot redeem/trade");
        }
        saved.setDirty();Files.write(SNAPSHOT,ledger.encode());
        org.slf4j.LoggerFactory.getLogger(EconomyRestartChecks.class).info("CIVITAS ECONOMY RESTART PASS: phase={}, reserve={}, issued={}, treasury={}, index={}",MODE,ledger.reserve(),ledger.issued(),ledger.treasury(),ledger.index());
        server.halt(false);
    }catch(Exception|AssertionError e){throw new RuntimeException("Economy restart fixture failed in "+MODE,e);}}
}
