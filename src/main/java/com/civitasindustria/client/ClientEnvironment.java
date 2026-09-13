package com.civitasindustria.client;

import com.civitasindustria.common.config.ClientConfig;
import com.civitasindustria.common.network.EnvironmentPayload;
import com.civitasindustria.domain.PollutionEffects;
import com.civitasindustria.domain.PollutionVisuals;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import java.util.LinkedHashSet;

@EventBusSubscriber(modid="civitas_industria",value=Dist.CLIENT)
public final class ClientEnvironment {
    // Published snapshots are never mutated: render workers can read primitive keys without allocations.
    private static volatile Long2ObjectOpenHashMap<EnvironmentPayload.Sample> samples=new Long2ObjectOpenHashMap<>();
    private static final LinkedHashSet<Long> columns=new LinkedHashSet<>(),sections=new LinkedHashSet<>();
    private static ClientLevel observedLevel;
    private static long received;
    private static float haze;
    private static boolean tintEnabled=true;
    private static final int[] DEGREE_COLORS={0x83b878,0xb6be79,0xd3b96d,0xcc965c,0xbc7055,0xa98080};
    private static long key(int x,int z){return ChunkPos.asLong(x,z);}
    private static void reset(ClientLevel level){samples=new Long2ObjectOpenHashMap<>();columns.clear();sections.clear();observedLevel=level;haze=0;}
    public static void receive(EnvironmentPayload packet){
        var level=Minecraft.getInstance().level;
        if(level==null||!level.dimension().location().equals(packet.dimension()))return;
        if(observedLevel!=level)reset(level);
        boolean prioritizeNewQueue=columns.isEmpty();
        var previous=samples;var next=new Long2ObjectOpenHashMap<EnvironmentPayload.Sample>(9);
        for(var value:packet.samples())next.put(key(value.x(),value.z()),value);
        samples=next;received=level.getGameTime();
        for(var old:previous.values()){
            var value=next.get(key(old.x(),old.z()));
            if(value==null||old.injury()!=value.injury()||old.water()!=value.water())invalidate(old.x(),old.z());
        }
        for(var value:next.values())if(!previous.containsKey(key(value.x(),value.z()))&&(value.injury()>0||value.water()>0))invalidate(value.x(),value.z());
        if(prioritizeNewQueue)prioritize();
    }
    private static void invalidate(int cellX,int cellZ){
        // Queue only coordinates here. Loaded-column/section inspection is spread over client ticks.
        for(int x=cellX*4-2;x<cellX*4+6;x++)for(int z=cellZ*4-2;z<cellZ*4+6;z++)
            if(columns.size()<512)columns.add(key(x,z));
    }
    private static void prioritize(){
        var player=Minecraft.getInstance().player;if(player==null||columns.isEmpty())return;
        int x=player.chunkPosition().x,z=player.chunkPosition().z;
        var ordered=new java.util.ArrayList<>(columns);
        ordered.sort(java.util.Comparator.comparingLong(p->{long dx=ChunkPos.getX(p)-x,dz=ChunkPos.getZ(p)-z;return dx*dx+dz*dz;}));
        columns.clear();columns.addAll(ordered);
    }
    private static void invalidateAll(){boolean empty=columns.isEmpty();for(var value:samples.values())invalidate(value.x(),value.z());if(empty)prioritize();}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(mc.level!=observedLevel)reset(mc.level);
        if(mc.level==null)return;
        if(!samples.isEmpty()&&mc.level.getGameTime()-received>300){invalidateAll();samples=new Long2ObjectOpenHashMap<>();haze=0;}
        boolean enabled=ClientConfig.TINT.get();if(enabled!=tintEnabled){tintEnabled=enabled;invalidateAll();}
        var queued=columns.iterator();
        for(int n=0;n<ClientConfig.TINT_COLUMNS.get()&&queued.hasNext()&&sections.size()+mc.level.getSectionsCount()<=4096;n++){
            long value=queued.next();queued.remove();int x=ChunkPos.getX(value),z=ChunkPos.getZ(value);
            var chunk=mc.level.getChunkSource().getChunk(x,z,ChunkStatus.FULL,false);
            if(chunk==null)continue;
            for(int y=mc.level.getMinSection();y<mc.level.getMaxSection()&&sections.size()<4096;y++)
                if(!chunk.getSection(y-mc.level.getMinSection()).hasOnlyAir())sections.add(SectionPos.asLong(x,y,z));
        }
        var dirty=sections.iterator();
        for(int n=0;n<ClientConfig.TINT_SECTIONS.get()&&dirty.hasNext();n++){
            long value=dirty.next();dirty.remove();mc.levelRenderer.setSectionDirty(SectionPos.x(value),SectionPos.y(value),SectionPos.z(value));
        }
        var s=mc.player==null?null:sample(mc.player.blockPosition());
        float target=s==null?0:s.smog();haze+=(target-haze)*.03f;
    }
    private static EnvironmentPayload.Sample sample(BlockPos pos){return samples.get(key(Math.floorDiv(pos.getX(),64),Math.floorDiv(pos.getZ(),64)));}
    @SubscribeEvent public static void color(ViewportEvent.ComputeFogColor event){
        if(!ClientConfig.HAZE.get()||event.getCamera().getFluidInCamera()!=net.minecraft.world.level.material.FogType.NONE)return;
        float amount=haze*ClientConfig.HAZE_STRENGTH.get().floatValue();
        event.setRed(event.getRed()*(1-amount*.3f)+.25f*amount);event.setGreen(event.getGreen()*(1-amount*.35f)+.2f*amount);event.setBlue(event.getBlue()*(1-amount*.5f)+.12f*amount);
    }
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog event){
        if(haze<.01||!ClientConfig.HAZE.get()||event.getType()!=net.minecraft.world.level.material.FogType.NONE)return;
        float amount=haze*ClientConfig.HAZE_STRENGTH.get().floatValue();
        event.scaleFarPlaneDistance(1-amount*.6f);event.scaleNearPlaneDistance(1-amount*.8f);event.setCanceled(true);
    }
    @SubscribeEvent public static void hud(RenderGuiEvent.Post event){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui||!ClientConfig.POLLUTION_HUD.get())return;
        var value=sample(mc.player.blockPosition());if(value==null)return;
        var degree=PollutionEffects.degree(value.severity());int rank=degree.ordinal();

        var gui=event.getGuiGraphics();
        var label=Component.translatable("gui.civitas_industria.pollution",Component.translatable("pollution.civitas_industria."+degree.name().toLowerCase(java.util.Locale.ROOT)));
        int width=Math.max(110,mc.font.width(label)+12);gui.fill(6,6,6+width,31,0x990f1718);
        gui.drawString(mc.font,label,12,10,DEGREE_COLORS[rank],false);
        for(int n=0;n<6;n++)gui.fill(12+n*16,23,25+n*16,26,n<=rank?0xff000000|DEGREE_COLORS[n]:0xff394346);
    }
    public static int tint(int original,BlockPos pos,boolean water){
        var snapshot=samples;if(snapshot.isEmpty()||!ClientConfig.TINT.get())return original;
        int x=Math.floorDiv(pos.getX()-32,64),z=Math.floorDiv(pos.getZ()-32,64);
        float fx=Math.floorMod(pos.getX()-32,64)/64f,fz=Math.floorMod(pos.getZ()-32,64)/64f,amount=0;
        for(int a=0;a<2;a++)for(int b=0;b<2;b++){
            var value=snapshot.get(key(x+a,z+b));
            if(value!=null)amount+=(water?value.water():value.injury())*(a==0?1-fx:fx)*(b==0?1-fz:fz);
        }
        return PollutionVisuals.tint(original,amount,water);
    }
    /** Development diagnostics, no scans or mutable state exposure. */
    public static int pendingColumns(){return columns.size();}
    public static int pendingSections(){return sections.size();}
}
