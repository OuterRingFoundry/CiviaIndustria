package com.civitasindustria.common.network;
import com.civitasindustria.domain.CellPos;
import com.civitasindustria.platform.WorldRuntime;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;
import java.util.function.Consumer;
/** Fixed nine-cell S2C neighborhood: no player-supplied coordinates or raw NBT. */
public record EnvironmentPayload(ResourceLocation dimension,List<Sample> samples) implements CustomPacketPayload {
    public record Sample(int x,int z,float smog,float injury,float water){
        public Sample {for(float f:new float[]{smog,injury,water})if(!Float.isFinite(f)||f<0||f>1)throw new IllegalArgumentException("Environment sample");}
    }
    public EnvironmentPayload {samples=List.copyOf(samples);if(samples.size()>9)throw new IllegalArgumentException("Environment packet bound");}
    public static Consumer<EnvironmentPayload> RECEIVER=packet->{};
    public static final Type<EnvironmentPayload> TYPE=new Type<>(ResourceLocation.parse("civitas_industria:environment"));
    public static final StreamCodec<RegistryFriendlyByteBuf,EnvironmentPayload> CODEC=new StreamCodec<>(){
        public EnvironmentPayload decode(RegistryFriendlyByteBuf b){
            ResourceLocation dimension=ResourceLocation.parse(b.readUtf(256));int size=b.readUnsignedByte();if(size>9)throw new IllegalArgumentException("Environment packet bound");
            List<Sample> samples=new ArrayList<>(size);for(int n=0;n<size;n++)samples.add(new Sample(b.readInt(),b.readInt(),b.readFloat(),b.readFloat(),b.readFloat()));
            return new EnvironmentPayload(dimension,samples);
        }
        public void encode(RegistryFriendlyByteBuf b,EnvironmentPayload p){b.writeUtf(p.dimension().toString(),256);b.writeByte(p.samples().size());for(var s:p.samples()){b.writeInt(s.x());b.writeInt(s.z());b.writeFloat(s.smog());b.writeFloat(s.injury());b.writeFloat(s.water());}}
    };
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){event.registrar("1").playToClient(TYPE,CODEC,(packet,context)->context.enqueueWork(()->RECEIVER.accept(packet)));}
    public static void send(ServerPlayer player){
        long start=System.nanoTime();var runtime=WorldRuntime.get(player.serverLevel());var center=CellPos.fromBlock(player.blockPosition().getX(),player.blockPosition().getZ());var samples=new ArrayList<Sample>(9);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
            var pos=center.offset(x,z);var data=runtime.state().cells.get(pos);
            samples.add(new Sample(pos.x(),pos.z(),data==null?0:(float)Math.min(1,data.aqi()/1000),data==null?0:(float)data.degradation,data==null?0:(float)(1-data.waterQuality()/100)));
        }
        PacketDistributor.sendToPlayer(player,new EnvironmentPayload(player.level().dimension().location(),samples));runtime.record("packets",start,samples.size());
    }
}
