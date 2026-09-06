package com.civitasindustria.mixin;
import com.civitasindustria.common.cargo.Encumbrance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge has no cancellable menu-slot click event; reject only routes depositing bulk into Ender storage. */
@Mixin(AbstractContainerMenu.class)
public abstract class EnderCargoMixin {
    @Inject(method="clicked",at=@At("HEAD"),cancellable=true)
    private void ci$protectEnder(int slot,int button,ClickType type,Player player,CallbackInfo callback){
        AbstractContainerMenu menu=(AbstractContainerMenu)(Object)this;
        if(!(menu instanceof ChestMenu chest)||chest.getContainer()!=player.getEnderChestInventory())return;
        int enderSlots=chest.getContainer().getContainerSize();
        boolean deny=false;
        if(type==ClickType.QUICK_MOVE&&slot>=enderSlots&&slot<menu.slots.size())deny=Encumbrance.mass(menu.getSlot(slot).getItem())>0;
        if(slot>=0&&slot<enderSlots){
            if(type==ClickType.PICKUP||type==ClickType.QUICK_CRAFT)deny=Encumbrance.mass(menu.getCarried())>0;
            if(type==ClickType.SWAP&&button>=0&&button<player.getInventory().getContainerSize())deny=Encumbrance.mass(player.getInventory().getItem(button))>0;
        }
        if(deny){callback.cancel();menu.broadcastChanges();}
    }
}
