package com.civitasindustria.common.factory;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;
public final class CommissioningRequirements extends SimpleJsonResourceReloadListener {
    public record Rule(ResourceLocation foundationTag,int radius,ResourceLocation tool){}
    public static final CommissioningRequirements INSTANCE=new CommissioningRequirements();
    private Map<ResourceLocation,Rule> rules=Map.of();
    private CommissioningRequirements(){super(new Gson(),"commissioning");}
    @Override protected void apply(Map<ResourceLocation,JsonElement> input,ResourceManager manager,ProfilerFiller profiler){
        if(input.size()>128)throw new IllegalArgumentException("Commissioning profile limit");var next=new HashMap<ResourceLocation,Rule>();
        for(var value:input.values()){
            var j=value.getAsJsonObject();int radius=j.get("foundation_radius").getAsInt();if(radius<1||radius>3)throw new IllegalArgumentException("Foundation radius");
            var target=ResourceLocation.parse(j.get("target").getAsString());var rule=new Rule(ResourceLocation.parse(j.get("foundation_tag").getAsString()),radius,ResourceLocation.parse(j.get("calibration_item").getAsString()));
            if(next.put(target,rule)!=null)throw new IllegalArgumentException("Duplicate commissioning target");
        }
        rules=Map.copyOf(next);
    }
    public Rule get(ResourceLocation block){return rules.get(block);}
}
