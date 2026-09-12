package com.civitasindustria.compat.chimneys;
import com.civitasindustria.common.factory.FactoryBlock;
import com.endertech.minecraft.mods.adchimneys.smoke.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
/** Optional native smoke source. The upstream chimney mod owns smoke routing. */
public final class ChimneyFactoryBlock extends FactoryBlock implements ISmokeEmitter {
    private Emitter emitter;
    public ChimneyFactoryBlock(Properties p){super(p);}
    @Override public Emitter getEmitter(LevelReader level,BlockPos pos){if(emitter==null)emitter=new Emitter(Emitter.Properties.tile(ACTIVE).smoke(0xff69645b,0.5f).maxGapLength(1));return emitter;}
    @Override protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.BaseEntityBlock> codec(){return simpleCodec(ChimneyFactoryBlock::new);}
}
