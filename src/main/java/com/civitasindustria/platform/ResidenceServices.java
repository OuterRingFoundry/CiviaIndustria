package com.civitasindustria.platform;

import com.civitasindustria.domain.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

/** Online-only residence sampling. Service payments are added after authority validation. */
public final class ResidenceServices {
    private ResidenceServices() {}
    public static boolean authorized(ServerLevel context, ResidenceRegistry.Home home) {
        ServerLevel homeLevel = context.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.parse(home.dimension())));
        if (homeLevel == null) return false;
        Parcel parcel = WorldRuntime.get(homeLevel).state().parcels.at(home.x(), home.y(), home.z()).orElse(null);
        return home.authorized(parcel);
    }
    public static boolean qualifyingPresence(ServerPlayer player, ResidenceRegistry.Home home) {
        return player.isAlive() && player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                && player.serverLevel().dimension().location().toString().equals(home.dimension())
                && home.cell().equals(CellPos.fromBlock(player.blockPosition().getX(), player.blockPosition().getZ()))
                && authorized(player.serverLevel(), home);
    }
    public static void tick(ServerLevel overworld) {
        long now = overworld.getGameTime();
        if (now % ResidenceRegistry.SAMPLE_TICKS != 0) return;
        long start = System.nanoTime();
        ResidenceSavedData saved = ResidenceSavedData.get(overworld);
        for (ServerPlayer player : overworld.getServer().getPlayerList().getPlayers()) {
            var home = saved.registry.home(player.getUUID()).orElse(null);
            if (home != null && saved.registry.observe(player.getUUID(), now, qualifyingPresence(player, home))) saved.setDirty();
        }
        WorldRuntime.get(overworld).record("residence", start, overworld.getServer().getPlayerList().getPlayers().size());
    }
    public static String status(ServerPlayer player) {
        ResidenceSavedData saved = ResidenceSavedData.get(player.serverLevel());
        var home = saved.registry.home(player.getUUID()).orElse(null);
        if (home == null) return "No residence declared. Stand inside an owned or explicitly trusted parcel and use /ci residence declare.";
        String location = home.dimension() + " " + home.x() + ", " + home.y() + ", " + home.z();
        String reason;
        long now = player.getServer().overworld().getGameTime();
        if (!authorized(player.serverLevel(), home)) reason = "Parcel missing, home outside parcel, or ownership/trust revoked.";
        else if (home.lastActivity() > now) reason = "Game clock moved backwards; return home to qualify again.";
        else if (home.lastActivity() >= 0 && now - home.lastActivity() > ResidenceRegistry.GRACE_TICKS)
            reason = "Activity grace expired; return home for ten minutes in survival mode.";
        else if (!home.recentlyQualified(now)) reason = "Qualifying survival time in home district: "
                + home.qualifyingTicks() / 20 + "/600 seconds.";
        else reason = "Recent residence activity qualified.";
        if (!qualifyingPresence(player, home)) reason += " Activity is not currently accruing.";
        return "Home: " + location + " | " + reason + " | Civic services are not available yet.";
    }
}
