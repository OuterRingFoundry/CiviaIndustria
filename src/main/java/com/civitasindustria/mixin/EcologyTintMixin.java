package com.civitasindustria.mixin;
import com.civitasindustria.client.ClientEnvironment;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BiomeColors.class)
public abstract class EcologyTintMixin {
    @Inject(method={"getAverageGrassColor","getAverageFoliageColor"},at=@At("RETURN"),cancellable=true)
    private static void vegetation(BlockAndTintGetter level,BlockPos pos,CallbackInfoReturnable<Integer> result){result.setReturnValue(ClientEnvironment.tint(result.getReturnValue(),pos,false));}
    @Inject(method="getAverageWaterColor",at=@At("RETURN"),cancellable=true)
    private static void water(BlockAndTintGetter level,BlockPos pos,CallbackInfoReturnable<Integer> result){result.setReturnValue(ClientEnvironment.tint(result.getReturnValue(),pos,true));}
}
