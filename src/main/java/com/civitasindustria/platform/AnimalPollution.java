package com.civitasindustria.platform;

import com.civitasindustria.common.config.ServerConfig;
import com.civitasindustria.domain.CellPos;
import com.civitasindustria.domain.PollutionEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Called only by a ticking animal, at most once per five seconds. No entity/world scans. */
public final class AnimalPollution {
    public static final ResourceLocation HEALTH = ResourceLocation.parse("civitas_industria:pollution_health");
    private AnimalPollution() {}
    public static double refresh(Animal animal) {
        if (!(animal.level() instanceof ServerLevel level)) return 1;
        var pos = animal.blockPosition();
        var cell = WorldRuntime.get(level).state().cells.get(CellPos.fromBlock(pos.getX(), pos.getZ()));
        double severity = PollutionEffects.severity(cell);
        double factor = ServerConfig.ANIMAL_EFFECTS.get()
            ? PollutionEffects.rate(severity, ServerConfig.ANIMAL_MIN_HEALTH.get()) : 1;
        // Quantization limits attribute sync traffic; this modifier never edits the base stat.
        double amount = Math.round((factor - 1) * 100) / 100.0;
        var health = animal.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            var existing = health.getModifier(HEALTH);
            if (amount == 0) { if (existing != null) health.removeModifier(HEALTH); }
            else if (existing == null || existing.amount() != amount) {
                health.removeModifier(HEALTH);
                health.addTransientModifier(new AttributeModifier(HEALTH, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            if (animal.getHealth() > animal.getMaxHealth()) animal.setHealth(animal.getMaxHealth());
        }
        // Recovery restores capacity, never free healing. Native vanilla age remains the saved authority.
        return ServerConfig.ANIMAL_EFFECTS.get()
            ? PollutionEffects.rate(severity, ServerConfig.ANIMAL_MIN_GROWTH.get()) : 1;
    }
}
