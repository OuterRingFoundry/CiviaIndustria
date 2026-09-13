package com.civitasindustria.common.threat;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import java.util.EnumSet;
/** Original scavenger constructs. Role and action change only at meaningful transitions. */
public final class IndustrialRaider extends Zombie {
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(IndustrialRaider.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WINDUP=SynchedEntityData.defineId(IndustrialRaider.class,EntityDataSerializers.BOOLEAN);
    public IndustrialRaider(EntityType<? extends Zombie> type,Level level){super(type,level);setCanPickUpLoot(false);xpReward=0;getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0);}
    @Override public void tick(){
        // A temporarily inaccessible member may outlive offline/expiry cleanup. It cannot resume unbudgeted AI.
        if(!level().isClientSide&&ThreatDirector.targetCell(this)==null){discard();return;}
        super.tick();
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(ROLE,0);builder.define(WINDUP,false);}
    public int role(){return entityData.get(ROLE);}public boolean windingUp(){return entityData.get(WINDUP);}
    public void configureRole(int value){
        int role=Math.clamp(value,0,2);entityData.set(ROLE,role);setBaby(false);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(role==1?36:role==2?24:18);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(role==0?.30:role==1?.19:.24);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(role==1?5:3);setHealth(getMaxHealth());
    }
    @Override public net.minecraft.network.chat.Component getName(){return hasCustomName()?super.getName():net.minecraft.network.chat.Component.literal(switch(role()){case 1->"Rivet Breaker";case 2->"Rivet Saboteur";default->"Rivet Runner";});}
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return SoundEvents.IRON_GOLEM_STEP;}
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return SoundEvents.IRON_GOLEM_HURT;}
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return SoundEvents.IRON_GOLEM_DEATH;}
    @Override protected boolean isSunSensitive(){return false;}
    @Override protected boolean convertsInWater(){return false;}
    @Override public boolean killedEntity(ServerLevel level,LivingEntity victim){return true;} // Constructs cannot convert villagers into unbudgeted zombies.
    @Override protected void registerGoals(){super.registerGoals();goalSelector.addGoal(1,new InfrastructureGoal());}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("CivitasRole",role());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(ROLE,Math.clamp(tag.getInt("CivitasRole"),0,2));entityData.set(WINDUP,false);}
    private final class InfrastructureGoal extends Goal {
        private BlockPos target;private int cooldown,age,windup;
        @Override public boolean requiresUpdateEveryTick(){return true;}
        InfrastructureGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){
            if(!(level() instanceof ServerLevel server)||cooldown-->0)return false;cooldown=40;
            var cell=ThreatDirector.targetCell(IndustrialRaider.this);
            target=cell==null?null:WorldRuntime.get(server).threatTarget(server,cell);return target!=null;
        }
        @Override public void start(){age=0;windup=0;}
        @Override public boolean canContinueToUse(){return target!=null&&age<200&&level().hasChunkAt(target)&&!level().getBlockState(target).isAir();}
        private boolean reachable(){
            if(target==null||distanceToSqr(target.getX()+.5,target.getY()+.5,target.getZ()+.5)>6.25)return false;
            var hit=level().clip(new net.minecraft.world.level.ClipContext(getEyePosition(),net.minecraft.world.phys.Vec3.atCenterOf(target),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,IndustrialRaider.this));
            return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS||hit.getBlockPos().equals(target);
        }
        @Override public void tick(){
            if(!(level() instanceof ServerLevel server)||target==null)return;age++;
            getLookControl().setLookAt(target.getX()+.5,target.getY()+.5,target.getZ()+.5);
            if(!reachable()){windup=0;entityData.set(WINDUP,false);if(age%20==1)getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,1.0);return;}
            getNavigation().stop();int duration=role()==0?16:role()==1?32:24;
            if(windup==0){entityData.set(WINDUP,true);playSound(SoundEvents.CHAIN_PLACE,.6f,role()==1?.6f:1.3f);}
            if(++windup>=duration){
                WorldRuntime.get(server).sabotage(target,server.getGameTime(),role()==1?2:1,role()==2?200:role()==0?60:100);swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                playSound(SoundEvents.ANVIL_HIT,.65f,role()==1?.65f:1.2f);entityData.set(WINDUP,false);windup=0;age=200;
            }
        }
        @Override public void stop(){target=null;windup=0;entityData.set(WINDUP,false);getNavigation().stop();}
    }
}
