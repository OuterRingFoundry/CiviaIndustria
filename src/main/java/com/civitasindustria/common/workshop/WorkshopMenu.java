package com.civitasindustria.common.workshop;
import com.civitasindustria.common.registry.CivitasRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.*;
public final class WorkshopMenu extends AbstractContainerMenu {
    private final WorkshopEntity machine;
    public final ContainerData data;
    public WorkshopMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf){this(id,inv,(WorkshopEntity)inv.player.level().getBlockEntity(buf.readBlockPos()),new SimpleContainerData(5));}
    public WorkshopMenu(int id,Inventory inv,WorkshopEntity machine,ContainerData data){
        super(CivitasRegistries.WORKSHOP_MENU.get(),id);this.machine=machine;this.data=data;
        IItemHandler items=machine==null?new ItemStackHandler(3):machine.inventory;
        addSlot(new SlotItemHandler(items,0,35,48));addSlot(new SlotItemHandler(items,1,62,48));
        addSlot(new SlotItemHandler(items,2,134,48){@Override public boolean mayPlace(ItemStack s){return false;}});
        for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+y*9+9,8+x*18,116+y*18));
        for(int x=0;x<9;x++)addSlot(new Slot(inv,x,8+x*18,174));addDataSlots(data);
    }
    @Override public boolean stillValid(Player p){return machine!=null&&machine.mayUse(p);}
    @Override public void clicked(int slot,int button,ClickType type,Player p){if(stillValid(p))super.clicked(slot,button,type,p);}
    @Override public boolean clickMenuButton(Player p,int id){return machine!=null&&machine.select(p,id);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player)||index<0||index>=slots.size())return ItemStack.EMPTY;
        Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack s=slot.getItem(),copy=s.copy();
        if(index<3){if(!moveItemStackTo(s,3,slots.size(),true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(s,0,2,false))return ItemStack.EMPTY;
        if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,s);return copy;
    }
}
