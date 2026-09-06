package com.civitasindustria.platform;
import com.civitasindustria.common.cargo.Encumbrance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import java.util.*;
public final class CargoMovement {
    private static final ResourceLocation PENALTY=ResourceLocation.fromNamespaceAndPath("civitas_industria","cargo_weight");
    private static final Map<UUID,Integer> STATES=new HashMap<>();
    private CargoMovement(){}
    public static void register(IEventBus bus){bus.addListener(CargoMovement::tick);bus.addListener(CargoMovement::jump);bus.addListener(CargoMovement::travel);bus.addListener(CargoMovement::teleport);bus.addListener(CargoMovement::logout);}
    private static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        if(player.tickCount%100==0)com.civitasindustria.common.network.EnvironmentPayload.send(player);
        int state=STATES.getOrDefault(player.getUUID(),0);
        if(player.tickCount%10==0){
            state=player.isCreative()||player.isSpectator()?0:Encumbrance.state(Encumbrance.carried(player));STATES.put(player.getUUID(),state);
            var speed=player.getAttribute(Attributes.MOVEMENT_SPEED);
            if(speed!=null){speed.removeModifier(PENALTY);if(state>0)speed.addTransientModifier(new AttributeModifier(PENALTY,state==1?-.2:state==2?-.5:-.8,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
        }
        if(state>0)player.setSprinting(false);
        if(state>=2&&player.isFallFlying())player.stopFallFlying();
    }
    private static void jump(LivingEvent.LivingJumpEvent event){
        if(event.getEntity() instanceof ServerPlayer player&&STATES.getOrDefault(player.getUUID(),0)>=2){player.setDeltaMovement(player.getDeltaMovement().multiply(1,.4,1));player.hurtMarked=true;}
    }
    private static void travel(EntityTravelToDimensionEvent event){
        if(event.getEntity() instanceof ServerPlayer player&&!player.isCreative()&&Encumbrance.carried(player)>0)event.setCanceled(true);
    }
    private static void teleport(net.neoforged.neoforge.event.entity.EntityTeleportEvent event){
        if(event.getEntity() instanceof ServerPlayer player&&!player.isCreative()&&!player.isSpectator()&&Encumbrance.carried(player)>0)event.setCanceled(true);
    }
    private static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event){STATES.remove(event.getEntity().getUUID());}
    public static void clear(){STATES.clear();}
}
