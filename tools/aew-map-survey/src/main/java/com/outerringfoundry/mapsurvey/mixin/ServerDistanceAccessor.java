package com.outerringfoundry.mapsurvey.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read the server's advertised distance without changing it or its chunk cache. */
@Mixin(ClientPacketListener.class)
public interface ServerDistanceAccessor {
    @Accessor("serverChunkRadius") int aewSurvey$getServerDistance();
}
