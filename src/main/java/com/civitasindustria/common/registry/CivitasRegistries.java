package com.civitasindustria.common.registry;
import com.civitasindustria.CivitasIndustria;
import com.civitasindustria.common.civilization.CivicBlock;
import com.civitasindustria.common.warehouse.*;
import com.civitasindustria.domain.WorldState;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import java.util.*;

public final class CivitasRegistries {
    public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(CivitasIndustria.MOD_ID);
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(CivitasIndustria.MOD_ID);
    public static final DeferredItem<Item> CALIBRATION_KIT=ITEMS.registerSimpleItem("calibration_kit");
    public static final DeferredItem<Item> PRECISION_COMPONENT=ITEMS.registerSimpleItem("precision_component");
    public static final DeferredItem<Item> REMEDIATION_REAGENT=ITEMS.registerSimpleItem("remediation_reagent");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,CivitasIndustria.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(Registries.ENTITY_TYPE,CivitasIndustria.MOD_ID);
    public static final DeferredHolder<EntityType<?>,EntityType<com.civitasindustria.common.threat.IndustrialRaider>> RAIDER=ENTITIES.register("industrial_raider",
        ()->EntityType.Builder.of(com.civitasindustria.common.threat.IndustrialRaider::new,net.minecraft.world.entity.MobCategory.MONSTER).sized(.6f,1.95f).clientTrackingRange(8).build("civitas_industria:industrial_raider"));
    private static void attributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event){event.put(RAIDER.get(),net.minecraft.world.entity.monster.Zombie.createAttributes().build());}
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,CivitasIndustria.MOD_ID);
    public static final Map<String,DeferredBlock<? extends Block>> CONTENT=new LinkedHashMap<>();
    private static BlockBehaviour.Properties metal(){return BlockBehaviour.Properties.of().strength(3,6).sound(SoundType.METAL);}
    static {
        for(var kind:WorldState.CivicNode.Kind.values()){
            String id=switch(kind){case CORE->"civic_core";case RELAY->"civic_relay";case LOGISTICS->"logistics_node";case DEFENSE->"defense_node";case MAINTENANCE->"maintenance_depot";};
            CONTENT.put(id,BLOCKS.register(id,()->new CivicBlock(metal(),kind)));
        }
        for(String id:List.of("cargo_crate","pallet","warehouse_controller","warehouse_port","cargo_loader","cargo_unloader","freight_terminal"))
            CONTENT.put(id,BLOCKS.register(id,()->id.equals("cargo_loader")||id.equals("cargo_unloader")||id.equals("freight_terminal")?
                new FreightBlock(metal().explosionResistance(3600000).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)):
                new StorageBlock(metal().explosionResistance(3600000).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK))));
        CONTENT.put("remediation_station",BLOCKS.register("remediation_station",()->new com.civitasindustria.common.environment.RemediationBlock(metal())));
        CONTENT.put("factory_controller",BLOCKS.register("factory_controller",()->new com.civitasindustria.common.factory.FactoryBlock(metal().explosionResistance(3600000).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK))));
        CONTENT.put("bulk_tank",BLOCKS.register("bulk_tank",()->new BulkTankBlock(metal().explosionResistance(3600000).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK))));
        CONTENT.put("warehouse_casing",BLOCKS.register("warehouse_casing",()->new WarehouseCasingBlock(metal())));
        for(String id:List.of("decorative_gear","decorative_fan","decorative_pump","decorative_gauge","decorative_piston","decorative_vent"))
            CONTENT.put(id,BLOCKS.register(id,()->new com.civitasindustria.common.decoration.DecorativeBlock(metal())));
        for(var entry:CONTENT.entrySet())ITEMS.register(entry.getKey(),()->new BlockItem(entry.getValue().get(),new Item.Properties().stacksTo(entry.getKey().equals("cargo_crate")?1:64)));
    }
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<CargoBlockEntity>> CARGO_ENTITY=BLOCK_ENTITIES.register("cargo",
        ()->BlockEntityType.Builder.of(CargoBlockEntity::new,CONTENT.values().stream().map(DeferredBlock::get).filter(b->b instanceof StorageBlock).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BulkTankBlockEntity>> TANK_ENTITY=BLOCK_ENTITIES.register("bulk_tank",
        ()->BlockEntityType.Builder.of(BulkTankBlockEntity::new,CONTENT.get("bulk_tank").get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<com.civitasindustria.common.factory.FactoryBlockEntity>> FACTORY_ENTITY=BLOCK_ENTITIES.register("factory_controller",
        ()->BlockEntityType.Builder.of(com.civitasindustria.common.factory.FactoryBlockEntity::new,CONTENT.get("factory_controller").get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<com.civitasindustria.common.decoration.DecorativeEntity>> DECORATIVE_ENTITY=BLOCK_ENTITIES.register("decoration",
        ()->BlockEntityType.Builder.of(com.civitasindustria.common.decoration.DecorativeEntity::new,CONTENT.values().stream().map(DeferredBlock::get).filter(b->b instanceof com.civitasindustria.common.decoration.DecorativeBlock).toArray(Block[]::new)).build(null));
    public static void capabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event){
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,FACTORY_ENTITY.get(),(factory,side)->factory.inventory);
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,TANK_ENTITY.get(),(tank,side)->tank);
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,CARGO_ENTITY.get(),(cargo,side)->cargo.handler);
    }
    public static void register(IEventBus bus){bus.addListener(CivitasRegistries::capabilities);bus.addListener(CivitasRegistries::attributes);BLOCKS.register(bus);ITEMS.register(bus);BLOCK_ENTITIES.register(bus);ENTITIES.register(bus);MENUS.register(bus);}
}
