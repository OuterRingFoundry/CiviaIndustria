package com.civitasindustria.client;
import com.civitasindustria.common.workshop.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
/** Native menu GUI: no client-authoritative inventory, energy or recipe payload. */
public final class WorkshopScreen extends AbstractContainerScreen<WorkshopMenu> {
    public WorkshopScreen(WorkshopMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=198;inventoryLabelY=104;}
    @Override protected void init(){super.init();String[] modes={"Turn","Mill","Drill"};for(int i=0;i<3;i++){final int operation=i;addRenderableWidget(Button.builder(Component.literal(modes[i]),b->{if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,operation);}).bounds(leftPos+8+i*54,topPos+21,51,18).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(new String[]{"Iron ingot -> precision shaft", "Iron plate -> gear blank", "Iron plate -> mounting plate"}[i]))).build());}}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        int x=leftPos,y=topPos;InventoryPanels.panel(g,x,y,imageWidth,imageHeight);InventoryPanels.slots(g,menu,x,y);
        g.fill(x+9,y+48,x+18,y+83,0xff171d1c);int charge=33*menu.data.get(0)/WorkshopEntity.CAPACITY;g.fill(x+10,y+82-charge,x+17,y+82,0xffe6b951);
        int progress=28*menu.data.get(1)/Math.max(1,menu.data.get(4));g.fill(x+90,y+53,x+119,y+59,0xff152425);g.fill(x+90,y+53,x+90+progress,y+59,0xffdfb85f);
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,8,8,0xff404040,false);String[] modes={"Turning","Milling","Drilling"};int mode=Math.clamp(menu.data.get(2),0,2);
        g.drawString(font,"Raw",34,39,0xff404040,false);g.drawString(font,"Tool",61,39,0xff404040,false);g.drawString(font,"Part",133,39,0xff404040,false);
        g.drawString(font,modes[mode]+" / "+menu.data.get(0)+" FE",25,72,0xff404040,false);
        String[] states={"Machining", "Needs calibration", "Redstone stopped", "Sabotaged", "Needs stock / insert", "Output blocked", "Needs power", "Data quarantined"};
        g.drawString(font,states[Math.clamp(menu.data.get(3),0,7)],8,89,0xff404040,false);g.drawString(font,playerInventoryTitle,8,104,0xff302c28,false);
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
