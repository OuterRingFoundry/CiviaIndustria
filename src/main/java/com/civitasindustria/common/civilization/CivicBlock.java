package com.civitasindustria.common.civilization;
import com.civitasindustria.domain.WorldState;
import com.civitasindustria.platform.WorldRuntime;
import com.civitasindustria.common.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class CivicBlock extends Block {
    private final WorldState.CivicNode.Kind kind;
    public CivicBlock(Properties p,WorldState.CivicNode.Kind kind){super(p);this.kind=kind;}
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){
        if(level instanceof ServerLevel server&&placer!=null)WorldRuntime.get(server).nodePlaced(pos,placer.getUUID(),kind);
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!state.is(next.getBlock())&&level instanceof ServerLevel server)WorldRuntime.get(server).nodeRemoved(pos);
        super.onRemove(state,level,pos,next,moving);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(stack.is(Items.IRON_INGOT)&&level instanceof ServerLevel server){
            var runtime=WorldRuntime.get(server);var node=runtime.state().nodes.get(pos.asLong());
            if(node!=null&&(kind==WorldState.CivicNode.Kind.CORE||kind==WorldState.CivicNode.Kind.MAINTENANCE||kind==WorldState.CivicNode.Kind.DEFENSE)){
                node.credits=Math.min(Long.MAX_VALUE-100000,node.credits)+ServerConfig.CREDIT_PER_INGOT.get();
                if(!player.isCreative())stack.shrink(1);runtime.dirty();
                player.displayClientMessage(Component.literal("Maintenance credits: "+node.credits),true);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level instanceof ServerLevel server){var n=WorldRuntime.get(server).state().nodes.get(pos.asLong());
            player.displayClientMessage(Component.literal(n==null?"Unregistered node; replace it.":kind+" | credits "+n.credits+" | missed payments "+n.missedPayments),false);}
        return InteractionResult.SUCCESS;
    }
}
