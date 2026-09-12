package com.civitasindustria.client;
import com.civitasindustria.common.economy.MarketMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class MarketScreen extends AbstractContainerScreen<MarketMenu> {
    public MarketScreen(MarketMenu m,Inventory i,Component title){super(m,i,title);imageWidth=176;imageHeight=232;inventoryLabelY=138;}
    private void button(String label,int x,int y,int w,int action,String tip){addRenderableWidget(Button.builder(Component.literal(label),b->{if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}).bounds(leftPos+x,topPos+y,w,18).tooltip(Tooltip.create(Component.literal(tip))).build());}
    @Override protected void init(){super.init();button("Deposit",8,38,50,0,"Deposit 1 diamond: receive 100 crowns");button("Redeem",61,38,52,1,"Spend 100 crowns: receive 1 reserved diamond");button("Fund",116,38,52,4,"Donate 100 crowns to the shared market treasury");String[] names={"Iron","Copper","Dust","Stone"};for(int i=0;i<4;i++)button(names[i],8+40*i,62,38,10+i,"Select market good");button("Buy 1",8,112,76,2,"Buy one item at the displayed ask price");button("Sell 1",92,112,76,3,"Sell one item at the displayed bid price; market must be funded");}
    @Override protected void renderBg(GuiGraphics g,float p,int mx,int my){InventoryPanels.panel(g,leftPos,topPos,imageWidth,imageHeight);InventoryPanels.slots(g,menu,leftPos,topPos);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,8,0x404040,false);g.drawString(font,"Crowns: "+menu.number(0),8,23,0x404040,false);int good=Math.clamp(menu.data.get(18),0,3);g.drawString(font,MarketMenu.good(good).getDescription(),8,85,0x404040,false);g.drawString(font,"Buy "+menu.number(5)+" / Sell "+menu.number(6)+" / Stock "+menu.number(4),8,98,0x404040,false);g.drawString(font,playerInventoryTitle,8,138,0x404040,false);}
    @Override public void render(GuiGraphics g,int x,int y,float p){super.render(g,x,y,p);renderTooltip(g,x,y);if(x>=leftPos+8&&x<leftPos+168&&y>=topPos+20&&y<topPos+36)g.renderTooltip(font,Component.literal("Prices: "+String.format(java.util.Locale.ROOT,"%.2f",menu.number(3)/100.0)+"% | Diamonds: "+menu.number(1)+" | Treasury: "+menu.number(2)),x,y);}
}
