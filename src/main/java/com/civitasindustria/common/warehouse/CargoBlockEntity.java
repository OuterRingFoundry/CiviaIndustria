package com.civitasindustria.common.warehouse;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import java.util.*;

public final class CargoBlockEntity extends BlockEntity {
    private BulkInventory inventory;
    private boolean structureDirty=true,formed;
    private boolean quarantined;
    private byte[] damagedSnapshot;
    public CargoBlockEntity(BlockPos pos,BlockState state){super(CivitasRegistries.CARGO_ENTITY.get(),pos,state);
        inventory=new BulkInventory(16,isWarehouse()?ServerConfig.WAREHOUSE_CAPACITY.get():ServerConfig.CRATE_CAPACITY.get());}
    private String kind(){return BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();}
    public boolean isWarehouse(){return kind().equals("warehouse_controller");}
    public boolean isPort(){return kind().equals("warehouse_port");}
    public boolean isFreight(){return kind().equals("cargo_loader")||kind().equals("cargo_unloader")||kind().equals("freight_terminal");}
    public BulkInventory inventory(){return inventory;}
    public void invalidateStructure(){structureDirty=true;}
    public boolean available(){
        if(quarantined)return false;
        if(!isWarehouse())return true;
        if(structureDirty&&level!=null){
            formed=true;
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)if(x!=0||z!=0){
                BlockPos p=worldPosition.offset(x,0,z);
                if(!level.hasChunkAt(p)){formed=false;continue;}
                String id=BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).toString();
                if(!id.equals("civitas_industria:warehouse_casing")&&!id.equals("civitas_industria:warehouse_port"))formed=false;
            }
            structureDirty=false;
        }return formed;
    }
    private CargoBlockEntity authority(){
        if(!isPort())return this;
        if(level==null)return null;
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
            BlockPos p=worldPosition.offset(x,0,z);
            if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof CargoBlockEntity c&&c.isWarehouse())return c;
        }return null;
    }
    public boolean hasContents(){return inventory.total()>0||quarantined;}
    public static boolean allowed(ItemStack stack){
        return !stack.isEmpty()&&stack.getMaxStackSize()>1&&stack.getComponentsPatch().isEmpty()
            &&!stack.has(DataComponents.CONTAINER)&&!stack.has(DataComponents.BLOCK_ENTITY_DATA)
            &&!(stack.getItem() instanceof BlockItem b&&b.getBlock() instanceof StorageBlock);
    }
    private String key(int slot){
        if(slot<0||slot>=16)return null;
        int i=0;for(String key:inventory.contents().keySet())if(i++==slot)return key;return null;
    }
    public final IItemHandler handler=new IItemHandler(){
        @Override public int getSlots(){return 16;}
        @Override public ItemStack getStackInSlot(int slot){
            var a=authority();if(a==null||!a.available())return ItemStack.EMPTY;
            String key=a.key(slot);return key==null?ItemStack.EMPTY:new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(key)),(int)Math.min(64,a.inventory.count(key)));
        }
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){
            var a=authority();if(a==null||!a.available()||slot<0||slot>=16||!allowed(stack))return stack;
            String key=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),existing=a.key(slot);
            if(existing!=null&&!existing.equals(key))return stack;
            int accepted=(int)a.inventory.insert(key,stack.getCount(),simulate);
            if(accepted==0)return stack;
            if(!simulate)a.setChanged();
            return stack.copyWithCount(stack.getCount()-accepted);
        }
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){
            var a=authority();if(a==null||!a.available()||amount<=0)return ItemStack.EMPTY;
            String key=a.key(slot);if(key==null)return ItemStack.EMPTY;
            Item item=BuiltInRegistries.ITEM.get(ResourceLocation.parse(key));
            int taken=(int)a.inventory.extract(key,Math.min(amount,item.getDefaultMaxStackSize()),simulate);
            if(!simulate&&taken>0)a.setChanged();
            return taken==0?ItemStack.EMPTY:new ItemStack(item,taken);
        }
        @Override public int getSlotLimit(int slot){return 64;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return slot>=0&&slot<16&&allowed(stack);}
    };
    public void interact(Player player,ItemStack held,boolean withdraw){
        CargoBlockEntity a=authority();
        if(a==null||!a.available()){player.displayClientMessage(Component.literal("Storage unavailable. Warehouse requires an eight-block casing/port ring. Quarantined data must be restored."),false);return;}
        if(withdraw){
            String key=held.isEmpty()?a.key(0):BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
            if(key!=null){
                ItemStack stack=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(key)),(int)Math.min(64,a.inventory.count(key)));
                int original=stack.getCount();
                if(original>0){player.getInventory().add(stack);int moved=original-stack.getCount();a.inventory.extract(key,moved,false);a.setChanged();}
            }
        }else if(!held.isEmpty()&&allowed(held)){
            long inserted=a.inventory.insert(BuiltInRegistries.ITEM.getKey(held.getItem()).toString(),held.getCount(),false);
            held.shrink((int)inserted);a.setChanged();
        }
        player.displayClientMessage(Component.literal("Cargo "+a.inventory.total()+"/"+a.inventory.capacity()+" | "+a.inventory.contents()),false);
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);
        tag.putInt("dataVersion",DataMigrationManager.VERSION);
        tag.putByteArray("cargo",quarantined?damagedSnapshot:DataMigrationManager.encodeInventory(inventory));
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);
        if(tag.contains("cargo"))try{
            inventory=DataMigrationManager.decodeInventory(tag.getByteArray("cargo"));
        }catch(IllegalArgumentException e){
            quarantined=true;damagedSnapshot=tag.getByteArray("cargo");
            org.slf4j.LoggerFactory.getLogger(CargoBlockEntity.class).error("Cargo quarantined at {}; original bytes retained",worldPosition,e);
        }
        structureDirty=true;
    }
}
