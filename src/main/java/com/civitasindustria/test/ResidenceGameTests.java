package com.civitasindustria.test;

import com.civitasindustria.domain.*;
import com.civitasindustria.platform.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;
import java.nio.file.*;
import java.util.*;

@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class ResidenceGameTests {
    @GameTest(template="empty")
    public static void residenceCommandsAndRevocation(GameTestHelper h) throws Exception {
        var level = h.getLevel();
        BlockPos pos = h.absolutePos(new BlockPos(1, 1, 1));
        UUID id = UUID.randomUUID();
        var player = FakePlayerFactory.get(level, new GameProfile(id, "CIResident"));
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        player.setGameMode(GameType.SURVIVAL);
        Parcel parcel = new Parcel(UUID.randomUUID(), UUID.randomUUID(), pos.getX(), pos.getY(), pos.getZ(),
                pos.getX()+2, pos.getY()+2, pos.getZ()+2, "residence", Set.of(id), Set.of(Parcel.Flag.PUBLIC_ACCESS));
        var runtime = WorldRuntime.get(level);
        var saved = ResidenceSavedData.get(level);
        var commands = level.getServer().getCommands().getDispatcher();
        var source = player.createCommandSourceStack();
        runtime.state().parcels.add(parcel);
        try {
            if (commands.execute("ci residence declare", source) != 1) throw new AssertionError("Declaration command failed");
            var home = saved.registry.home(id).orElseThrow();
            if (!ResidenceServices.qualifyingPresence(player, home)) throw new AssertionError("Owned/trusted survival presence rejected");
            player.setGameMode(GameType.CREATIVE);
            if (ResidenceServices.qualifyingPresence(player, home)) throw new AssertionError("Creative presence accrued");
            player.setGameMode(GameType.SURVIVAL);
            player.setPos(pos.getX()+128, pos.getY(), pos.getZ());
            if (ResidenceServices.qualifyingPresence(player, home)) throw new AssertionError("Remote player accrued");
            player.setPos(pos.getX()+.5, pos.getY(), pos.getZ()+.5);
            var nether = level.getServer().getLevel(Level.NETHER);
            if (nether == null || ResidenceSavedData.get(nether) != saved) throw new AssertionError("Residence authority split across dimensions");
            saved.registry.observe(id, 0, true);
            NeoForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(player));
            saved.registry.observe(id, 20, true);
            if (saved.registry.home(id).orElseThrow().qualifyingTicks() != 0) throw new AssertionError("Logout retained observation");
            commands.execute("ci residence withdraw", source);
            if (saved.registry.home(id).isPresent()) throw new AssertionError("Withdraw command retained residence");
            commands.execute("ci residence declare", source);
            runtime.state().parcels.remove(parcel.id());
            runtime.state().parcels.add(new Parcel(parcel.id(), parcel.owner(), parcel.minX(), parcel.minY(), parcel.minZ(),
                    parcel.maxX(), parcel.maxY(), parcel.maxZ(), parcel.name(), Set.of(), parcel.flags()));
            if (ResidenceServices.authorized(level, home)) throw new AssertionError("Trust revocation bypassed");
            boolean refused = false;
            try { commands.execute("ci residence declare", source.withPermission(4)); }
            catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) { refused = true; }
            if (!refused) throw new AssertionError("Operator/public access minted resident eligibility");
            commands.execute("ci residence status", source);
            commands.execute("ci parcel remove", source.withPermission(4));
            if (saved.registry.home(id).isPresent()) throw new AssertionError("Parcel deletion retained residence");
        } finally {
            saved.registry.withdraw(id); saved.setDirty();
            runtime.state().parcels.remove(parcel.id()); runtime.dirty();
        }
        h.succeed();
    }

    @GameTest(template="empty")
    public static void residenceFileRefusalAndReload(GameTestHelper h) throws Exception {
        Path directory = Files.createTempDirectory("civitas-residence-check-");
        Path file = directory.resolve("residences.dat");
        try {
            if (ResidenceSavedData.readStrict(file).size() != 0 || Files.exists(file)) throw new AssertionError("Missing file was changed");
            var registry = new ResidenceRegistry();
            UUID owner = UUID.randomUUID();
            var parcel = new Parcel(UUID.randomUUID(), owner, -64, 0, -64, -1, 100, -1, "persist", Set.of(), Set.of());
            registry.declare(owner, "minecraft:overworld", parcel, -1, 64, -1);
            CompoundTag data = new CompoundTag();
            data.putInt("dataVersion", 1); data.putByteArray("snapshot", registry.encode());
            CompoundTag root = new CompoundTag(); root.put("data", data);
            NbtIo.writeCompressed(root, file);
            if (!Arrays.equals(registry.encode(), ResidenceSavedData.readStrict(file).encode())) throw new AssertionError("Saved residence identity changed");
            data.putInt("dataVersion", 99); NbtIo.writeCompressed(root, file); refusesUnchanged(file);
            data.putInt("dataVersion", 1); data.putByteArray("snapshot", new byte[]{1, 2, 3});
            NbtIo.writeCompressed(root, file); refusesUnchanged(file);
            data.putString("unknownAuthority", "preserve"); NbtIo.writeCompressed(root, file); refusesUnchanged(file);
            Files.write(file, new byte[]{1, 2, 3}); refusesUnchanged(file);
        } finally { Files.deleteIfExists(file); Files.deleteIfExists(directory); }
        h.succeed();
    }
    private static void refusesUnchanged(Path file) throws Exception {
        byte[] before = Files.readAllBytes(file);
        boolean refused = false;
        try { ResidenceSavedData.readStrict(file); } catch (IllegalStateException e) { refused = true; }
        if (!refused || !Arrays.equals(before, Files.readAllBytes(file))) throw new AssertionError("Unsafe residence file handling");
    }
}
