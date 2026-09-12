package com.civitasindustria.compat.chimneys;
import com.civitasindustria.common.factory.FactoryBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
/** Indirection keeps optional interface classes out of the registry verifier. */
public final class ChimneyIntegration {
    public static FactoryBlock createFactory(BlockBehaviour.Properties properties){return new ChimneyFactoryBlock(properties);}
}
