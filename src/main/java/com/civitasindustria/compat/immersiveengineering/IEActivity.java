package com.civitasindustria.compat.immersiveengineering;
import net.minecraft.world.level.block.state.BlockState;
/** Only a public block-state activity property is accepted; decorative/dummy parts never emit by default. */
public final class IEActivity {
    public static boolean active(BlockState state){
        for(var p:state.getProperties())if(p.getName().equals("active"))return Boolean.TRUE.equals(state.getValue(p));
        return false;
    }
}
