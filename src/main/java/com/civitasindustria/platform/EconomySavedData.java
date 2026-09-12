package com.civitasindustria.platform;

import com.civitasindustria.domain.CrownLedger;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

/** Loaded explicitly before registration so vanilla's fallback cannot overwrite bad data. */
public final class EconomySavedData extends SavedData {
    public static final String NAME = "civitas_economy";
    public final CrownLedger registry;
    private EconomySavedData(CrownLedger registry) { this.registry = registry; }

    public static EconomySavedData get(ServerLevel level) {
        return WorldRuntime.get(level.getServer().overworld()).economy;
    }
    static EconomySavedData loadStrict(ServerLevel overworld) {
        Path file = overworld.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(NAME + ".dat");
        EconomySavedData result = new EconomySavedData(readStrict(file));
        overworld.getDataStorage().set(NAME, result);
        return result;
    }
    /** Exposed for file-preservation acceptance probes; reads but never changes the file. */
    public static CrownLedger readStrict(Path file) {
        if (Files.notExists(file)) return new CrownLedger();
        try {
            if (Files.size(file) > CrownLedger.MAX_BYTES) throw new IOException("Compressed economy byte limit");
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.create(CrownLedger.MAX_BYTES + 4096L));
            if (!root.contains("data", Tag.TAG_COMPOUND)) throw new IOException("Missing economy data compound");
            CompoundTag data = root.getCompound("data");
            if (!data.getAllKeys().equals(Set.of("dataVersion", "snapshot"))
                    || !data.contains("dataVersion", Tag.TAG_INT) || data.getInt("dataVersion") != CrownLedger.VERSION
                    || !data.contains("snapshot", Tag.TAG_BYTE_ARRAY))
                throw new IOException("Unsupported or malformed economy envelope");
            return CrownLedger.decode(data.getByteArray("snapshot"));
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Economy data was not loaded; refusing to overwrite " + file, e);
        }
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("dataVersion", CrownLedger.VERSION);
        tag.putByteArray("snapshot", registry.encode());
        return tag;
    }
}
