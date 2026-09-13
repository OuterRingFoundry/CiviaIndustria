package com.civitasindustria.compat.chimneys;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.environment.ChimneyOutlet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;
public final class ChimneyChecks {
    public static void verify(GameTestHelper h){BlockPos p=new BlockPos(2,1,2);h.setBlock(p,CivitasRegistries.CONTENT.get("factory_controller").get());var factory=h.getBlockState(p).getBlock();if(!(factory instanceof com.endertech.minecraft.mods.adchimneys.smoke.ISmokeEmitter emitter)||emitter.getEmitter(h.getLevel(),h.absolutePos(p))==null)throw new AssertionError("Factory native smoke source missing");var chimney=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("adchimneys:metal_chimney"));h.setBlock(p.above(),chimney);h.setBlock(p.above(2),chimney);BlockPos outlet=ChimneyOutlet.find(h.getLevel(),h.absolutePos(p));if(!outlet.equals(h.absolutePos(p.above(3))))throw new AssertionError("Physical chimney outlet not found: "+outlet);h.setBlock(p.above(3),net.minecraft.world.level.block.Blocks.STONE);if(!ChimneyOutlet.find(h.getLevel(),h.absolutePos(p)).equals(h.absolutePos(p)))throw new AssertionError("Blocked chimney silently vented");}
}
