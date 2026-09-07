package com.civitasindustria.mixin;
import com.civitasindustria.common.factory.MachineCommissioning;
import com.civitasindustria.compat.immersiveengineering.IECommissioning;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets="blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperMaster",remap=false)
public abstract class IECommissioningMixin {
    @Shadow(remap=false) public abstract BlockEntity getMasterBE();
    @Inject(method="tickServer",at=@At("HEAD"),cancellable=true,remap=false)
    private void civitas$commissionedProcessing(CallbackInfo callback){
        var machine=getMasterBE();if(!MachineCommissioning.canOperate(machine)){IECommissioning.suspend(machine);callback.cancel();}
    }
}
