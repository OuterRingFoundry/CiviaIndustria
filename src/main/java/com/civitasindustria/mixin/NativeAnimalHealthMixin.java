package com.civitasindustria.mixin;

import com.civitasindustria.common.config.ServerConfig;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Regional health has one owner. Native join-time base-stat reductions otherwise persist/stack. */
@Pseudo
@Mixin(targets="com.endertech.minecraft.mods.adpother.events.EntityEvents",remap=false)
public abstract class NativeAnimalHealthMixin {
    @Inject(method="onCreatureJoinWorld",at=@At("HEAD"),cancellable=true,remap=false)
    private static void civitas$reversibleAnimalHealth(EntityJoinLevelEvent event,CallbackInfo callback){
        if(ServerConfig.ANIMAL_EFFECTS.get()&&event.getEntity() instanceof Animal)callback.cancel();
    }
}
