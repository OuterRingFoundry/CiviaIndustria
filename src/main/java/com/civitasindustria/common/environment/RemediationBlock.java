package com.civitasindustria.common.environment;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
/** Earned regional cleanup; no ticking entity or immediate ecological reset. */
public final class RemediationBlock extends Block {
    public RemediationBlock(Properties properties){super(properties);}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!stack.is(CivitasRegistries.REMEDIATION_REAGENT.get()))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(level instanceof ServerLevel server){
            var runtime=WorldRuntime.get(server);var cell=runtime.state().cells.get(CellPos.fromBlock(pos.getX(),pos.getZ()));
            if(cell!=null){
                double removed=0;
                for(Pollutant pollutant:new Pollutant[]{Pollutant.W_INDUSTRIAL,Pollutant.W_ACIDITY,Pollutant.W_TOXICITY,Pollutant.SOIL_ACIDITY,Pollutant.SOIL_TOXICITY}){
                    double amount=Math.min(25,cell.get(pollutant));cell.add(pollutant,-amount);removed+=amount;
                }
                if(removed>0){stack.shrink(1);runtime.dirty();player.displayClientMessage(net.minecraft.network.chat.Component.literal("Remediation applied. Vegetation and water quality recover gradually."),true);}
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
