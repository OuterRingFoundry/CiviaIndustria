package com.civitasindustria.common.economy;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.*;
import com.civitasindustria.platform.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
public final class MarketMenu extends AbstractContainerMenu {
    private final BlockPos pos;private final Player owner;
    public final ContainerData data=new SimpleContainerData(20);
    public MarketMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf){this(id,inv,buf.readBlockPos());}
    public MarketMenu(int id,Inventory inv,BlockPos pos){super(CivitasRegistries.MARKET_MENU.get(),id);this.pos=pos.immutable();owner=inv.player;for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inv,x+y*9+9,8+x*18,150+y*18));for(int x=0;x<9;x++)addSlot(new Slot(inv,x,8+x*18,208));addDataSlots(data);refresh();}
    public static boolean mayUse(Player p,BlockPos pos){return p.level().hasChunkAt(pos)&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockState(pos).is(CivitasRegistries.CONTENT.get("market_counter").get())&&(!(p instanceof ServerPlayer sp)||ParcelProtection.allows(sp.serverLevel(),pos,p,Parcel.Flag.CONTAINER)&&ParcelProtection.allows(sp.serverLevel(),pos,p,Parcel.Flag.INTERACT));}
    @Override public boolean stillValid(Player p){return p==owner&&(p.level().isClientSide||mayUse(p,pos));}
    public int number(int field){return (data.get(field*2)&65535)|((data.get(field*2+1)&65535)<<16);}
    private void number(int field,long value){data.set(field*2,(int)value&65535);data.set(field*2+1,(int)(value>>>16)&65535);}
    private void refresh(){if(!(owner instanceof ServerPlayer p))return;var l=EconomySavedData.get(p.serverLevel()).registry;int good=Math.clamp(data.get(18),0,3);number(0,l.balance(p.getUUID()));number(1,l.reserve());number(2,l.treasury());number(3,l.index());number(4,l.stock(good));number(5,l.ask(good));number(6,l.bid(good));}
    @Override public void broadcastChanges(){refresh();super.broadcastChanges();}
    public static Item good(int i){return BuiltInRegistries.ITEM.get(ResourceLocation.parse(CrownLedger.GOODS[i]));}
    private static int find(Inventory inv,Item item){for(int i=0;i<36;i++)if(inv.getItem(i).is(item)&&inv.getItem(i).getComponentsPatch().isEmpty())return i;return -1;}
    private static int space(Inventory inv,Item item){ItemStack offered=new ItemStack(item);for(int i=0;i<36;i++){var s=inv.getItem(i);if(s.isEmpty()||(ItemStack.isSameItemSameComponents(s,offered)&&s.getCount()<s.getMaxStackSize()))return i;}return -1;}
    private static void give(Inventory inv,int slot,Item item){var s=inv.getItem(slot);if(s.isEmpty())inv.setItem(slot,new ItemStack(item));else s.grow(1);inv.setChanged();}
    @Override public boolean clickMenuButton(Player actor,int action){if(!(actor instanceof ServerPlayer p)||!stillValid(p))return false;if(action>=10&&action<14){data.set(18,action-10);refresh();return true;}var saved=EconomySavedData.get(p.serverLevel());var l=saved.registry;Inventory inv=p.getInventory();int good=data.get(18);boolean ok=false;int slot;
        switch(action){
            case 0 -> {slot=find(inv,Items.DIAMOND);if(slot>=0&&l.deposit(p.getUUID())){inv.getItem(slot).shrink(1);ok=true;}}
            case 1 -> {slot=space(inv,Items.DIAMOND);if(slot>=0&&l.redeem(p.getUUID())){give(inv,slot,Items.DIAMOND);ok=true;}}
            case 2 -> {slot=space(inv,good(good));if(slot>=0&&l.buy(p.getUUID(),good)){give(inv,slot,good(good));ok=true;}}
            case 3 -> {slot=find(inv,good(good));if(slot>=0&&l.sell(p.getUUID(),good)){inv.getItem(slot).shrink(1);ok=true;}}
            case 4 -> ok=l.fund(p.getUUID());
            default -> {return false;}
        }
        if(ok){saved.setDirty();inv.setChanged();}else p.displayClientMessage(Component.literal("Trade unavailable: check balance, market funds/stock and inventory space."),true);refresh();broadcastChanges();return ok;
    }
    @Override public void clicked(int slot,int button,ClickType type,Player p){if(stillValid(p))super.clicked(slot,button,type,p);}
    @Override public ItemStack quickMoveStack(Player p,int i){if(!stillValid(p)||i<0||i>=36)return ItemStack.EMPTY;var slot=slots.get(i);var stack=slot.getItem();var copy=stack.copy();if(stack.isEmpty()||!(i<27?moveItemStackTo(stack,27,36,false):moveItemStackTo(stack,0,27,false)))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return copy;}
}
