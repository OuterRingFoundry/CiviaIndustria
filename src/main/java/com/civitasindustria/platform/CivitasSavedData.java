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
                if(!root.contains("data",Tag.TAG_COMPOUND))throw new IOException("Missing data compound");
                CompoundTag data=root.getCompound("data");
                if(!data.contains("dataVersion",Tag.TAG_INT))throw new IOException("Missing integer schema");
                int version=data.getInt("dataVersion");
                if(version<1||version>DataMigrationManager.VERSION)throw new IOException("Unsupported schema "+version);
                if(version==1&&!data.contains("snapshot")) {
                    if(data.getAllKeys().size()!=1)throw new IOException("Unexpected foundation fields");
                    state=new WorldState();
                } else {
                    if(!data.contains("snapshot",Tag.TAG_BYTE_ARRAY))throw new IOException("Missing snapshot bytes");
                    byte[] snapshot=data.getByteArray("snapshot");
                    if(snapshot.length<8||java.nio.ByteBuffer.wrap(snapshot).getInt(4)!=version)
                        throw new IOException("Envelope/snapshot schema mismatch");
                    state=DataMigrationManager.decode(snapshot);
                }
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
