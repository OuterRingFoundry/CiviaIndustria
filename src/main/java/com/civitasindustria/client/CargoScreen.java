package com.civitasindustria.client;
import com.civitasindustria.common.warehouse.CargoMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class CargoScreen extends AbstractContainerScreen<CargoMenu>{
    public CargoScreen(CargoMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=166;inventoryLabelY=72;}
    @Override protected void renderBg(GuiGraphics g,float p,int x,int y){InventoryPanels.panel(g,leftPos,topPos,imageWidth,imageHeight);InventoryPanels.slots(g,menu,leftPos,topPos);}
    @Override public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);if(hoveredSlot!=null&&hoveredSlot.index<16)g.renderTooltip(font,Component.literal("Stored: "+menu.count(hoveredSlot.index)+" | Shift-click: transfer stack"),x,y);}
}
