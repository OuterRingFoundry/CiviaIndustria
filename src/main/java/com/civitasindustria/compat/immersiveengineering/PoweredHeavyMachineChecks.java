package com.civitasindustria.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.civitasindustria.common.factory.MachineCommissioning;
import com.civitasindustria.common.registry.CivitasRegistries;
import com.civitasindustria.domain.Commissioning;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

/** Opt-in GameTest fixtures: native formation and ports, loaded-time production and paid repair. */
public final class PoweredHeavyMachineChecks {
    private PoweredHeavyMachineChecks() {}

    public static void run(GameTestHelper h, boolean diesel) {
        var level = h.getLevel();
        var origin = h.absolutePos(new BlockPos(2, 3, 2));
        var template = diesel ? IEMultiblocks.DIESEL_GENERATOR : IEMultiblocks.ARC_FURNACE;
        for (var info : template.getStructure(level)) level.setBlock(origin.offset(info.pos()), info.state(), 3);
        check(template.createStructure(level, origin.offset(template.getTriggerOffset()), Direction.SOUTH,
            net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level)), "Native formation refused");
        var master = (MultiblockBlockEntityMaster<?>) level.getBlockEntity(origin.offset(template.getMasterFromOriginOffset()));
        check(master != null, "Missing master");
        var helper = master.getHelper();
        var context = helper.getContext();
        var size = template.getSize(level);
        for (int x = 0; x < size.getX(); x++) for (int z = 0; z < size.getZ(); z++)
            level.setBlock(context.getLevel().toAbsolute(new BlockPos(x, -1, z)), Blocks.IRON_BLOCK.defaultBlockState(), 3);

        // This is IE's real capacitor and generator output position, not a mocked energy receiver.
        var receiverPos = context.getLevel().toAbsolute(new BlockPos(1, 2, 4));
        if (diesel) {
            level.setBlock(receiverPos, IEBlocks.MetalDevices.CAPACITOR_HV.get().defaultBlockState(), 3);
            var capacitor = (CapacitorBlockEntity) level.getBlockEntity(receiverPos);
            for (var direction : Direction.values()) capacitor.sideConfig.put(direction, IOSideConfig.NONE);
            capacitor.sideConfig.put(Direction.DOWN, IOSideConfig.INPUT);
            level.invalidateCapabilities(receiverPos);
        } else {
            var state = (ArcFurnaceLogic.State) helper.getState();
            // Electrodes are installed through the inventory used by IE's menu.
            for (int i = 0; i < ArcFurnaceLogic.ELECTRODE_COUNT; i++)
                state.inventory.setStackInSlot(ArcFurnaceLogic.FIRST_ELECTRODE_SLOT + i, new ItemStack(IEItems.Misc.GRAPHITE_ELECTRODE.get()));
        }
        boolean supplied = false, powered = diesel;
        var energyPort = new BlockPos[1]; var energySide = new Direction[1]; int[] suppliedEnergy = {0};
        for (int x = 0; x < size.getX(); x++) for (int y = 0; y < size.getY(); y++) for (int z = 0; z < size.getZ(); z++) {
            var pos = context.getLevel().toAbsolute(new BlockPos(x, y, z));
            for (var side : Direction.values()) {
                if (diesel && !supplied) {
                    var fluid = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
                    var fuel = new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("immersiveengineering:biodiesel")), 24000);
                    if (fluid != null && fluid.fill(fuel, FluidAction.SIMULATE) == 24000) {
                        check(fluid.fill(fuel, FluidAction.EXECUTE) == 24000, "Fuel simulation disagreed"); supplied = true;
                    }
                } else if (!diesel) {
                    var energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
                    if (!powered && energy != null) {
                        int accepted = energy.receiveEnergy(ArcFurnaceLogic.ENERGY_CAPACITY, false);
                        if (accepted > 0) { powered = true; energyPort[0] = pos; energySide[0] = side; suppliedEnergy[0] = accepted; }
                    }
                    var items = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
                    if (!supplied && items != null) for (int slot = 0; slot < items.getSlots(); slot++)
                        if (items.insertItem(slot, new ItemStack(Items.IRON_ORE), true).isEmpty()) {
                            check(items.insertItem(slot, new ItemStack(Items.IRON_ORE), false).isEmpty(), "Input simulation disagreed");
                            supplied = true; break;
                        }
                }
            }
        }
        check(supplied && powered, "Missing native fuel/input/energy ports");
        var before = processing(master);
        for (int i = 0; i < 40; i++) helper.tickServer();
        check(before.equals(processing(master)), "Uncommissioned machine consumed resources");
        if (diesel) check(storedEnergy(h, receiverPos) == 0, "Uncommissioned generator produced energy");
        calibrate(master);

        var brokenPad = context.getLevel().toAbsolute(new BlockPos(0, -1, 0));
        h.runAfterDelay(250, () -> {
            check(MachineCommissioning.stage(master) == Commissioning.Stage.READY, "Calibration did not finish naturally");
            if (diesel) {
                var state = (DieselGeneratorLogic.State) helper.getState();
                check(state.isActive() && state.tank.getFluidAmount() < 24000 && storedEnergy(h, receiverPos) > 0, "Diesel did not turn fuel into capacitor energy");
            } else {
                var state = (ArcFurnaceLogic.State) helper.getState();
                check(state.isClientActive() && !state.getProcessQueue().isEmpty() && state.getEnergy().getEnergyStored() < suppliedEnergy[0], "Arc did not start its powered recipe");
            }
            level.setBlock(brokenPad, Blocks.AIR.defaultBlockState(), 3);
        });
        h.runAfterDelay(280, () -> {
            check(MachineCommissioning.stage(master) == Commissioning.Stage.DEGRADED, "Broken foundation did not suspend production");
            check(diesel ? !((DieselGeneratorLogic.State) helper.getState()).isActive() : !((ArcFurnaceLogic.State) helper.getState()).isClientActive(), "Suspended activity remained visible");
            var paused = processing(master);
            int energy = diesel ? storedEnergy(h, receiverPos) : 0;
            // Round-trip the actual owning BE while its partially processed recipe/fuel is suspended.
            var saved = master.saveWithFullMetadata(level.registryAccess());
            master.loadWithComponents(saved, level.registryAccess());
            check(paused.equals(processing(master)), "Paused processing changed on BE reload");
            h.runAfterDelay(40, () -> {
                check(paused.equals(processing(master)), "Degraded machine continued processing");
                if (diesel) check(storedEnergy(h, receiverPos) == energy, "Degraded generator still supplied energy");
                level.setBlock(brokenPad, Blocks.IRON_BLOCK.defaultBlockState(), 3);
                calibrate(master);
            });
        });
        h.runAfterDelay(600, () -> {
            if (!diesel) {
                var energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, energyPort[0], energySide[0]);
                check(energy != null, "Arc energy port lost after reload");
                suppliedEnergy[0] += energy.receiveEnergy(ArcFurnaceLogic.ENERGY_CAPACITY, false);
            }
        });
        h.runAfterDelay(850, () -> {
            check(MachineCommissioning.canOperate(master), "Paid repair did not restore production");
            if (diesel) {
                var state = (DieselGeneratorLogic.State) helper.getState();
                check(state.isActive() && state.tank.getFluidAmount() > 0 && state.tank.getFluidAmount() < 23000 && storedEnergy(h, receiverPos) > 1000000,
                    "Generator failed to resume after repair");
            } else {
                var state = (ArcFurnaceLogic.State) helper.getState();
                int ingots = 0, ore = 0, slag = 0;
                var slagItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse("immersiveengineering:slag"));
                for (int i = 0; i < state.inventory.getSlots(); i++) {
                    var stack = state.inventory.getStackInSlot(i);
                    if (stack.is(Items.IRON_INGOT)) ingots += stack.getCount();
                    if (stack.is(Items.IRON_ORE)) ore += stack.getCount();
                    if (stack.is(slagItem)) slag += stack.getCount();
                }
                check(ingots == 2 && ore == 0 && slag == 1 && state.getProcessQueue().isEmpty(),
                    "Arc output conservation failed: ingots=" + ingots + " ore=" + ore + " slag=" + slag + " state=" + processing(master));
                check(suppliedEnergy[0] - state.getEnergy().getEnergyStored() == 102400, "Arc recipe energy accounting changed");
                for (int i = 0; i < ArcFurnaceLogic.ELECTRODE_COUNT; i++)
                    check(state.inventory.getStackInSlot(ArcFurnaceLogic.FIRST_ELECTRODE_SLOT + i).getDamageValue() > 0, "Powered production did not wear electrodes");
            }
            h.succeed();
        });
    }

    private static CompoundTag processing(MultiblockBlockEntityMaster<?> master) {
        var tag = new CompoundTag(); var state = master.getHelper().getState();
        if (state instanceof ArcFurnaceLogic.State arc) arc.writeSaveNBT(tag, master.getLevel().registryAccess());
        else ((DieselGeneratorLogic.State) state).writeSaveNBT(tag, master.getLevel().registryAccess());
        return tag;
    }
    private static int storedEnergy(GameTestHelper h, BlockPos pos) {
        var energy = h.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, pos, Direction.DOWN);
        check(energy != null, "Missing capacitor energy capability"); return energy.getEnergyStored();
    }
    private static void calibrate(MultiblockBlockEntityMaster<?> master) {
        var kit = new ItemStack(CivitasRegistries.CALIBRATION_KIT.get());
        check(MachineCommissioning.begin(master, kit) && kit.isEmpty(), "Calibration/repair must consume one kit");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
