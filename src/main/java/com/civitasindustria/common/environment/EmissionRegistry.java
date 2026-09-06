package com.civitasindustria.common.environment;
import com.civitasindustria.api.environment.EmissionProfile;
import com.civitasindustria.domain.Pollutant;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;
public final class EmissionRegistry extends SimpleJsonResourceReloadListener {
    public static final EmissionRegistry INSTANCE=new EmissionRegistry();
    private Map<ResourceLocation,EmissionProfile> profiles=Map.of();
    private long generation;
    private EmissionRegistry(){super(new Gson(),"environment/emissions");}
    @Override protected void apply(Map<ResourceLocation,JsonElement> resources,ResourceManager manager,ProfilerFiller profiler){
        Map<ResourceLocation,EmissionProfile> next=new HashMap<>();
        if(resources.size()>10000)throw new IllegalArgumentException("Too many emission profiles");
        for(var entry:resources.entrySet()){
            JsonObject json=entry.getValue().getAsJsonObject();
            String target=json.get("target").getAsString();
            EnumMap<Pollutant,Double> emissions=new EnumMap<>(Pollutant.class);
            if(json.has("emissions"))for(var e:json.getAsJsonObject("emissions").entrySet()){
                Pollutant p=switch(e.getKey()){
                    case "particulate" -> Pollutant.PM;case "sulfur_oxides" -> Pollutant.SOX;case "nitrogen_oxides" -> Pollutant.NOX;
                    case "toxic_gas" -> Pollutant.TOX;default -> Pollutant.valueOf(e.getKey().toUpperCase(Locale.ROOT));
                };emissions.put(p,e.getValue().getAsDouble());
            }
            EmissionProfile profile=new EmissionProfile(target,json.get("industrial_load").getAsDouble(),emissions,
                json.has("active_property")?json.get("active_property").getAsString():"");
            if(next.put(ResourceLocation.parse(target),profile)!=null)throw new IllegalArgumentException("Duplicate emission target "+target);
        }
        profiles=Map.copyOf(next);generation++;
    }
    public EmissionProfile get(ResourceLocation id){return profiles.get(id);}
    public long generation(){return generation;}
}
