package com.civitasindustria.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractContainerMenu;
/** Original code-drawn vanilla-style panel; no upstream GUI texture copies. */
final class InventoryPanels {
    static void panel(GuiGraphics g,int x,int y,int w,int h){g.fill(x,y,x+w,y+h,0xff373737);g.fill(x+1,y+1,x+w-1,y+h-1,0xff555555);g.fill(x+2,y+2,x+w-2,y+h-2,0xffc6c6c6);g.fill(x+2,y+2,x+w-3,y+3,0xffffffff);g.fill(x+2,y+3,x+3,y+h-3,0xffffffff);}
    static void slots(GuiGraphics g,AbstractContainerMenu menu,int x,int y){for(var s:menu.slots){int a=x+s.x-1,b=y+s.y-1;g.fill(a,b,a+18,b+18,0xff373737);g.fill(a+1,b+1,a+18,b+18,0xffffffff);g.fill(a+1,b+1,a+17,b+17,0xff8b8b8b);}}
}
