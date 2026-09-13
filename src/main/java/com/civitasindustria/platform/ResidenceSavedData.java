package com.civitasindustria.platform;

import com.civitasindustria.domain.ResidenceRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

/** Loaded explicitly before registration so vanilla's fallback cannot overwrite bad data. */
public final class ResidenceSavedData extends SavedData {
    public static final String NAME = "civitas_residences";
    public final ResidenceRegistry registry;
    private ResidenceSavedData(ResidenceRegistry registry) { this.registry = registry; }

    public static ResidenceSavedData get(ServerLevel level) {
        return WorldRuntime.get(level.getServer().overworld()).residences;
    }
    static ResidenceSavedData loadStrict(ServerLevel overworld) {
        Path file = overworld.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(NAME + ".dat");
        ResidenceSavedData result = new ResidenceSavedData(readStrict(file));
        overworld.getDataStorage().set(NAME, result);
        return result;
    }
    /** Exposed for file-preservation acceptance probes; reads but never changes the file. */
    public static ResidenceRegistry readStrict(Path file) {
        if (Files.notExists(file)) return new ResidenceRegistry();
        try {
            if (Files.size(file) > ResidenceRegistry.MAX_BYTES) throw new IOException("Compressed residence byte limit");
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.create(ResidenceRegistry.MAX_BYTES + 4096L));
            if (!root.contains("data", Tag.TAG_COMPOUND)) throw new IOException("Missing residence data compound");
            CompoundTag data = root.getCompound("data");
            if (!data.getAllKeys().equals(Set.of("dataVersion", "snapshot"))
                    || !data.contains("dataVersion", Tag.TAG_INT) || data.getInt("dataVersion") != ResidenceRegistry.VERSION
                    || !data.contains("snapshot", Tag.TAG_BYTE_ARRAY))
                throw new IOException("Unsupported or malformed residence envelope");
            return ResidenceRegistry.decode(data.getByteArray("snapshot"));
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Residence data was not loaded; refusing to overwrite " + file, e);
        }
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("dataVersion", ResidenceRegistry.VERSION);
        tag.putByteArray("snapshot", registry.encode());
        return tag;
    }
}
