package com.civitasindustria.common.factory;

import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.Commissioning;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Location-bound commissioning in the owning BE's persistent data. No global entity cache. */
public final class MachineCommissioning {
    private static final ResourceLocation CRUSHING_WHEEL=ResourceLocation.parse("create:crushing_wheel");
    public static final String KEY="CivitasCommissioning";
    private MachineCommissioning(){}
    public static CommissioningRequirements.Rule rule(BlockEntity machine){
        return machine==null?null:CommissioningRequirements.INSTANCE.get(BuiltInRegistries.BLOCK.getKey(machine.getBlockState().getBlock()));
    }
    private static boolean valid(CompoundTag tag){
        try{return tag.contains("version",Tag.TAG_INT)&&tag.getInt("version")==1&&tag.contains("position",Tag.TAG_LONG)
            &&tag.contains("dimension",Tag.TAG_STRING)&&tag.getString("dimension").length()<=256&&tag.contains("elapsed",Tag.TAG_INT)&&tag.contains("stage",Tag.TAG_STRING)
            &&ResourceLocation.tryParse(tag.getString("dimension"))!=null&&tag.getInt("elapsed")>=0&&tag.getInt("elapsed")<=72000
            &&Commissioning.Stage.valueOf(tag.getString("stage"))!=null;}catch(RuntimeException ignored){return false;}
    }
    private static boolean bound(BlockEntity machine,CompoundTag tag){return machine.getLevel()!=null&&tag.getLong("position")==machine.getBlockPos().asLong()&&tag.getString("dimension").equals(machine.getLevel().dimension().location().toString());}
    public static boolean foundation(BlockEntity machine){
        var rule=rule(machine);if(rule==null||!(machine.getLevel() instanceof ServerLevel level))return false;
        if(rule.multiblock())return net.neoforged.fml.ModList.get().isLoaded("immersiveengineering")&&com.civitasindustria.compat.immersiveengineering.IECommissioning.foundation(machine,rule.foundationTag());
        var material=TagKey.create(Registries.BLOCK,rule.foundationTag());
        for(int x=-rule.radius();x<=rule.radius();x++)for(int z=-rule.radius();z<=rule.radius();z++){
            var pos=machine.getBlockPos().offset(x,-rule.depth(),z);if(!level.hasChunkAt(pos)||!level.getBlockState(pos).is(material))return false;
        }
        return true;
    }
    private static boolean loadAllowed(BlockEntity machine){return machine.getLevel() instanceof ServerLevel level&&WorldRuntime.get(level).load(machine.getBlockPos())<ServerConfig.LOAD_LIMIT.get();}
    public static Commissioning.Stage stage(BlockEntity machine){
        var root=machine.getPersistentData();if(!root.contains(KEY))return Commissioning.Stage.UNCOMMISSIONED;
        var tag=root.getCompound(KEY);return valid(tag)&&bound(machine,tag)?Commissioning.Stage.valueOf(tag.getString("stage")):Commissioning.Stage.DEGRADED;
    }
    public static boolean canOperate(BlockEntity machine){
        if(rule(machine)==null)return true;
        if(!(machine.getLevel() instanceof ServerLevel level))return true; // Only the server authorizes production.
        var root=machine.getPersistentData();if(!root.contains(KEY,Tag.TAG_COMPOUND))return false;
        var tag=root.getCompound(KEY);if(!valid(tag))return false; // Preserve unknown/corrupt payload verbatim.
        if(!bound(machine,tag))return false;
        long now=level.getGameTime(),last=tag.getLong("checkedAt");
        if(!tag.contains("checkedAt",Tag.TAG_LONG)||last>now||now-last>=20){
            var state=new Commissioning();state.stage=Commissioning.Stage.valueOf(tag.getString("stage"));state.elapsed=tag.getInt("elapsed");
            var before=state.stage;int elapsed=state.elapsed;
            state.update(foundation(machine),loadAllowed(machine),20,ServerConfig.COMMISSION_TICKS.get());
            tag.putLong("checkedAt",now);tag.putString("stage",state.stage.name());tag.putInt("elapsed",state.elapsed);
            if(state.stage!=before||elapsed!=state.elapsed)machine.setChanged();
        }
        return Commissioning.Stage.READY.name().equals(tag.getString("stage"));
    }
    public static boolean begin(BlockEntity machine,ItemStack kit){
        var rule=rule(machine);if(rule==null||!(machine.getLevel() instanceof ServerLevel level)||!BuiltInRegistries.ITEM.getKey(kit.getItem()).equals(rule.tool())||kit.isEmpty())return false;
        var root=machine.getPersistentData();if(root.contains(KEY)&&(!root.contains(KEY,Tag.TAG_COMPOUND)||!valid(root.getCompound(KEY))))return false;
        var old=stage(machine);if(old==Commissioning.Stage.READY||old==Commissioning.Stage.COMMISSIONING||!foundation(machine)||!loadAllowed(machine))return false;
        var tag=new CompoundTag();tag.putInt("version",1);tag.putLong("position",machine.getBlockPos().asLong());tag.putString("dimension",level.dimension().location().toString());tag.putString("stage",Commissioning.Stage.COMMISSIONING.name());tag.putInt("elapsed",0);tag.putLong("checkedAt",level.getGameTime());root.put(KEY,tag);
        kit.shrink(1);machine.setChanged();return true;
    }
    public static boolean decommission(BlockEntity machine){
        var root=machine.getPersistentData();if(!root.contains(KEY,Tag.TAG_COMPOUND)||!valid(root.getCompound(KEY)))return false;
        var tag=root.getCompound(KEY);tag.putString("stage",Commissioning.Stage.UNCOMMISSIONED.name());tag.putInt("elapsed",0);machine.setChanged();return true;
    }
    public static boolean movable(BlockEntity machine){
        if(rule(machine)==null)return true;
        if(!machine.getPersistentData().contains(KEY))return true;
        return machine.getPersistentData().contains(KEY,Tag.TAG_COMPOUND)&&valid(machine.getPersistentData().getCompound(KEY))&&Commissioning.Stage.UNCOMMISSIONED.name().equals(machine.getPersistentData().getCompound(KEY).getString("stage"));
    }
    public static boolean crusherCanOperate(BlockEntity controller){
        if(!(controller.getLevel() instanceof ServerLevel level))return true;
        int wheels=0;boolean ready=true;
        for(var direction:Direction.values()){
            var pos=controller.getBlockPos().relative(direction);if(!level.hasChunkAt(pos))continue;
            if(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).equals(CRUSHING_WHEEL)){wheels++;var machine=level.getBlockEntity(pos);if(machine==null||!canOperate(machine))ready=false;}
        }
        return wheels==2&&ready;
    }
    public static void register(IEventBus bus){bus.addListener(EventPriority.LOWEST,MachineCommissioning::interact);}
    private static void interact(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getLevel() instanceof ServerLevel level))return;
        BlockEntity machine=level.getBlockEntity(event.getPos());if(machine==null||machine instanceof FactoryBlockEntity)return;
        if(net.neoforged.fml.ModList.get().isLoaded("immersiveengineering"))machine=com.civitasindustria.compat.immersiveengineering.IECommissioning.resolve(machine);
        var rule=rule(machine);if(rule==null)return;ItemStack held=event.getItemStack();
        boolean kit=BuiltInRegistries.ITEM.getKey(held.getItem()).equals(rule.tool());if(!kit)return;
        Player player=event.getEntity();boolean changed=player.isShiftKeyDown()?decommission(machine):begin(machine,held);
        String status=stage(machine).name();
        player.displayClientMessage(Component.literal("Machine "+status+(changed?"":" — needs its heavy foundation, calibration kit and acceptable regional load")+" | sneak with kit to decommission"),true);
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
