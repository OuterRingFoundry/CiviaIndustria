package com.civitasindustria.common.threat;
import com.civitasindustria.domain.CellPos;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import java.util.EnumSet;
public final class IndustrialRaider extends Zombie {
    public IndustrialRaider(EntityType<? extends Zombie> type,Level level){super(type,level);}
    @Override protected void registerGoals(){super.registerGoals();goalSelector.addGoal(1,new InfrastructureGoal());}
    private final class InfrastructureGoal extends Goal {
        private BlockPos target;
        private int cooldown;
        InfrastructureGoal(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){
            if(!(level() instanceof ServerLevel server))return false;
            if(cooldown-->0)return false;cooldown=20;
            target=WorldRuntime.get(server).threatTarget(server,CellPos.fromBlock(blockPosition().getX(),blockPosition().getZ()));return target!=null;
        }
        @Override public boolean canContinueToUse(){return target!=null&&level().hasChunkAt(target)&&!level().getBlockState(target).isAir();}
        @Override public void tick(){
            if(!(level() instanceof ServerLevel server)||target==null)return;
            if(tickCount%20==0)getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,1.0);
            if(tickCount%40==0&&distanceToSqr(target.getX()+.5,target.getY(),target.getZ()+.5)<9){
                WorldRuntime.get(server).sabotage(target,server.getGameTime());
                getLookControl().setLookAt(target.getX()+.5,target.getY()+.5,target.getZ()+.5);swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
        }
        @Override public void stop(){target=null;getNavigation().stop();}
    }
}
