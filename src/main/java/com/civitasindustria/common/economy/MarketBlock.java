package com.civitasindustria.common.economy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
/** Stateless counter; all money and stock live in the overworld ledger. */
public final class MarketBlock extends Block {
    public MarketBlock(Properties p){super(p);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer server&&MarketMenu.mayUse(player,p))server.openMenu(new SimpleMenuProvider((id,inv,owner)->new MarketMenu(id,inv,p),Component.translatable("block.civitas_industria.market_counter")),p);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
