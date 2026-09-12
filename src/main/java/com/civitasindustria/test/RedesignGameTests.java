package com.civitasindustria.test;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.economy.MarketMenu;
import com.civitasindustria.common.warehouse.*;
import com.civitasindustria.platform.*;
import com.civitasindustria.domain.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import com.mojang.authlib.GameProfile;
import java.util.*;
@GameTestHolder("civitas_industria") @PrefixGameTestTemplate(false)
public final class RedesignGameTests {
    @GameTest(template="empty")
    public static void diamondMarketAndBulkMenu(GameTestHelper h){
        BlockPos p=new BlockPos(1,2,1),absolute=h.absolutePos(p);h.setBlock(p,CivitasRegistries.CONTENT.get("market_counter").get());var player=FakePlayerFactory.get(h.getLevel(),new GameProfile(UUID.randomUUID(),"CIMarket"));player.setPos(absolute.getX()+.5,absolute.getY(),absolute.getZ()+.5);player.getInventory().clearContent();player.getInventory().setItem(0,new ItemStack(Items.DIAMOND,2));var menu=new MarketMenu(1,player.getInventory(),absolute);var saved=EconomySavedData.get(h.getLevel());long supply=saved.registry.issued();if(!menu.clickMenuButton(player,0)||saved.registry.balance(player.getUUID())!=100||player.getInventory().getItem(0).getCount()!=1)throw new AssertionError("Deposit inventory/ledger mismatch");if(!menu.clickMenuButton(player,1)||saved.registry.issued()!=supply||player.getInventory().getItem(0).getCount()!=2)throw new AssertionError("Redeem conservation");if(menu.clickMenuButton(player,99))throw new AssertionError("Invalid button accepted");
        // Full inventory refuses redemption without debiting the ledger.
        menu.clickMenuButton(player,0);for(int i=0;i<36;i++)player.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));if(menu.clickMenuButton(player,1)||saved.registry.balance(player.getUUID())!=100)throw new AssertionError("Full-inventory redemption lost currency");player.getInventory().clearContent();if(!menu.clickMenuButton(player,1))throw new AssertionError("Redeem recovery");
        BlockPos box=new BlockPos(3,2,1);h.setBlock(box,CivitasRegistries.CONTENT.get("cargo_crate").get());var cargo=(CargoBlockEntity)h.getBlockEntity(box);cargo.handler.insertItem(0,new ItemStack(Items.IRON_INGOT,64),false);var storage=new CargoMenu(2,player.getInventory(),cargo);storage.clicked(0,1,ClickType.PICKUP,player);if(storage.getCarried().getCount()!=32||cargo.total()!=32)throw new AssertionError("Right-click half transfer");storage.clicked(0,0,ClickType.PICKUP,player);if(!storage.getCarried().isEmpty()||cargo.total()!=64)throw new AssertionError("Carried deposit duplication");storage.quickMoveStack(player,0);if(cargo.total()!=0||player.getInventory().countItem(Items.IRON_INGOT)!=64)throw new AssertionError("Shift-click conservation");player.setPos(absolute.getX()+100,absolute.getY(),absolute.getZ());if(menu.stillValid(player)||menu.clickMenuButton(player,0)||storage.stillValid(player))throw new AssertionError("Remote access accepted");h.succeed();
    }
    @GameTest(template="empty")
    public static void civilRaidExemptionsAndNativeChimney(GameTestHelper h){
        if(net.neoforged.fml.ModList.get().isLoaded("civil"))com.civitasindustria.compat.civil.CivilChecks.verify();
        if(net.neoforged.fml.ModList.get().isLoaded("adchimneys"))com.civitasindustria.compat.chimneys.ChimneyChecks.verify(h);
        h.succeed();
    }
}
