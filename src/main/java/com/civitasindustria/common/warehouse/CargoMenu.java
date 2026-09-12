package com.civitasindustria.common.warehouse;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.Parcel;
import com.civitasindustria.platform.ParcelProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
/** A live view, never a second inventory. Bulk slots use delta transfers only. */
public final class CargoMenu extends AbstractContainerMenu {
    private final CargoBlockEntity cargo;
    public final ContainerData counts=new SimpleContainerData(64);
    private final SimpleContainer view=new SimpleContainer(16);
    public CargoMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf){this(id,inv,(CargoBlockEntity)null);buf.readBlockPos();}
    public CargoMenu(int id,Inventory inv,CargoBlockEntity cargo){super(CivitasRegistries.CARGO_MENU.get(),id);this.cargo=cargo;for(int y=0;y<2;y++)for(int x=0;x<8;x++)addSlot(new Slot(view,x+y*8,17+x*18,24+y*18){@Override public boolean mayPlace(ItemStack s){return false;}});for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+y*9+9,8+x*18,84+y*18));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,8+x*18,142));addDataSlots(counts);refresh();}
    public static void open(Player player,CargoBlockEntity cargo){if(player instanceof ServerPlayer p&&mayUse(p,cargo))p.openMenu(new SimpleMenuProvider((id,inv,owner)->new CargoMenu(id,inv,cargo),Component.literal("Bulk storage")),cargo.getBlockPos());}
    private static boolean mayUse(Player p,CargoBlockEntity cargo){if(cargo==null||cargo.isRemoved()||p.distanceToSqr(cargo.getBlockPos().getCenter())>64)return false;var a=cargo.authority();if(a==null||!a.available())return false;return !(p instanceof ServerPlayer s)||(ParcelProtection.allows(s.serverLevel(),cargo.getBlockPos(),p,Parcel.Flag.INTERACT)&&ParcelProtection.allows(s.serverLevel(),cargo.getBlockPos(),p,Parcel.Flag.CONTAINER)&&ParcelProtection.allows(s.serverLevel(),a.getBlockPos(),p,Parcel.Flag.CONTAINER));}
    @Override public boolean stillValid(Player p){return p.level().isClientSide||mayUse(p,cargo);}
    public long count(int slot){long value=0;for(int i=0;i<4;i++)value|=(long)(counts.get(slot*4+i)&65535)<<(i*16);return value;}
    private void refresh(){if(cargo==null)return;var a=cargo.authority();for(int i=0;i<16;i++){view.setItem(i,cargo.handler.getStackInSlot(i));long count=a==null?0:a.slotCount(i);for(int j=0;j<4;j++)counts.set(i*4+j,(int)(count>>>(16*j))&65535);}}
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    @Override public void clicked(int index,int button,ClickType type,Player p){if(!stillValid(p))return;if(index>=0&&index<16){if(p.level().isClientSide)return;if(type==ClickType.QUICK_MOVE){quickMoveStack(p,index);}else if(type==ClickType.PICKUP&&(button==0||button==1)){var carried=getCarried();if(carried.isEmpty()){var offered=cargo.handler.getStackInSlot(index);setCarried(cargo.handler.extractItem(index,button==1?(offered.getCount()+1)/2:64,false));}else{int amount=button==1?1:carried.getCount();var offered=carried.copyWithCount(amount);var rest=cargo.handler.insertItem(index,offered,false);carried.shrink(amount-rest.getCount());setCarried(carried);}}broadcastChanges();return;}super.clicked(index,button,type,p);}
    @Override public boolean canTakeItemForPickAll(ItemStack stack,Slot slot){return slot.container!=view&&super.canTakeItemForPickAll(stack,slot);}
    @Override public ItemStack quickMoveStack(Player p,int index){if(!stillValid(p)||cargo==null||index<0||index>=slots.size())return ItemStack.EMPTY;if(index<16){var offered=cargo.handler.extractItem(index,64,true);var original=offered.copy();if(offered.isEmpty()||!moveItemStackTo(offered,16,slots.size(),true))return ItemStack.EMPTY;cargo.handler.extractItem(index,original.getCount()-offered.getCount(),false);refresh();return original;}var slot=slots.get(index);var stack=slot.getItem();var original=stack.copy();for(int i=0;i<16&&!stack.isEmpty();i++){var remaining=cargo.handler.insertItem(i,stack,false);stack.setCount(remaining.getCount());}if(stack.getCount()==original.getCount())return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);refresh();return original;}
}
