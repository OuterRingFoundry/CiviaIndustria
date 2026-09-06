package com.civitasindustria.common.warehouse;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class WarehouseStructure {
    private WarehouseStructure() {}
    /** Two blocks covers diagonal ring members and neighboring port capability caches. */
    public static void changed(Level level,BlockPos changed) {
        if(level.isClientSide)return;
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){
            BlockPos pos=changed.offset(x,0,z);
            if(level.hasChunkAt(pos)&&level.getBlockEntity(pos) instanceof CargoBlockEntity cargo)cargo.invalidateStructure();
        }
    }
}
