package com.civitasindustria.compat.create;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
/** This class is reached only behind the Create-presence guard. */
public final class PowerBridge {
    public static DeferredHolder<BlockEntityType<?>,BlockEntityType<PowerBridgeEntity>> ENTITY;
    public static void registerContent(){
        for(String id:java.util.List.of("electric_motor","rotation_dynamo"))CivitasRegistries.CONTENT.put(id,CivitasRegistries.BLOCKS.register(id,()->new PowerBridgeBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(4,3600000).noOcclusion().sound(net.minecraft.world.level.block.SoundType.METAL).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK))));
        ENTITY=CivitasRegistries.BLOCK_ENTITIES.register("power_bridge",()->BlockEntityType.Builder.of(PowerBridgeEntity::new,CivitasRegistries.CONTENT.get("electric_motor").get(),CivitasRegistries.CONTENT.get("rotation_dynamo").get()).build(null));
    }
    public static void capabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e){e.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,ENTITY.get(),(be,side)->side==be.getBlockState().getValue(PowerBridgeBlock.FACING)?null:be.port);}
}
