package com.civitasindustria.common.factory;
import com.civitasindustria.api.industrial.FactoryController;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.Commissioning;
import com.civitasindustria.platform.WorldRuntime;
import com.civitasindustria.common.config.ServerConfig;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
public final class FactoryBlockEntity extends BlockEntity implements FactoryController {
    private final Commissioning commissioning=new Commissioning();
    private int progress;
    private String recipe="";
    private CompoundTag quarantine;
    public final ItemStackHandler inventory=new ItemStackHandler(3){
        @Override public boolean isItemValid(int slot,ItemStack stack){return quarantine==null&&stack.getComponentsPatch().isEmpty()&&(slot==0?FactoryRecipes.INSTANCE.find(BuiltInRegistries.ITEM.getKey(stack.getItem()))!=null:slot==1&&(stack.is(Items.COAL)||stack.is(Items.CHARCOAL)));}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return quarantine!=null?ItemStack.EMPTY:super.extractItem(slot,amount,simulate);}
        @Override protected void onContentsChanged(int slot){setChanged();}
    };
    public FactoryBlockEntity(BlockPos pos,BlockState state){super(CivitasRegistries.FACTORY_ENTITY.get(),pos,state);}
    @Override public Commissioning.Stage commissioningStage(){return commissioning.stage;}
    @Override public int parallelBatchSize(){return 16;}
    @Override public int operationProgress(){return progress;}
    public boolean hasContents(){return quarantine!=null||!inventory.getStackInSlot(0).isEmpty()||!inventory.getStackInSlot(1).isEmpty()||!inventory.getStackInSlot(2).isEmpty();}
    private CommissioningRequirements.Rule requirements(){return CommissioningRequirements.INSTANCE.get(BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()));}
    private boolean foundation(){
        var rule=requirements();if(level==null||rule==null)return false;
        var tag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,rule.foundationTag());
        for(int x=-rule.radius();x<=rule.radius();x++)for(int z=-rule.radius();z<=rule.radius();z++){
            BlockPos p=worldPosition.offset(x,-1,z);if(!level.hasChunkAt(p)||!level.getBlockState(p).is(tag))return false;
        }
        return true;
    }
    public boolean beginCommissioning(){return quarantine==null&&level instanceof ServerLevel server&&commissioning.begin(foundation(),WorldRuntime.get(server).load(worldPosition)<ServerConfig.LOAD_LIMIT.get());}
    public void step(){
        if(!(level instanceof ServerLevel server)||quarantine!=null)return;
        var before=commissioning.stage;
        commissioning.update(foundation(),WorldRuntime.get(server).load(worldPosition)<ServerConfig.LOAD_LIMIT.get(),20,ServerConfig.COMMISSION_TICKS.get());
        if(before!=commissioning.stage||commissioning.stage==Commissioning.Stage.COMMISSIONING)setChanged();
        boolean active=false;
        if(commissioning.stage==Commissioning.Stage.READY&&!WorldRuntime.get(server).jammed(worldPosition,server.getGameTime())){
            ItemStack input=inventory.getStackInSlot(0),fuel=inventory.getStackInSlot(1),output=inventory.getStackInSlot(2);
            var r=FactoryRecipes.INSTANCE.find(BuiltInRegistries.ITEM.getKey(input.getItem()));
            if(input.getComponentsPatch().isEmpty()&&fuel.getComponentsPatch().isEmpty()&&r!=null&&BuiltInRegistries.ITEM.containsKey(r.output())&&input.getCount()>=r.count()&&fuel.getCount()>=r.fuel()&&(fuel.is(Items.COAL)||fuel.is(Items.CHARCOAL))){
                Item target=BuiltInRegistries.ITEM.get(r.output());
                if(target!=Items.AIR&&(output.isEmpty()||output.is(target)&&output.getComponentsPatch().isEmpty())&&output.getCount()+r.count()<=target.getDefaultMaxStackSize()){
                    if(!recipe.equals(r.id().toString())){recipe=r.id().toString();progress=0;}
                    active=true;progress+=20;
                    if(progress>=r.duration()){
                        input.shrink(r.count());fuel.shrink(r.fuel());
                        if(output.isEmpty())inventory.setStackInSlot(2,new ItemStack(target,r.count()));else output.grow(r.count());
                        progress=0;
                    }
                    setChanged();
                }
            }
        }else if(progress!=0){progress=0;setChanged();}
        if(getBlockState().getValue(FactoryBlock.ACTIVE)!=active)server.setBlock(worldPosition,getBlockState().setValue(FactoryBlock.ACTIVE,active),3);
    }
    public void interact(Player player,ItemStack held,boolean withdraw){
        if(requirements()!=null&&BuiltInRegistries.ITEM.getKey(held.getItem()).equals(requirements().tool())&&beginCommissioning()){held.shrink(1);setChanged();}
        else if(withdraw){
            for(int slot:new int[]{2,0,1}){ItemStack stack=inventory.extractItem(slot,64,true);if(stack.isEmpty())continue;int count=stack.getCount();player.getInventory().add(stack);inventory.extractItem(slot,count-stack.getCount(),false);break;}
        }else for(int slot=0;slot<2;slot++){int count=held.getCount();ItemStack rest=inventory.insertItem(slot,held,false);int moved=count-rest.getCount();if(moved>0){held.shrink(moved);break;}}
        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Factory "+commissioning.stage+" | progress "+progress+" | 3x3 iron foundation; calibration kit required"),false);
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider provider){
        super.saveAdditional(tag,provider);if(quarantine!=null){tag.merge(quarantine.copy());return;}
        tag.putInt("dataVersion",1);tag.put("inventory",inventory.serializeNBT(provider));tag.putString("commission",commissioning.stage.name());tag.putInt("commissionTicks",commissioning.elapsed);tag.putInt("progress",progress);tag.putString("recipe",recipe);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider provider){
        super.loadAdditional(tag,provider);quarantine=null;
        if(!tag.contains("dataVersion"))return;
        try{
            if(tag.getInt("dataVersion")!=1)throw new IllegalArgumentException();
            var stored=tag.getCompound("inventory");if(stored.getInt("Size")!=3)throw new IllegalArgumentException();
            java.util.Set<Integer> slots=new java.util.HashSet<>();
            for(var element:stored.getList("Items",Tag.TAG_COMPOUND)){
                var stack=(CompoundTag)element;var id=ResourceLocation.parse(stack.getString("id"));int slot=stack.getInt("Slot");
                if(slot<0||slot>=3||!slots.add(slot)||!BuiltInRegistries.ITEM.containsKey(id)||ItemStack.parse(provider,stack).isEmpty())throw new IllegalArgumentException();
            }
            commissioning.stage=Commissioning.Stage.valueOf(tag.getString("commission"));commissioning.elapsed=tag.getInt("commissionTicks");progress=tag.getInt("progress");recipe=tag.getString("recipe");
            if(commissioning.elapsed<0||commissioning.elapsed>72000||progress<0||progress>72000||recipe.length()>256)throw new IllegalArgumentException();
            inventory.deserializeNBT(provider,stored);
        }catch(RuntimeException e){quarantine=tag.copy();}
    }
}
