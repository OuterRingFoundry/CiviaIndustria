package com.civitasindustria.compat.create;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class PowerBridgeBlock extends DirectionalKineticBlock implements IBE<PowerBridgeEntity> {
    public PowerBridgeBlock(Properties p){super(p);}
    @Override public Direction.Axis getRotationAxis(BlockState s){return s.getValue(FACING).getAxis();}
    @Override public boolean hasShaftTowards(LevelReader l,BlockPos p,BlockState s,Direction face){return face==s.getValue(FACING);}
    @Override public Class<PowerBridgeEntity> getBlockEntityClass(){return PowerBridgeEntity.class;}
    @Override public BlockEntityType<? extends PowerBridgeEntity> getBlockEntityType(){return PowerBridge.ENTITY.get();}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(!l.isClientSide&&l.getBlockEntity(p) instanceof PowerBridgeEntity e){
            boolean addon=net.neoforged.fml.ModList.get().isLoaded("createaddition");
            int cost=addon?com.civitasindustria.compat.createaddition.PowerRates.motorCost():PowerBridgeEntity.MOTOR_COST;
            double rate=addon?com.civitasindustria.compat.createaddition.PowerRates.dynamoPerRpm():8;
            String conversion=e.motor()?"Motor: "+cost+" FE/t -> 32 RPM, 512 SU":String.format(java.util.Locale.ROOT,"Dynamo: 16 SU/RPM -> %.3f FE/RPM/t",rate);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(conversion+" | buffer "+e.port.getEnergyStored()+" FE | actual "+e.getSpeed()+" RPM | redstone stops"),true);
        }return InteractionResult.SUCCESS;
    }
}
