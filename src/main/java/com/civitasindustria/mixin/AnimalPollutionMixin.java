package com.civitasindustria.mixin;

import com.civitasindustria.platform.AnimalPollution;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public abstract class AnimalPollutionMixin {
    @Unique private boolean civitas$sampled, civitas$explicitAge;
    @Unique private int civitas$nextSampleTick;
    @Unique private double civitas$growth=1, civitas$fraction;
    @Unique private void civitas$refresh(Animal animal){
        if(!animal.level().isClientSide&&animal.isAlive()
            &&(!civitas$sampled||animal.tickCount-civitas$nextSampleTick>=0)){
            civitas$nextSampleTick=animal.tickCount+(civitas$sampled?100:1+Math.floorMod(animal.getId(),100));civitas$sampled=true;
            civitas$growth=AnimalPollution.refresh(animal);
        }
    }
    @Inject(method="aiStep",at=@At("HEAD"))
    private void civitas$refreshEnvironment(CallbackInfo callback){
        if((Object)this instanceof Animal animal)civitas$refresh(animal);
    }
    // Feeding and NBT loading are explicit age changes, including a final -1 -> 0 transition.
    @Inject(method={"ageUp(IZ)V","readAdditionalSaveData"},at=@At("HEAD"))
    private void civitas$beginExplicitAge(CallbackInfo callback){civitas$explicitAge=true;}
    @Inject(method={"ageUp(IZ)V","readAdditionalSaveData"},at=@At("RETURN"))
    private void civitas$endExplicitAge(CallbackInfo callback){civitas$explicitAge=false;}
    @Inject(method="setAge",at=@At("HEAD"),cancellable=true)
    private void civitas$naturalGrowth(int nextAge,CallbackInfo callback){
        if(civitas$explicitAge||!((Object)this instanceof Animal animal)||animal.level().isClientSide)return;
        int age=animal.getAge();
        // Vanilla and ServerCore inactive ticks both use one-tick increments. Adult cooldowns are negative increments.
        if(age>=0||nextAge!=age+1)return;
        civitas$refresh(animal);
        if(civitas$growth>=1)return;
        civitas$fraction+=civitas$growth;
        if(civitas$fraction+1e-9<1){callback.cancel();return;}
        civitas$fraction=Math.max(0,civitas$fraction-1);
    }
}
