package com.civitasindustria.compat.create;
import com.civitasindustria.common.warehouse.CargoBlockEntity;
import com.civitasindustria.common.config.ServerConfig;
import com.simibubi.create.api.contraption.storage.item.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
/** Long-count moving authority; exchanges use bounded int insert/extract windows. */
public final class MountedCargo extends MountedItemStorage {
    public record Entry(String key,long count){
        static final Codec<Entry> CODEC=RecordCodecBuilder.create(i->i.group(Codec.STRING.fieldOf("key").forGetter(Entry::key),Codec.LONG.validate(n->n>=0&&n<=Long.MAX_VALUE/32?DataResult.success(n):DataResult.error(()->"Mounted count bounds")).fieldOf("count").forGetter(Entry::count)).apply(i,Entry::new));
        public Entry {if(key.length()>256||count<0||count>Long.MAX_VALUE/32||count>0&&ResourceLocation.tryParse(key)==null)throw new IllegalArgumentException("Mounted cargo entry");}
    }
    public static final MapCodec<MountedCargo> CODEC=Entry.CODEC.listOf(16,16).fieldOf("slots").xmap(MountedCargo::new,MountedCargo::entries);
    private final String[] keys=new String[16];private final long[] counts=new long[16];
    public MountedCargo(List<Entry> entries){super(CreateCargo.TYPE.get());if(entries.size()!=16)throw new IllegalArgumentException("Mounted cargo slots");Set<String> unique=new HashSet<>();for(int i=0;i<16;i++){var e=entries.get(i);keys[i]=e.key();counts[i]=e.count();if(e.count()>0&&!unique.add(e.key()))throw new IllegalArgumentException("Duplicate cargo keys");}}
    public List<Entry> entries(){List<Entry> result=new ArrayList<>(16);for(int i=0;i<16;i++)result.add(new Entry(keys[i],counts[i]));return result;}
    public long total(){long total=0;for(long n:counts)total=Math.addExact(total,n);return total;}
    private Item item(int slot){var id=ResourceLocation.tryParse(keys[slot]);return id==null||!BuiltInRegistries.ITEM.containsKey(id)?Items.AIR:BuiltInRegistries.ITEM.get(id);}
    @Override public int getSlots(){return 16;}
    @Override public int getSlotLimit(int slot){return 64;}
    @Override public boolean isItemValid(int slot,ItemStack stack){return slot>=0&&slot<16&&CargoBlockEntity.allowed(stack);}
    @Override public ItemStack getStackInSlot(int slot){if(slot<0||slot>=16)return ItemStack.EMPTY;var item=item(slot);return item==Items.AIR?ItemStack.EMPTY:new ItemStack(item,(int)Math.min(item.getDefaultMaxStackSize(),counts[slot]));}
    @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){
        if(!isItemValid(slot,stack))return stack;String key=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if(counts[slot]>0&&!keys[slot].equals(key))return stack;
        for(int i=0;i<16;i++)if(i!=slot&&counts[i]>0&&keys[i].equals(key))return stack;
        int accepted=(int)Math.min(stack.getCount(),Math.max(0,ServerConfig.CRATE_CAPACITY.get()-total()));
        if(!simulate&&accepted>0){keys[slot]=key;counts[slot]+=accepted;}
        return stack.copyWithCount(stack.getCount()-accepted);
    }
    @Override public ItemStack extractItem(int slot,int amount,boolean simulate){
        if(slot<0||slot>=16||amount<=0)return ItemStack.EMPTY;Item item=item(slot);if(item==Items.AIR)return ItemStack.EMPTY;
        int taken=(int)Math.min(Math.min(amount,item.getDefaultMaxStackSize()),counts[slot]);if(!simulate){counts[slot]-=taken;if(counts[slot]==0)keys[slot]="";}
        return new ItemStack(item,taken);
    }
    @Override public void setStackInSlot(int slot,ItemStack stack){throw new UnsupportedOperationException("Long cargo requires insert/extract; absolute int replacement is unsafe");}
    @Override public boolean handleInteraction(net.minecraft.server.level.ServerPlayer player,com.simibubi.create.content.contraptions.Contraption contraption,net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo info){return false;}
    @Override public void unmount(Level level,BlockState state,BlockPos pos,BlockEntity entity){
        if(!(entity instanceof CargoBlockEntity cargo))throw new IllegalStateException("Missing destination cargo authority at "+pos);
        cargo.restoreMounted(keys,counts);
    }
    public static final class Type extends MountedItemStorageType<MountedCargo>{
        public Type(){super(MountedCargo.CODEC);}
        @Override public MountedCargo mount(Level level,BlockState state,BlockPos pos,BlockEntity entity){
            if(!(entity instanceof CargoBlockEntity cargo)||cargo.isQuarantined())return null;
            var keys=cargo.mountedKeys();var counts=cargo.mountedCounts();var entries=new ArrayList<Entry>(16);
            for(int i=0;i<16;i++)entries.add(new Entry(keys[i],counts[i]));return new MountedCargo(entries);
        }
    }
}
