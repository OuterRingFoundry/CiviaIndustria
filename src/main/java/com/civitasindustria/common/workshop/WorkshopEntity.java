package com.civitasindustria.common.workshop;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.factory.MachineCommissioning;
import com.civitasindustria.platform.*;
import com.civitasindustria.domain.Parcel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.*;
public final class WorkshopEntity extends BlockEntity implements MenuProvider,RetainedMachine {
    public static final int CAPACITY=16000;
    private int energy,progress,mode,status;
    private String recipeIdentity="";
    private CompoundTag quarantine;
    public final ItemStackHandler inventory=new ItemStackHandler(3){
        @Override public boolean isItemValid(int slot,ItemStack s){return quarantine==null&&(slot==0?WorkshopRecipes.INSTANCE.accepts(s):slot==1&&s.is(CivitasRegistries.CUTTING_INSERT.get())&&s.getDamageValue()<s.getMaxDamage());}
        @Override protected void onContentsChanged(int slot){if(slot<2)progress=0;setChanged();}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return quarantine!=null?ItemStack.EMPTY:super.extractItem(slot,amount,simulate);}
    };
    public final IItemHandler automation=new IItemHandler(){
        public int getSlots(){return 3;}public ItemStack getStackInSlot(int s){return inventory.getStackInSlot(s);}public int getSlotLimit(int s){return inventory.getSlotLimit(s);}
        public boolean isItemValid(int s,ItemStack v){return inventory.isItemValid(s,v);}public ItemStack insertItem(int s,ItemStack v,boolean sim){return inventory.insertItem(s,v,sim);}
        public ItemStack extractItem(int s,int a,boolean sim){return s==2?inventory.extractItem(s,a,sim):ItemStack.EMPTY;}
    };
    public final IEnergyStorage power=new IEnergyStorage(){
        public int receiveEnergy(int n,boolean sim){int accepted=quarantine==null?Math.min(Math.max(0,n),Math.min(1024,CAPACITY-energy)):0;if(!sim&&accepted>0){energy+=accepted;setChanged();}return accepted;}
        public int extractEnergy(int n,boolean sim){return 0;}public int getEnergyStored(){return energy;}public int getMaxEnergyStored(){return CAPACITY;}public boolean canExtract(){return false;}public boolean canReceive(){return quarantine==null;}
    };
    public final ContainerData data=new ContainerData(){
        public int get(int i){var r=WorkshopRecipes.INSTANCE.get(mode);return switch(i){case 0->energy;case 1->progress;case 2->mode;case 3->status;case 4->r==null?1:r.ticks();default->0;};}
        public void set(int i,int v){}public int getCount(){return 5;}
    };
    public WorkshopEntity(BlockPos p,BlockState s){super(CivitasRegistries.WORKSHOP_ENTITY.get(),p,s);}
    public boolean mayUse(Player p){return quarantine==null&&!isRemoved()&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64&&(!(level instanceof ServerLevel s)||ParcelProtection.allows(s,worldPosition,p,Parcel.Flag.INTERACT)&&ParcelProtection.allows(s,worldPosition,p,Parcel.Flag.CONTAINER));}
    public boolean select(Player p,int operation){if(!mayUse(p)||operation<0||operation>2)return false;if(mode!=operation){mode=operation;level.setBlock(worldPosition,getBlockState().setValue(WorkshopBlock.OPERATION,mode),3);progress=0;recipeIdentity="";setChanged();}return true;}
    public boolean retainsContents(){return quarantine!=null||!inventory.getStackInSlot(0).isEmpty()||!inventory.getStackInSlot(1).isEmpty()||!inventory.getStackInSlot(2).isEmpty();}
    public void tick(){
        if(!(level instanceof ServerLevel server))return;
        status=0;boolean active=false;
        if(quarantine!=null)status=7;
        else if(!MachineCommissioning.canOperate(this))status=1;
        else if(server.hasNeighborSignal(worldPosition))status=2;
        else if(WorldRuntime.get(server).jammed(worldPosition,server.getGameTime()))status=3;
        else {
            var r=WorkshopRecipes.INSTANCE.get(mode);var input=inventory.getStackInSlot(0);var tool=inventory.getStackInSlot(1);var out=inventory.getStackInSlot(2);
            if(r==null||!input.is(r.input())||!input.getComponentsPatch().isEmpty()||!tool.is(CivitasRegistries.CUTTING_INSERT.get())||tool.getDamageValue()>=tool.getMaxDamage()||!BuiltInRegistries.ITEM.containsKey(r.output()))status=4;
            else {
                var result=BuiltInRegistries.ITEM.get(r.output());
                if(result==Items.AIR||!out.isEmpty()&&(!out.is(result)||!out.getComponentsPatch().isEmpty()||out.getCount()>=out.getMaxStackSize()))status=5;
                else if(energy<r.energy())status=6;
                else {
                    String identity=r.toString();if(!identity.equals(recipeIdentity)){progress=0;recipeIdentity=identity;}
                    energy-=r.energy();progress++;active=true;
                    if(progress>=r.ticks()){
                        inventory.extractItem(0,1,false);
                        tool.setDamageValue(tool.getDamageValue()+1);if(tool.getDamageValue()>=tool.getMaxDamage())inventory.setStackInSlot(1,ItemStack.EMPTY);
                        if(out.isEmpty())inventory.setStackInSlot(2,new ItemStack(result));else out.grow(1);
                        progress=0;
                    }setChanged();
                }
            }
        }
        if(getBlockState().getValue(WorkshopBlock.ACTIVE)!=active)server.setBlock(worldPosition,getBlockState().setValue(WorkshopBlock.ACTIVE,active),3);
    }
    @Override public Component getDisplayName(){return Component.translatable("block.civitas_industria.precision_workbench");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player player){return new WorkshopMenu(id,inv,this,data);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider p){super.saveAdditional(t,p);if(quarantine!=null){t.merge(quarantine.copy());return;}t.putInt("version",1);t.putInt("energy",energy);t.putInt("progress",progress);t.putInt("mode",mode);t.putString("recipe",recipeIdentity);t.put("inventory",inventory.serializeNBT(p));}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider p){
        super.loadAdditional(t,p);quarantine=null;if(!t.contains("version")){if(t.contains("energy")||t.contains("inventory")||t.contains("progress")||t.contains("mode")||t.contains("recipe")){quarantine=t.copy();energy=0;progress=0;status=7;}return;}
        try{
            if(!t.contains("version",Tag.TAG_INT)||!t.contains("energy",Tag.TAG_INT)||!t.contains("progress",Tag.TAG_INT)||!t.contains("mode",Tag.TAG_INT)||!t.contains("recipe",Tag.TAG_STRING)||!t.contains("inventory",Tag.TAG_COMPOUND)||t.getInt("version")!=1||t.getInt("energy")<0||t.getInt("energy")>CAPACITY||t.getInt("progress")<0||t.getInt("progress")>32000||t.getInt("mode")<0||t.getInt("mode")>2||t.getString("recipe").length()>1024)throw new IllegalArgumentException();
            var saved=t.getCompound("inventory");if(saved.getInt("Size")!=3)throw new IllegalArgumentException();var seen=new java.util.HashSet<Integer>();
            for(var e:saved.getList("Items",Tag.TAG_COMPOUND)){var s=(CompoundTag)e;int slot=s.getInt("Slot");var item=ItemStack.parse(p,s).orElseThrow();if(slot<0||slot>2||!seen.add(slot)||item.getCount()>item.getMaxStackSize())throw new IllegalArgumentException();}
            inventory.deserializeNBT(p,saved);energy=t.getInt("energy");progress=t.getInt("progress");mode=t.getInt("mode");recipeIdentity=t.getString("recipe");
        }catch(RuntimeException e){quarantine=t.copy();energy=0;progress=0;status=7;}
    }
}
