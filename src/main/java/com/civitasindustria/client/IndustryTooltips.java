package com.civitasindustria.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Assembly instructions are available directly in the creative inventory. */
@EventBusSubscriber(modid="civitas_industria", value=Dist.CLIENT)
public final class IndustryTooltips {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        var id=BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!id.getNamespace().equals("civitas_industria")) return;
        String key=switch(id.getPath()) {
            case "warehouse_controller" -> "controller";
            case "warehouse_casing" -> "casing";
            case "warehouse_port" -> "port";
            default -> null;
        };
        if (key!=null) event.getToolTip().add(Component.translatable("tooltip.civitas_industria.warehouse."+key).withStyle(ChatFormatting.GRAY));
    }
}
