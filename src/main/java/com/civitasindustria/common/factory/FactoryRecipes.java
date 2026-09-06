package com.civitasindustria.common.factory;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;
public final class FactoryRecipes extends SimpleJsonResourceReloadListener {
    public record Recipe(ResourceLocation id,ResourceLocation input,ResourceLocation output,int count,int duration,int fuel){}
    public static final FactoryRecipes INSTANCE=new FactoryRecipes();
    private Map<ResourceLocation,Recipe> byInput=Map.of();
    private FactoryRecipes(){super(new Gson(),"factory/recipes");}
    @Override protected void apply(Map<ResourceLocation,JsonElement> resources,ResourceManager manager,ProfilerFiller profiler){
        if(resources.size()>1024)throw new IllegalArgumentException("Factory recipe limit");
        Map<ResourceLocation,Recipe> next=new HashMap<>();
        resources.forEach((id,value)->{
            var j=value.getAsJsonObject();var r=new Recipe(id,ResourceLocation.parse(j.get("input").getAsString()),ResourceLocation.parse(j.get("output").getAsString()),j.get("batch").getAsInt(),j.get("duration").getAsInt(),j.get("fuel").getAsInt());
            if(r.count()<1||r.count()>64||r.duration()<20||r.duration()>72000||r.fuel()<1||r.fuel()>64||next.put(r.input(),r)!=null)throw new IllegalArgumentException("Invalid/duplicate factory recipe "+id);
        });byInput=Map.copyOf(next);
    }
    public Recipe find(ResourceLocation item){return byInput.get(item);}
}
