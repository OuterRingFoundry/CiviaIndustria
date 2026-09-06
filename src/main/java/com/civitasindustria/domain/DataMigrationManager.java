package com.civitasindustria.domain;
import java.io.*;
import java.util.*;

/** Domain snapshot format independent of Minecraft NBT. Bounded and deterministic. */
public final class DataMigrationManager {
    public static final int VERSION=3, INVENTORY_VERSION=2, MAX_RECORDS=100_000, MAX_BYTES=32*1024*1024;
    private static final int MAGIC=0x43495649;
    public static byte[] encode(WorldState state) {
        try {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
            out.writeInt(MAGIC);out.writeInt(VERSION);
            writeSize(out,state.cells.size());
            for(var e:state.cells.entrySet()){
                out.writeInt(e.getKey().x());out.writeInt(e.getKey().z());CellData c=e.getValue();
                for(double v:c.pollutants)out.writeDouble(v);
                for(double v:new double[]{c.vegetationHealth,c.biodiversity,c.cropSuitability,c.aquaticHealth,c.degradation,c.threatPressure,c.acidPrecursorLoad})out.writeDouble(v);
                out.writeLong(c.lastEnvironmentUpdate);out.writeLong(c.lastEcologyUpdate);out.writeLong(c.lastThreatUpdate);
                out.writeUTF(c.civilization.name());
            }
            writeSize(out,state.nodes.size());
            for(var e:state.nodes.entrySet()){
                var n=e.getValue();out.writeLong(e.getKey());out.writeInt(n.cell.x());out.writeInt(n.cell.z());
                uuid(out,n.owner);out.writeUTF(n.kind.name());out.writeLong(n.credits);out.writeInt(n.missedPayments);
            }
            List<Parcel> parcels=state.parcels.all().stream().sorted(Comparator.comparing(p->p.id().toString())).toList();
            writeSize(out,parcels.size());
            for(Parcel p:parcels){
                uuid(out,p.id());uuid(out,p.owner());
                for(int v:new int[]{p.minX(),p.minY(),p.minZ(),p.maxX(),p.maxY(),p.maxZ()})out.writeInt(v);
                out.writeUTF(p.name());out.writeInt(p.trusted().size());
                for(UUID id:p.trusted().stream().sorted().toList())uuid(out,id);
                out.writeInt(p.flags().size());for(Parcel.Flag flag:p.flags().stream().sorted().toList())out.writeUTF(flag.name());
            }
            writeSize(out,state.rawLoad.size());
            for(var entry:state.rawLoad.entrySet().stream().sorted(Comparator.comparingInt((Map.Entry<IndustrialLoad.Chunk,Double> e)->e.getKey().x()).thenComparingInt(e->e.getKey().z())).toList()){
                out.writeInt(entry.getKey().x());out.writeInt(entry.getKey().z());out.writeDouble(entry.getValue());
            }
            out.flush();if(bytes.size()>MAX_BYTES)throw new IllegalStateException("World snapshot exceeds safe size");
            return bytes.toByteArray();
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    public static WorldState decode(byte[] bytes) {
        if(bytes.length>MAX_BYTES)throw new IllegalArgumentException("Snapshot too large");
        try{
            DataInputStream in=new DataInputStream(new ByteArrayInputStream(bytes));
            if(in.readInt()!=MAGIC)throw new IOException("Invalid Civitas magic");
            int version=in.readInt();
            if(version<1||version>VERSION)throw new IOException("Unsupported Civitas data version "+version);
            WorldState s=new WorldState();
            // v1 is the explicitly empty foundation format. No gameplay state existed in Phase 0.
            if(version==1){if(in.available()!=0)throw new IOException("Unexpected v1 state");return s;}
            int count=size(in,MAX_RECORDS);
            for(int k=0;k<count;k++){
                CellPos p=new CellPos(in.readInt(),in.readInt());CellData c=new CellData();
                for(int i=0;i<c.pollutants.length;i++)c.pollutants[i]=number(in,1_000_000);
                c.vegetationHealth=number(in,1);c.biodiversity=number(in,1);c.cropSuitability=number(in,1);
                c.aquaticHealth=number(in,1);c.degradation=number(in,1);c.threatPressure=number(in,1_000_000);c.acidPrecursorLoad=number(in,2_000_000);
                c.lastEnvironmentUpdate=in.readLong();c.lastEcologyUpdate=in.readLong();c.lastThreatUpdate=in.readLong();
                c.civilization=CellData.Civilization.valueOf(in.readUTF());
                if(s.cells.put(p,c)!=null)throw new IOException("Duplicate cell");
            }
            count=size(in,MAX_RECORDS);
            for(int k=0;k<count;k++){
                long key=in.readLong();var node=new WorldState.CivicNode(new CellPos(in.readInt(),in.readInt()),uuid(in),WorldState.CivicNode.Kind.valueOf(in.readUTF()));
                node.credits=in.readLong();node.missedPayments=in.readInt();
                if(node.credits<0||node.missedPayments<0||s.nodes.put(key,node)!=null)throw new IOException("Invalid civic node");
            }
            count=size(in,MAX_RECORDS);
            for(int k=0;k<count;k++){
                UUID id=uuid(in),owner=uuid(in);int x=in.readInt(),y=in.readInt(),z=in.readInt(),a=in.readInt(),b=in.readInt(),c=in.readInt();
                String name=in.readUTF();Set<UUID> trusted=new HashSet<>();
                for(int n=size(in,64);n>0;n--)trusted.add(uuid(in));
                Set<Parcel.Flag> flags=EnumSet.noneOf(Parcel.Flag.class);
                for(int n=size(in,Parcel.Flag.values().length);n>0;n--)flags.add(Parcel.Flag.valueOf(in.readUTF()));
                s.parcels.add(new Parcel(id,owner,x,y,z,a,b,c,name,trusted,flags));
            }
            if(version>=3){
                count=size(in,MAX_RECORDS);for(int n=0;n<count;n++){
                    var chunk=new IndustrialLoad.Chunk(in.readInt(),in.readInt());double load=number(in,1_000_000_000_000.0);
                    if(s.rawLoad.put(chunk,load)!=null)throw new IOException("Duplicate industrial chunk");
                }
            }
            if(in.available()!=0)throw new IOException("Trailing snapshot data");
            return s;
        }catch(IOException|IllegalArgumentException e){throw new IllegalArgumentException("Refusing damaged or incompatible Civitas data",e);}
    }
    public static byte[] encodeInventory(BulkInventory inventory){
        try{
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
            out.writeInt(INVENTORY_VERSION);out.writeInt(inventory.maxKeys());out.writeLong(inventory.capacity());out.writeInt(inventory.contents().size());
            for(var e:inventory.contents().entrySet()){out.writeUTF(e.getKey());out.writeLong(e.getValue());}
            return bytes.toByteArray();
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    public static BulkInventory decodeInventory(byte[] bytes){
        if(bytes.length>128*1024)throw new IllegalArgumentException("Cargo data too large");
        try{
            DataInputStream in=new DataInputStream(new ByteArrayInputStream(bytes));
            if(in.readInt()!=INVENTORY_VERSION)throw new IOException("Unsupported cargo data version");
            BulkInventory inventory=new BulkInventory(in.readInt(),in.readLong());
            for(int n=size(in,256);n>0;n--){String key=in.readUTF();long count=in.readLong();
                if(count<=0||inventory.count(key)!=0||inventory.insert(key,count,false)!=count)throw new IOException("Invalid cargo counts");}
            if(in.available()!=0)throw new IOException("Trailing cargo data");
            return inventory;
        }catch(IOException|IllegalArgumentException e){throw new IllegalArgumentException("Invalid cargo snapshot",e);}
    }
    private static double number(DataInputStream in,double max)throws IOException{double v=in.readDouble();if(!Double.isFinite(v)||v<0||v>max)throw new IOException("Invalid numeric state");return v;}
    private static int size(DataInputStream in,int max)throws IOException{int n=in.readInt();if(n<0||n>max)throw new IOException("Record limit");return n;}
    private static void writeSize(DataOutputStream out,int n)throws IOException{if(n>MAX_RECORDS)throw new IOException("Record limit");out.writeInt(n);}
    private static UUID uuid(DataInputStream in)throws IOException{return new UUID(in.readLong(),in.readLong());}
    private static void uuid(DataOutputStream out,UUID id)throws IOException{out.writeLong(id.getMostSignificantBits());out.writeLong(id.getLeastSignificantBits());}
}
