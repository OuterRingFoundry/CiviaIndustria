package com.civitasindustria.common.warehouse;

import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import java.util.*;

/** Stable slots adapt compact long-count authority to the int-sized item capability. */
public final class CargoBlockEntity extends BlockEntity {
    private BulkInventory inventory;
    private final String[] slots=new String[16];
    private boolean structureDirty=true,formed;
    private CompoundTag quarantine;
    private ItemStack transit=ItemStack.EMPTY;
    private long lastTransfer;
    private int inputCursor;
    public CargoBlockEntity(BlockPos pos,BlockState state){
        super(CivitasRegistries.CARGO_ENTITY.get(),pos,state);
        inventory=new BulkInventory(16,configuredCapacity());
    }
    private String kind(){return BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();}
    public boolean isWarehouse(){return kind().equals("warehouse_controller");}
    public boolean isPort(){return kind().equals("warehouse_port");}
    public boolean isFreight(){return Set.of("cargo_loader","cargo_unloader","freight_terminal").contains(kind());}
    private long configuredCapacity(){return isWarehouse()?ServerConfig.WAREHOUSE_CAPACITY.get():ServerConfig.CRATE_CAPACITY.get();}
    @Override public void setChanged(){super.setChanged();if(level instanceof net.minecraft.server.level.ServerLevel server&&!isPort())com.civitasindustria.platform.WorldRuntime.get(server).cargoSignal(worldPosition,total());}
    public String[] mountedKeys(){if(isWarehouse()||isPort()||isFreight()||quarantine!=null)throw new IllegalStateException("Fixed cargo authority");String[] result=new String[16];for(int i=0;i<16;i++)result[i]=slots[i]==null?"":slots[i];return result;}
    public long[] mountedCounts(){long[] result=new long[16];for(int i=0;i<16;i++)result[i]=slots[i]==null?0:inventory.count(slots[i]);return result;}
    public void restoreMounted(String[] keys,long[] counts){
        if(keys.length!=16||counts.length!=16)throw new IllegalArgumentException("Mounted slots");long total=0;for(long count:counts){if(count<0)throw new IllegalArgumentException("Mounted counts");total=Math.addExact(total,count);}
        BulkInventory restored=new BulkInventory(16,Math.max(configuredCapacity(),Math.max(1,total)));String[] mapped=new String[16];
        for(int i=0;i<16;i++)if(counts[i]>0){if(restored.count(keys[i])>0||restored.insert(keys[i],counts[i],false)!=counts[i])throw new IllegalArgumentException("Mounted key");mapped[i]=keys[i];}
        inventory=restored;System.arraycopy(mapped,0,slots,0,16);transit=ItemStack.EMPTY;quarantine=null;setChanged();
    }
    public long total(){return inventory.total();}
    public boolean isQuarantined(){return quarantine!=null;}
    public void invalidateStructure(){structureDirty=true;if(level!=null)level.invalidateCapabilities(worldPosition);}
    public boolean available(){
        if(isRemoved()||quarantine!=null)return false;
        if(!isWarehouse())return true;
        if(structureDirty&&level!=null){
            long started=System.nanoTime();formed=true;
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0){
                BlockPos p=worldPosition.offset(x,0,z);
                if(!level.hasChunkAt(p)){formed=false;continue;}
                var block=level.getBlockState(p).getBlock();
                if(block!=CivitasRegistries.CONTENT.get("warehouse_casing").get()&&block!=CivitasRegistries.CONTENT.get("warehouse_port").get())formed=false;
            }
            structureDirty=false;
            if(level instanceof net.minecraft.server.level.ServerLevel server)com.civitasindustria.platform.WorldRuntime.get(server).record("warehouse",started,8);
        }
        return formed;
    }
    public CargoBlockEntity authority(){
        if(!isPort())return this;
        if(level==null||isRemoved())return null;
        CargoBlockEntity owner=null;
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
            BlockPos p=worldPosition.offset(x,0,z);
            if(!level.hasChunkAt(p))return null; // ownership cannot be inferred through unloaded chunks
            if(level.getBlockEntity(p) instanceof CargoBlockEntity c&&c.isWarehouse()){
                if(owner!=null)return null;
                owner=c;
            }
        }
        return owner;
    }
    public boolean hasContents(){return inventory.total()>0||quarantine!=null||!transit.isEmpty();}
    public static boolean allowed(ItemStack stack){
        return !stack.isEmpty()&&stack.getMaxStackSize()>1&&stack.getComponentsPatch().isEmpty()
            &&!stack.has(DataComponents.CONTAINER)&&!stack.has(DataComponents.BLOCK_ENTITY_DATA)
            &&!(stack.getItem() instanceof BlockItem b&&b.getBlock() instanceof StorageBlock);
    }
    private Item item(String key){
        if(key==null)return Items.AIR;
        var id=ResourceLocation.tryParse(key);
        return id!=null&&BuiltInRegistries.ITEM.containsKey(id)?BuiltInRegistries.ITEM.get(id):Items.AIR;
    }
    private boolean validSlot(int slot){return slot>=0&&slot<slots.length;}
    private long insert(int slot,ItemStack stack,boolean simulate){
        if(!validSlot(slot)||!allowed(stack))return 0;
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if(slots[slot]!=null&&!slots[slot].equals(key))return 0;
        for(int i=0;i<slots.length;i++)if(i!=slot&&key.equals(slots[i]))return 0;
        long allowed=Math.min(stack.getCount(),Math.max(0,configuredCapacity()-inventory.total()));
        long accepted=inventory.insert(key,allowed,simulate);
        if(!simulate&&accepted>0){slots[slot]=key;setChanged();}
        return accepted;
    }
    public final IItemHandler handler=new IItemHandler(){
        @Override public int getSlots(){return 16;}
        @Override public ItemStack getStackInSlot(int slot){
            var a=authority();if(a==null||!a.available()||!a.validSlot(slot))return ItemStack.EMPTY;
            String key=a.slots[slot];Item item=a.item(key);
            return item==Items.AIR?ItemStack.EMPTY:new ItemStack(item,(int)Math.min(item.getDefaultMaxStackSize(),a.inventory.count(key)));
        }
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){
            var a=authority();if(a==null||!a.available())return stack;
            int accepted=(int)a.insert(slot,stack,simulate);
            return accepted==0?stack:stack.copyWithCount(stack.getCount()-accepted);
        }
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){
            var a=authority();if(a==null||!a.available()||!a.validSlot(slot)||amount<=0)return ItemStack.EMPTY;
            String key=a.slots[slot];Item item=a.item(key);
            if(item==Items.AIR)return ItemStack.EMPTY; // never decrement unknown item counts
            int taken=(int)a.inventory.extract(key,Math.min(amount,item.getDefaultMaxStackSize()),simulate);
            if(!simulate&&taken>0){if(a.inventory.count(key)==0)a.slots[slot]=null;a.setChanged();}
            return taken==0?ItemStack.EMPTY:new ItemStack(item,taken);
        }
        @Override public int getSlotLimit(int slot){return 64;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return validSlot(slot)&&allowed(stack);}
    };
    public void interact(Player player,ItemStack held,boolean withdraw){
        CargoBlockEntity a=authority();
        if(a==null||!a.available()){
            player.displayClientMessage(Component.literal("Storage unavailable: check warehouse ring, unique controller, or quarantined save."),false);return;
        }
        if(withdraw){
            for(int i=0;i<16;i++){
                ItemStack offered=a.handler.extractItem(i,64,true);
                if(offered.isEmpty()||(!held.isEmpty()&&!ItemStack.isSameItemSameComponents(offered,held)))continue;
                int before=offered.getCount();player.getInventory().add(offered);
                a.handler.extractItem(i,before-offered.getCount(),false);break;
            }
        }else if(allowed(held)){
            for(int i=0;i<16;i++){
                int count=held.getCount();ItemStack remainder=a.handler.insertItem(i,held,false);
                int moved=count-remainder.getCount();if(moved>0){held.shrink(moved);break;}
            }
        }
        player.displayClientMessage(Component.literal("Cargo "+a.total()+"/"+a.configuredCapacity()+" | "+a.inventory.contents()),false);
    }
    /** One bounded batch per visit, with overflow retained in a persisted transit slot. */
    public void transfer(long now){
        if(!isFreight()||level==null||!available()||now-lastTransfer<10)return;
        lastTransfer=now;
        if(!transit.isEmpty()){
            for(int slot=0;slot<16&&!transit.isEmpty();slot++)transit=handler.insertItem(slot,transit,false);
            setChanged();if(!transit.isEmpty())return;
        }
        Direction facing=getBlockState().getValue(FreightBlock.FACING);
        BlockPos output=worldPosition.relative(facing),input=worldPosition.relative(facing.getOpposite());
        if(level.hasChunkAt(output)&&!sameAuthority(output)){
            var target=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,output,facing.getOpposite());
            if(target!=null){
                int budget=ServerConfig.TRANSFER_BATCH.get();
                for(int slot=0;slot<16&&budget>0;slot++){
                    ItemStack offered=handler.extractItem(slot,Math.min(64,budget),true);int count=offered.getCount();
                    for(int targetSlot=0;targetSlot<Math.min(256,target.getSlots())&&!offered.isEmpty();targetSlot++)offered=target.insertItem(targetSlot,offered,false);
                    int moved=count-offered.getCount();if(moved>0){handler.extractItem(slot,moved,false);budget-=moved;}
                }
            }
        }
        if(level.hasChunkAt(input)&&!sameAuthority(input)){
            var source=level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,input,facing);
            if(source==null)return;
            int slots=Math.min(256,source.getSlots());if(slots==0)return;
            for(int visited=0;visited<slots;visited++){
                int slot=Math.floorMod(inputCursor++,slots);ItemStack offered=source.extractItem(slot,Math.min(64,ServerConfig.TRANSFER_BATCH.get()),true);
                if(!allowed(offered))continue;
                int accepted=0;for(int own=0;own<16;own++){accepted=offered.getCount()-handler.insertItem(own,offered,true).getCount();if(accepted>0)break;}
                if(accepted==0)continue;
                transit=source.extractItem(slot,accepted,false);setChanged();
                for(int own=0;own<16&&!transit.isEmpty();own++)transit=handler.insertItem(own,transit,false);
                setChanged();break;
            }
        }
    }
    private boolean sameAuthority(BlockPos pos){return level.getBlockEntity(pos) instanceof CargoBlockEntity c&&c.authority()==authority();}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);
        if(quarantine!=null){tag.merge(quarantine.copy());return;}
        tag.putInt("dataVersion",DataMigrationManager.INVENTORY_VERSION);
        if(!transit.isEmpty())tag.put("cargoTransit",transit.save(registries));
        tag.putInt("cargoInputCursor",inputCursor);
        tag.putByteArray("cargo",DataMigrationManager.encodeInventory(inventory));
        ListTag keys=new ListTag();for(String key:slots)keys.add(StringTag.valueOf(key==null?"":key));tag.put("cargoSlots",keys);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);quarantine=null;transit=ItemStack.EMPTY;inventory=new BulkInventory(16,configuredCapacity());Arrays.fill(slots,null);
        if(!tag.contains("cargo")&&!tag.contains("dataVersion"))return;
        try{
            if(!tag.contains("dataVersion",Tag.TAG_INT)||tag.getInt("dataVersion")!=DataMigrationManager.INVENTORY_VERSION||!tag.contains("cargo",Tag.TAG_BYTE_ARRAY))throw new IllegalArgumentException("Cargo envelope");
            transit=tag.contains("cargoTransit",Tag.TAG_COMPOUND)?ItemStack.parseOptional(registries,tag.getCompound("cargoTransit")):ItemStack.EMPTY;
            if(tag.contains("cargoTransit")&&(!allowed(transit)))throw new IllegalArgumentException("Invalid transit cargo");
            inputCursor=tag.getInt("cargoInputCursor");
            BulkInventory loaded=DataMigrationManager.decodeInventory(tag.getByteArray("cargo"));
            if(loaded.maxKeys()!=16)throw new IllegalArgumentException("Unsupported slot count");
            for(String key:loaded.contents().keySet())if(item(key)==Items.AIR)throw new IllegalArgumentException("Unregistered cargo item "+key);
            if(tag.contains("cargoSlots")){
                ListTag keys=tag.getList("cargoSlots",Tag.TAG_STRING);if(keys.size()!=16)throw new IllegalArgumentException("Invalid slots");
                Set<String> unique=new HashSet<>();
                for(int i=0;i<16;i++){String key=keys.getString(i);if(!key.isEmpty()){
                    if(loaded.count(key)==0||!unique.add(key))throw new IllegalArgumentException("Invalid slot key");slots[i]=key;
                }}
                if(unique.size()!=loaded.contents().size())throw new IllegalArgumentException("Unmapped cargo");
            }else{int i=0;for(String key:loaded.contents().keySet())slots[i++]=key;}
            // Capacity reductions grandfather existing counts; insertion still uses the current configured cap.
            inventory=new BulkInventory(16,Math.max(configuredCapacity(),Math.max(1,loaded.total())));
            for(var entry:loaded.contents().entrySet())inventory.insert(entry.getKey(),entry.getValue(),false);
        }catch(IllegalArgumentException e){
            quarantine=tag.copy();
            org.slf4j.LoggerFactory.getLogger(CargoBlockEntity.class).error("Cargo quarantined at {}; original NBT retained",worldPosition,e);
        }
        structureDirty=true;
    }
}
