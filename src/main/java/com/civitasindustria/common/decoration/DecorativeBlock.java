package com.civitasindustria.common.decoration;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class DecorativeBlock extends BaseEntityBlock {
    public DecorativeBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(DecorativeBlock::new);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DecorativeEntity(p,s);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos p,Player player,BlockHitResult hit){
        if(!level.isClientSide&&level.getBlockEntity(p) instanceof DecorativeEntity decoration){decoration.change(player.isShiftKeyDown());player.displayClientMessage(net.minecraft.network.chat.Component.literal("Decoration: "+(decoration.enabled?decoration.rpm+" RPM":"stopped")+"; sneak-click changes speed"),true);}
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
