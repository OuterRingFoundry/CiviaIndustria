package com.civitasindustria.platform;

import com.civitasindustria.CivitasIndustria;
import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.CellPos;
import com.civitasindustria.domain.Pollutant;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.biome.Biome;

/** Loaded-player exposure only; no world scan or client-class references. */
public final class GameplayHooks {
    public static final TagKey<Biome> HIGH_RECOVERY=biomeTag("high_ecological_recovery");
    public static final TagKey<Biome> RESISTANT=biomeTag("pollution_resistant");
    private GameplayHooks() {}
    private static TagKey<Biome> biomeTag(String path) {
        return TagKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath(CivitasIndustria.MOD_ID,path));
    }

    public static void environmentEffects(ServerLevel level,WorldRuntime runtime) {
        if(!ServerConfig.EXPOSURE.get())return;
        for(var player:level.players()) {
            if(player.isCreative()||player.isSpectator())continue;
            var pos=player.blockPosition();
            var cell=runtime.state().cells.get(CellPos.fromBlock(pos.getX(),pos.getZ()));
            if(cell==null)continue;
            boolean severeAir=cell.aqi()>=ServerConfig.EXPOSURE_AQI.get();
            boolean acidRain=level.isRainingAt(pos)&&cell.acidPrecursorLoad>=ServerConfig.EXPOSURE_ACID.get();
            boolean toxicWater=player.isInWater()&&cell.get(Pollutant.W_TOXICITY)>=ServerConfig.EXPOSURE_WATER.get();
            if(severeAir||acidRain||toxicWater)
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,120,0,false,false,true));
        }
    }

    public static void warn(ServerLevel level,CellPos cell) {
        for(var player:level.players())
            if(CellPos.fromBlock(player.blockPosition().getX(),player.blockPosition().getZ()).equals(cell))
                player.sendSystemMessage(Component.literal("Industrial threat warning: reduce emissions or restore shared defenses."));
    }
}
