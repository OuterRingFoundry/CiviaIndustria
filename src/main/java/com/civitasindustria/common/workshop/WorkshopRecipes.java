package com.civitasindustria.common.workshop;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import java.util.*;
/** Small reloadable operation table, atomically replaced on successful reload. */
public final class WorkshopRecipes extends SimpleJsonResourceReloadListener {
    public record Recipe(int mode,TagKey<Item> input,ResourceLocation output,int ticks,int energy){}
    public static final WorkshopRecipes INSTANCE=new WorkshopRecipes();
    private Map<Integer,Recipe> recipes=Map.of();
    private WorkshopRecipes(){super(new Gson(),"workshop/recipes");}
    @Override protected void apply(Map<ResourceLocation,JsonElement> resources,ResourceManager manager,ProfilerFiller profiler){
        if(resources.size()>3)throw new IllegalArgumentException("At most three workshop operations");
        Map<Integer,Recipe> next=new HashMap<>();
        resources.forEach((id,value)->{var j=value.getAsJsonObject();int mode=j.get("mode").getAsInt();
            var r=new Recipe(mode,TagKey.create(Registries.ITEM,ResourceLocation.parse(j.get("input_tag").getAsString())),ResourceLocation.parse(j.get("output").getAsString()),j.get("ticks").getAsInt(),j.get("energy_per_tick").getAsInt());
            if(mode<0||mode>2||r.ticks()<20||r.ticks()>32000||r.energy()<1||r.energy()>4096||next.put(mode,r)!=null)throw new IllegalArgumentException("Invalid workshop operation "+id);
        });recipes=Map.copyOf(next);
    }
    public Recipe get(int mode){return recipes.get(mode);}
    public boolean accepts(ItemStack stack){return stack.getComponentsPatch().isEmpty()&&recipes.values().stream().anyMatch(r->stack.is(r.input()));}
}
