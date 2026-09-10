package com.civitasindustria.mixin;

import com.civitasindustria.domain.CellPos;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The native mod exposes counters but no NeoForge counter-change event. */
@Pseudo
@Mixin(targets="com.endertech.minecraft.mods.adpother.pollution.WorldData", remap=false)
public abstract class NativePollutionChangeMixin {
    @Inject(method="tryChangePollutionLevelBy", at=@At("TAIL"), remap=false)
    private static void civitas$wakeRegion(ServerLevel level, BlockPos pos, BlockState state, int amount, CallbackInfo ci) {
        if (amount != 0 && state != null && level.hasChunkAt(pos)
                && net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("adpother"))
            WorldRuntime.get(level).nativePollutionChanged(CellPos.fromBlock(pos.getX(), pos.getZ()));
    }
}
