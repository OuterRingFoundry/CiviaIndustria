package com.civitasindustria.compat.create;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.common.warehouse.CargoBlockEntity;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
public final class MountedCargoChecks {
    public static void assembly(GameTestHelper h) throws Exception {
        var level=h.getLevel();var anchor=h.absolutePos(new BlockPos(2,1,2));var cargoPos=anchor.above();
        var assembler=net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse("create:cart_assembler"));
        level.setBlock(anchor.below(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
        level.setBlock(anchor,assembler.defaultBlockState(),3);level.setBlock(cargoPos,CivitasRegistries.CONTENT.get("cargo_crate").get().defaultBlockState(),3);
        var cargo=(CargoBlockEntity)level.getBlockEntity(cargoPos);String[] keys=new String[16];java.util.Arrays.fill(keys,"");long[] counts=new long[16];keys[0]="minecraft:iron_ingot";counts[0]=5_000_000_000L;cargo.restoreMounted(keys,counts);
        var contraption=new com.simibubi.create.content.contraptions.mounted.MountedContraption();
        if(!contraption.assemble(level,anchor))throw new AssertionError("Actual Create assembly failed");
        contraption.getStorage().initialize();
        if(contraption.getStorage().getAllItemStorages().values().stream().noneMatch(s->s instanceof MountedCargo))throw new AssertionError("Create used fallback storage");
        contraption.removeBlocksFromWorld(level,BlockPos.ZERO);
        if(level.getBlockEntity(cargoPos)!=null)throw new AssertionError("Source retained second authority");
        var encoded=contraption.writeNBT(level.registryAccess(),false);
        var resumed=com.simibubi.create.content.contraptions.Contraption.fromNBT(level,encoded,false);
        BlockPos destination=anchor.offset(2048,0,0);level.getChunkAt(destination);
        try{
            resumed.addBlocksToWorld(level,new com.simibubi.create.content.contraptions.StructureTransform(destination,0,0,0));
            if(!(level.getBlockEntity(destination.above()) instanceof CargoBlockEntity restored)||restored.total()!=counts[0])throw new AssertionError("Actual disassembly/reload lost long cargo");
        }finally{level.setBlock(destination.above(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);level.setBlock(destination,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);}
    }
    public static void run(GameTestHelper h){
        BlockPos source=new BlockPos(1,1,1),destination=new BlockPos(4,1,1);var block=CivitasRegistries.CONTENT.get("cargo_crate").get();h.setBlock(source,block);h.setBlock(destination,block);
        var original=(CargoBlockEntity)h.getBlockEntity(source);var result=(CargoBlockEntity)h.getBlockEntity(destination);
        String[] keys=new String[16];java.util.Arrays.fill(keys,"");long[] counts=new long[16];keys[0]="minecraft:iron_ingot";counts[0]=5_000_000_000L;original.restoreMounted(keys,counts);
        var type=CreateCargo.TYPE.get();var mounted=type.mount(h.getLevel(),original.getBlockState(),original.getBlockPos(),original);
        if(mounted.total()!=counts[0])throw new AssertionError("Mounted count truncated");
        var preview=mounted.extractItem(0,64,true);if(preview.getCount()!=64||mounted.total()!=counts[0])throw new AssertionError("Mounted simulation mutated");
        var taken=mounted.extractItem(0,64,false);if(!taken.is(Items.IRON_INGOT)||mounted.total()!=counts[0]-64)throw new AssertionError("Mounted extraction");
        var encoded=MountedCargo.CODEC.codec().encodeStart(JsonOps.INSTANCE,mounted).getOrThrow();
        var restored=MountedCargo.CODEC.codec().parse(JsonOps.INSTANCE,encoded).getOrThrow();
        restored.unmount(h.getLevel(),result.getBlockState(),result.getBlockPos(),result);
        if(result.total()+taken.getCount()!=counts[0])throw new AssertionError("Transit codec/disassembly conservation");
        if(!com.simibubi.create.api.contraption.BlockMovementChecks.isMovementAllowed(original.getBlockState(),h.getLevel(),original.getBlockPos()))throw new AssertionError("Crate assembly blocked");
        // Partial restore into the same block is replacement, not additive replay.
        restored.unmount(h.getLevel(),result.getBlockState(),result.getBlockPos(),result);if(result.total()!=counts[0]-64)throw new AssertionError("Repeated restore duplicated");
    }
}
