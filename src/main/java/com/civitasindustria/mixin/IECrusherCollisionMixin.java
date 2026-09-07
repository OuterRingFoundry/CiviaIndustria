package com.civitasindustria.mixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets="blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic",remap=false)
public abstract class IECrusherCollisionMixin {
    @Inject(method="onEntityCollision",at=@At("HEAD"),cancellable=true,remap=false)
    private void civitas$commissionedCollision(@Coerce Object context,net.minecraft.core.BlockPos relative,net.minecraft.world.entity.Entity entity,CallbackInfo callback){
        if(!entity.level().isClientSide&&!com.civitasindustria.compat.immersiveengineering.IECommissioning.collisionAllowed(context))callback.cancel();
    }
}
