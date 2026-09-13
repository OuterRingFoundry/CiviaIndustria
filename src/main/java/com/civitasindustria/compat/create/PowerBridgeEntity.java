package com.civitasindustria.compat.create;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.civitasindustria.common.workshop.RetainedMachine;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;
/** Real stress load and lossy conversion. No persistent or transient energy is minted by simulation. */
public final class PowerBridgeEntity extends GeneratingKineticBlockEntity implements RetainedMachine {
    public static final int CAPACITY=16000,MOTOR_COST=512;
    private int energy;
    private boolean powered;
    private boolean rotationRefresh=true;
    private CompoundTag quarantine;
    public final IEnergyStorage port=new IEnergyStorage(){
        public int receiveEnergy(int amount,boolean simulate){int n=canReceive()?Math.min(Math.max(0,amount),Math.min(2048,CAPACITY-energy)):0;if(!simulate&&n>0){energy+=n;setChanged();}return n;}
        public int extractEnergy(int amount,boolean simulate){int n=canExtract()?Math.min(Math.max(0,amount),Math.min(2048,energy)):0;if(!simulate&&n>0){energy-=n;setChanged();}return n;}
        public int getEnergyStored(){return energy;}public int getMaxEnergyStored(){return CAPACITY;}
        public boolean canExtract(){return quarantine==null&&!motor();}public boolean canReceive(){return quarantine==null&&motor();}
    };
    public PowerBridgeEntity(BlockPos p,BlockState s){super(PowerBridge.ENTITY.get(),p,s);}
    public boolean motor(){return BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath().equals("electric_motor");}
    public boolean retainsContents(){return quarantine!=null;}
    @Override public float calculateStressApplied(){return lastStressApplied=motor()?0:16;}
    @Override public float calculateAddedStressCapacity(){return lastCapacityProvided=motor()?16:0;}
    @Override public float getGeneratedSpeed(){return motor()&&powered&&quarantine==null?32:0;}
    @Override public void tick(){
        super.tick();if(!(level instanceof ServerLevel server))return;
        boolean permitted=quarantine==null&&!server.hasNeighborSignal(worldPosition)&&!WorldRuntime.get(server).jammed(worldPosition,server.getGameTime());
        if(motor()){
            int cost=net.neoforged.fml.ModList.get().isLoaded("createaddition")?com.civitasindustria.compat.createaddition.PowerRates.motorCost():MOTOR_COST;
            boolean next=permitted&&energy>=cost;
            if(next){energy-=cost;setChanged();}
            if(next!=powered||rotationRefresh){powered=next;rotationRefresh=false;updateGeneratedRotation();sendData();}
        }else if(quarantine==null){
            // getSpeed is zero for an overstressed native network. Clamp before converting.
            float rpm=Math.abs(getSpeed());int generated=permitted&&hasNetwork()&&hasSource()&&!isOverStressed()&&Float.isFinite(rpm)?(int)(Math.min(rpm,256)*(net.neoforged.fml.ModList.get().isLoaded("createaddition")?com.civitasindustria.compat.createaddition.PowerRates.dynamoPerRpm():8)):0;
            if(generated>0){energy=Math.min(CAPACITY,energy+generated);setChanged();}
            for(Direction face:Direction.values()){
                if(energy<=0)break;if(face==getBlockState().getValue(PowerBridgeBlock.FACING))continue;
                BlockPos other=worldPosition.relative(face);if(!server.hasChunkAt(other))continue;
                IEnergyStorage receiver=server.getCapability(Capabilities.EnergyStorage.BLOCK,other,face.getOpposite());if(receiver==null||!receiver.canReceive())continue;
                int offer=Math.min(energy,2048),accepted=receiver.receiveEnergy(offer,false);
                if(accepted<0||accepted>offer)throw new IllegalStateException("Invalid adjacent energy receiver result");
                if(accepted>0){energy-=accepted;setChanged();}
            }
        }
    }
    @Override protected void write(CompoundTag t,HolderLookup.Provider p,boolean packet){super.write(t,p,packet);if(!packet&&quarantine!=null){t.merge(quarantine.copy());return;}t.putInt("ciPowerVersion",1);t.putInt("ciEnergy",energy);if(packet)t.putBoolean("ciPowered",powered);}
    @Override protected void read(CompoundTag t,HolderLookup.Provider p,boolean packet){
        super.read(t,p,packet);if(packet){powered=t.getBoolean("ciPowered");return;}
        powered=false;rotationRefresh=true;quarantine=null;energy=0;
        if((t.contains("ciPowerVersion")||t.contains("ciEnergy"))&&(!t.contains("ciPowerVersion",Tag.TAG_INT)||t.getInt("ciPowerVersion")!=1||!t.contains("ciEnergy",Tag.TAG_INT)||t.getInt("ciEnergy")<0||t.getInt("ciEnergy")>CAPACITY)){quarantine=t.copy();return;}
        energy=t.getInt("ciEnergy");
    }
}
