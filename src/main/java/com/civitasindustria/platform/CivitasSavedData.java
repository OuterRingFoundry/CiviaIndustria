package com.civitasindustria.platform;
import com.civitasindustria.domain.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.*;
import java.io.IOException;

public final class CivitasSavedData extends SavedData {
    public static final String NAME="civitas_industria";
    public final WorldState state;
    private CivitasSavedData(WorldState state){this.state=state;}
    /** Explicit bounded load: vanilla SavedData fallback must never replace a damaged file. */
    public static CivitasSavedData loadStrict(ServerLevel level){
        Path file=DimensionType.getStorageFolder(level.dimension(),level.getServer().getWorldPath(LevelResource.ROOT)).resolve("data").resolve(NAME+".dat");
        WorldState state=new WorldState();
        if(Files.exists(file)){
            try {
                CompoundTag root=NbtIo.readCompressed(file,NbtAccounter.create(64L*1024*1024));
                CompoundTag data=root.getCompound("data");
                int version=data.getInt("dataVersion");
                if(version<1||version>DataMigrationManager.VERSION)throw new IOException("Unsupported schema "+version);
                if(version==1&&!data.contains("snapshot")) state=new WorldState();
                else state=DataMigrationManager.decode(data.getByteArray("snapshot"));
            }catch(IOException|RuntimeException e){throw new IllegalStateException("Civitas data was not loaded; refusing to overwrite "+file,e);}
        }
        CivitasSavedData result=new CivitasSavedData(state);
        level.getDataStorage().set(NAME,result);
        return result;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
        tag.putInt("dataVersion",DataMigrationManager.VERSION);tag.putByteArray("snapshot",DataMigrationManager.encode(state));return tag;
    }
}
