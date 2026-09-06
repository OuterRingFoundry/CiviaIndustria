package com.civitasindustria.common.warehouse;

import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.DataMigrationManager;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** One homogeneous fluid, long internal millibuckets, bounded int capability operations. */
public final class BulkTankBlockEntity extends BlockEntity implements IFluidHandler {
    private Fluid fluid=Fluids.EMPTY;
    private long amount;
    private CompoundTag quarantine;
    public BulkTankBlockEntity(BlockPos pos,BlockState state){super(CivitasRegistries.TANK_ENTITY.get(),pos,state);}
    public long amount(){return amount;}
    public boolean hasContents(){return amount>0||quarantine!=null;}
    private boolean available(){return !isRemoved()&&quarantine==null;}
    @Override public int getTanks(){return 1;}
    @Override public FluidStack getFluidInTank(int tank){return tank!=0||!available()||amount==0?FluidStack.EMPTY:new FluidStack(fluid,(int)Math.min(Integer.MAX_VALUE,amount));}
    @Override public int getTankCapacity(int tank){return tank==0?(int)Math.min(Integer.MAX_VALUE,ServerConfig.TANK_CAPACITY.get()):0;}
    @Override public boolean isFluidValid(int tank,FluidStack stack){return tank==0&&!stack.isEmpty()&&stack.getComponentsPatch().isEmpty();}
    @Override public int fill(FluidStack stack,FluidAction action){
        if(!available()||!isFluidValid(0,stack)||(amount>0&&fluid!=stack.getFluid()))return 0;
        int accepted=(int)Math.min(stack.getAmount(),Math.max(0,ServerConfig.TANK_CAPACITY.get()-amount));
        if(action.execute()&&accepted>0){fluid=stack.getFluid();amount+=accepted;setChanged();}
        return accepted;
    }
    @Override public FluidStack drain(FluidStack stack,FluidAction action){
        return stack.isEmpty()||!stack.getComponentsPatch().isEmpty()||stack.getFluid()!=fluid?FluidStack.EMPTY:drain(stack.getAmount(),action);
    }
    @Override public FluidStack drain(int requested,FluidAction action){
        if(!available()||requested<=0||amount==0)return FluidStack.EMPTY;
        int taken=(int)Math.min(requested,amount);FluidStack result=new FluidStack(fluid,taken);
        if(action.execute()){amount-=taken;if(amount==0)fluid=Fluids.EMPTY;setChanged();}return result;
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);if(quarantine!=null){tag.merge(quarantine.copy());return;}
        tag.putInt("dataVersion",1);tag.putString("fluid",amount==0?"":BuiltInRegistries.FLUID.getKey(fluid).toString());tag.putLong("amount",amount);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);quarantine=null;fluid=Fluids.EMPTY;amount=0;
        if(!tag.contains("dataVersion"))return;
        try{
            if(tag.contains("cargo")){
                if(tag.getInt("dataVersion")!=2||DataMigrationManager.decodeInventory(tag.getByteArray("cargo")).total()!=0)throw new IllegalArgumentException("Legacy item tank contains cargo");
                return; // explicitly migrate empty draft tanks only
            }
            if(tag.getInt("dataVersion")!=1||!tag.contains("amount",Tag.TAG_LONG)||!tag.contains("fluid",Tag.TAG_STRING))throw new IllegalArgumentException("Tank envelope");
            long count=tag.getLong("amount");String key=tag.getString("fluid");
            if(count<0||count>Long.MAX_VALUE/2||count==0&&!key.isEmpty())throw new IllegalArgumentException("Tank count");
            if(count>0){var id=ResourceLocation.parse(key);if(!BuiltInRegistries.FLUID.containsKey(id)||(fluid=BuiltInRegistries.FLUID.get(id))==Fluids.EMPTY)throw new IllegalArgumentException("Missing fluid");}
            amount=count;
        }catch(IllegalArgumentException e){quarantine=tag.copy();}
    }
}
