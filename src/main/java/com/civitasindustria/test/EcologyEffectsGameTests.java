package com.civitasindustria.test;

import com.civitasindustria.domain.*;
import com.civitasindustria.platform.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class EcologyEffectsGameTests {
    @GameTest(template="empty",batch="ecology_effects",timeoutTicks=360)
    public static void cropsAnimalHealthAndMaturation(GameTestHelper h){
        var level=h.getLevel();var runtime=WorldRuntime.get(level);
        // Explicit disposable pen, isolated from other GameTest pollution cells.
        var pos=new BlockPos(4098,-59,4098);var region=CellPos.fromBlock(pos.getX(),pos.getZ());
        level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);level.getChunkAt(pos);
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)level.setBlock(pos.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
        CellData original=runtime.state().cells.get(region),cell=new CellData();cell.add(Pollutant.PM,5000);runtime.state().cells.put(region,cell);
        var baby=EntityType.COW.create(level);var adult=EntityType.COW.create(level);
        for(var cow:new net.minecraft.world.entity.animal.Cow[]{baby,adult}){cow.setNoAi(true);cow.setInvulnerable(true);cow.moveTo(pos.getX()+.5+(cow==adult?4:0),pos.getY(),pos.getZ()+.5,0,0);level.addFreshEntity(cow);}
        baby.setAge(-24000);adult.setAge(600);
        var other=ResourceLocation.parse("civitas_industria:test_other_health");
        baby.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(other,2,AttributeModifier.Operation.ADD_VALUE));
        int[] recoveredAge={0};
        h.runAtTickTime(130,()->{
            if(Math.abs(baby.getMaxHealth()-7.2)>1e-4||baby.getAttribute(Attributes.MAX_HEALTH).getBaseValue()!=10)throw new AssertionError("Pollution did not preserve base/other modifiers: "+baby.getMaxHealth());
            int grown=baby.getAge()+24000;
            if(baby.tickCount<80||Math.abs(grown-(int)Math.floor(baby.tickCount*.35))>1)throw new AssertionError("Expected slowed natural maturation, observed "+grown);
            if(adult.tickCount<80||adult.getAge()!=600-adult.tickCount)throw new AssertionError("Adult breeding cooldown changed: age="+adult.getAge()+" ticks="+adult.tickCount);
            int beforeFeeding=baby.getAge();baby.ageUp(1);
            if(baby.getAge()!=beforeFeeding+20)throw new AssertionError("Paid/explicit age acceleration was slowed");
            baby.setAge(beforeFeeding);
            CompoundTag saved=new CompoundTag();baby.saveWithoutId(saved);var restored=EntityType.COW.create(level);restored.load(saved);
            AnimalPollution.refresh(restored);
            if(restored.getAge()!=baby.getAge()||Math.abs(restored.getMaxHealth()-7.2)>1e-4||restored.getAttribute(Attributes.MAX_HEALTH).getModifier(other)==null)throw new AssertionError("Animal reload lost age or stacked health penalty");
            int accepted=0;
            for(int n=0;n<1000;n++){var event=new CropGrowEvent.Pre(level,pos,Blocks.WHEAT.defaultBlockState());NeoForge.EVENT_BUS.post(event);if(event.getResult()!=CropGrowEvent.Pre.Result.DO_NOT_GROW)accepted++;}
            if(accepted<70||accepted>240)throw new AssertionError("Polluted crop event rate outside expected 15% range: "+accepted);
            // Explicitly restore the disposable pen and its transport halo for the recovery half.
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)runtime.state().cells.remove(region.offset(x,z));
            baby.setHealth(5);
            var allowed=new CropGrowEvent.Pre(level,pos,Blocks.WHEAT.defaultBlockState());NeoForge.EVENT_BUS.post(allowed);
            if(allowed.getResult()==CropGrowEvent.Pre.Result.DO_NOT_GROW)throw new AssertionError("Pristine crop growth blocked");
        });
        h.runAtTickTime(260,()->{
            if(Math.abs(baby.getMaxHealth()-12)>1e-4||Math.abs(baby.getHealth()-5)>1e-4||baby.getAttribute(Attributes.MAX_HEALTH).getModifier(AnimalPollution.HEALTH)!=null)throw new AssertionError("Recovery must remove only pollution modifier, without free healing");
            recoveredAge[0]=baby.getAge();
        });
        h.runAtTickTime(300,()->{
            if(baby.getAge()-recoveredAge[0]!=40)throw new AssertionError("Recovered natural maturation not vanilla speed");
            baby.discard();adult.discard();
            if(original==null)runtime.state().cells.remove(region);else runtime.state().cells.put(region,original);
            level.setChunkForced(pos.getX()>>4,pos.getZ()>>4,false);runtime.dirty();
            org.slf4j.LoggerFactory.getLogger(EcologyEffectsGameTests.class).info("CIVITAS ECOLOGY EFFECTS PASS: crop events, natural animal aging, health modifiers, NBT and recovery");
            h.succeed();
        });
    }
}
