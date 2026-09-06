package com.civitasindustria.common.decoration;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
/** Persistent animation parameters only. Neither side registers a ticker. */
public final class DecorativeEntity extends BlockEntity {
    public boolean enabled=true;public int rpm=15;public long startTime;
    private CompoundTag quarantine;
    public DecorativeEntity(BlockPos p,BlockState s){super(CivitasRegistries.DECORATIVE_ENTITY.get(),p,s);}
    public void change(boolean speed){if(quarantine!=null)return;if(speed)rpm=rpm>=60?5:rpm+5;else enabled=!enabled;startTime=level.getGameTime();setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider provider){super.saveAdditional(tag,provider);if(quarantine!=null){tag.merge(quarantine.copy());return;}tag.putInt("dataVersion",1);tag.putBoolean("enabled",enabled);tag.putInt("rpm",rpm);tag.putLong("startTime",startTime);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider provider){super.loadAdditional(tag,provider);quarantine=null;if(!tag.contains("dataVersion"))return;if(tag.getInt("dataVersion")!=1||tag.getInt("rpm")<0||tag.getInt("rpm")>60){quarantine=tag.copy();enabled=false;return;}enabled=tag.getBoolean("enabled");rpm=tag.getInt("rpm");startTime=tag.getLong("startTime");}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider){return saveWithoutMetadata(provider);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
