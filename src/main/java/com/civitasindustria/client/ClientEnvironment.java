package com.civitasindustria.client;
import com.civitasindustria.common.network.EnvironmentPayload;
import com.civitasindustria.domain.CellPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.util.*;
@EventBusSubscriber(modid="civitas_industria",value=Dist.CLIENT)
public final class ClientEnvironment {
    private static volatile Map<CellPos,EnvironmentPayload.Sample> SAMPLES=Map.of();
    private static final java.util.LinkedHashSet<Long> DIRTY=new java.util.LinkedHashSet<>();
    private static ResourceLocation dimension;
    private static long received;
    private static float haze;
    private static boolean tintEnabled=true;
    public static void receive(EnvironmentPayload packet){
        var level=Minecraft.getInstance().level;if(level==null||!level.dimension().location().equals(packet.dimension()))return;
        dimension=packet.dimension();var previous=SAMPLES;var next=new HashMap<CellPos,EnvironmentPayload.Sample>();for(var s:packet.samples())next.put(new CellPos(s.x(),s.z()),s);SAMPLES=Map.copyOf(next);received=level.getGameTime();
        Set<CellPos> changed=new HashSet<>(previous.keySet());changed.addAll(next.keySet());
        changed.removeIf(p->{var old=previous.get(p);var value=next.get(p);return old!=null&&value!=null&&(int)(old.injury()*20)==(int)(value.injury()*20)&&(int)(old.water()*20)==(int)(value.water()*20);});
        invalidate(changed);
    }
    private static void invalidate(Collection<CellPos> cells){
        var level=Minecraft.getInstance().level;if(level==null||cells.isEmpty())return;
        level.clearTintCaches();
        // Bilinear samples also affect the adjoining half-cell; dirty the bounded halo.
        for(var cell:cells)for(int x=cell.x()*4-2;x<cell.x()*4+6;x++)for(int z=cell.z()*4-2;z<cell.z()*4+6;z++)if(level.hasChunk(x,z))
            for(int y=level.getMinSection();y<level.getMaxSection()&&DIRTY.size()<4096;y++)if(!level.getChunk(x,z).getSection(y-level.getMinSection()).hasOnlyAir())DIRTY.add(net.minecraft.core.SectionPos.asLong(x,y,z));
    }
    private static EnvironmentPayload.Sample sample(BlockPos pos){return SAMPLES.get(CellPos.fromBlock(pos.getX(),pos.getZ()));}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(mc.level==null){SAMPLES=Map.of();DIRTY.clear();dimension=null;haze=0;return;}
        if(!mc.level.dimension().location().equals(dimension)||mc.level.getGameTime()-received>300){var previous=SAMPLES;SAMPLES=Map.of();invalidate(previous.keySet());dimension=null;haze=0;}
        boolean enabled=com.civitasindustria.common.config.ClientConfig.TINT.get();if(enabled!=tintEnabled){tintEnabled=enabled;invalidate(SAMPLES.keySet());}
        var dirty=DIRTY.iterator();for(int n=0;n<8&&dirty.hasNext();n++){var section=net.minecraft.core.SectionPos.of(dirty.next());dirty.remove();mc.levelRenderer.setSectionDirty(section.x(),section.y(),section.z());}
        var s=mc.player==null?null:sample(mc.player.blockPosition());float target=s==null?0:s.smog();haze+=(target-haze)*.03f;
    }
    @SubscribeEvent public static void color(ViewportEvent.ComputeFogColor event){
        if(!com.civitasindustria.common.config.ClientConfig.HAZE.get()||event.getCamera().getFluidInCamera()!=net.minecraft.world.level.material.FogType.NONE)return;
        event.setRed(event.getRed()*(1-haze*.3f)+.25f*haze);event.setGreen(event.getGreen()*(1-haze*.35f)+.2f*haze);event.setBlue(event.getBlue()*(1-haze*.5f)+.12f*haze);
    }
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog event){
        if(haze<.01||!com.civitasindustria.common.config.ClientConfig.HAZE.get()||event.getType()!=net.minecraft.world.level.material.FogType.NONE)return;
        event.scaleFarPlaneDistance(1-haze*.6f);event.scaleNearPlaneDistance(1-haze*.8f);event.setCanceled(true);
    }
    public static int tint(int original,BlockPos pos,boolean water){
        var snapshot=SAMPLES;if(snapshot.isEmpty()||!com.civitasindustria.common.config.ClientConfig.TINT.get())return original;
        int x=Math.floorDiv(pos.getX()-32,64),z=Math.floorDiv(pos.getZ()-32,64);float fx=Math.floorMod(pos.getX()-32,64)/64f,fz=Math.floorMod(pos.getZ()-32,64)/64f,amount=0;
        for(int a=0;a<2;a++)for(int c=0;c<2;c++){var value=snapshot.get(new CellPos(x+a,z+c));if(value!=null)amount+=(water?value.water():value.injury())*(a==0?1-fx:fx)*(c==0?1-fz:fz)*.6f;}
        int target=water?0x747546:0x9b8753;
        int r=(int)(((original>>16)&255)*(1-amount)+((target>>16)&255)*amount),g=(int)(((original>>8)&255)*(1-amount)+((target>>8)&255)*amount),b=(int)((original&255)*(1-amount)+(target&255)*amount);
        return (r<<16)|(g<<8)|b;
    }
}
