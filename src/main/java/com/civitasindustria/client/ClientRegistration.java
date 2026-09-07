package com.civitasindustria.client;
import com.civitasindustria.CivitasIndustria;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid=CivitasIndustria.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientRegistration {
    @SubscribeEvent public static void models(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event){for(String name:DecorativeRenderer.PARTS)event.register(DecorativeRenderer.model(name));}
    @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event){event.enqueueWork(()->com.civitasindustria.common.network.EnvironmentPayload.RECEIVER=ClientEnvironment::receive);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerBlockEntityRenderer(CivitasRegistries.DECORATIVE_ENTITY.get(),DecorativeRenderer::new);event.registerEntityRenderer(CivitasRegistries.RAIDER.get(),net.minecraft.client.renderer.entity.ZombieRenderer::new);}
}
