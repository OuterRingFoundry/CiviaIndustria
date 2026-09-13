package com.civitasindustria.client;
import com.civitasindustria.CivitasIndustria;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@EventBusSubscriber(modid=CivitasIndustria.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientRegistration {
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(RivetRaiderModel.LAYER,RivetRaiderModel::layer);}

    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){event.register(CivitasRegistries.WORKSHOP_MENU.get(),WorkshopScreen::new);event.register(CivitasRegistries.MARKET_MENU.get(),MarketScreen::new);event.register(CivitasRegistries.CARGO_MENU.get(),CargoScreen::new);}

    @SubscribeEvent public static void models(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event){for(String name:DecorativeRenderer.PARTS)event.register(DecorativeRenderer.model(name));for(String part:java.util.List.of("workshop_chuck","workshop_drill","power_rotor"))event.register(WorkshopRenderer.model(part));}
    @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event){event.enqueueWork(()->com.civitasindustria.common.network.EnvironmentPayload.RECEIVER=ClientEnvironment::receive);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerBlockEntityRenderer(CivitasRegistries.DECORATIVE_ENTITY.get(),DecorativeRenderer::new);event.registerBlockEntityRenderer(CivitasRegistries.WORKSHOP_ENTITY.get(),WorkshopRenderer::new);if(net.neoforged.fml.ModList.get().isLoaded("create"))PowerBridgeRenderer.register(event);event.registerEntityRenderer(CivitasRegistries.RAIDER.get(),RivetRaiderRenderer::new);}
}
