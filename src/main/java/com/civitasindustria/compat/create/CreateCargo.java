package com.civitasindustria.compat.create;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.warehouse.CargoBlockEntity;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
public final class CreateCargo {
    private static final DeferredRegister<MountedItemStorageType<?>> TYPES=DeferredRegister.create(CreateBuiltInRegistries.MOUNTED_ITEM_STORAGE_TYPE.key(),"civitas_industria");
    public static final DeferredHolder<MountedItemStorageType<?>,MountedCargo.Type> TYPE=TYPES.register("cargo",MountedCargo.Type::new);
    public static void register(IEventBus bus){TYPES.register(bus);bus.addListener(CreateCargo::setup);}
    private static boolean mobile(net.minecraft.world.level.block.state.BlockState state){return state.is(CivitasRegistries.CONTENT.get("cargo_crate").get())||state.is(CivitasRegistries.CONTENT.get("pallet").get());}
    private static void setup(FMLCommonSetupEvent event){event.enqueueWork(()->{
        for(String name:new String[]{"cargo_crate","pallet"})MountedItemStorageType.REGISTRY.register(CivitasRegistries.CONTENT.get(name).get(),TYPE.get());
        BlockMovementChecks.registerMovementAllowedCheck((state,level,pos)->{
            if(mobile(state))return BlockMovementChecks.CheckResult.of(level.getBlockEntity(pos) instanceof CargoBlockEntity cargo&&!cargo.isQuarantined());
            if(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("civitas_industria")&&level.getBlockEntity(pos)!=null)return BlockMovementChecks.CheckResult.FAIL;
            return BlockMovementChecks.CheckResult.PASS;
        });
    });}
}
