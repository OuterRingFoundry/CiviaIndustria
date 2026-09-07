package com.civitasindustria.mixin;
import com.civitasindustria.common.factory.MachineCommissioning;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets="com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity",remap=false)
public abstract class CreateCrusherCommissioningMixin {
    @Inject(method="tick",at=@At("HEAD"),cancellable=true,remap=false)
    private void civitas$commissionedProcessing(CallbackInfo callback){
        if(!MachineCommissioning.crusherCanOperate((BlockEntity)(Object)this))callback.cancel();
    }
}
