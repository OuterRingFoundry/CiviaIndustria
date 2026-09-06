package com.civitasindustria.common.registry;

import com.civitasindustria.CivitasIndustria;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Empty registration roots. Content is introduced only in its approved phase. */
public final class CivitasRegistries {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CivitasIndustria.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CivitasIndustria.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CivitasIndustria.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CivitasIndustria.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CivitasIndustria.MOD_ID);

    private CivitasRegistries() {}
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ENTITIES.register(bus);
        MENUS.register(bus);
    }
}
