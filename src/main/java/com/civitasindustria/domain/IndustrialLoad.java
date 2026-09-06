package com.civitasindustria.domain;
import java.util.Map;
public final class IndustrialLoad {
    public record Chunk(int x,int z) {}
    public static double effective(Map<Chunk,Double> raw,Chunk center,double cardinal,double diagonal) {
        double total=raw.getOrDefault(center,0.0);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++) {
            if(x==0&&z==0)continue;
            total+=raw.getOrDefault(new Chunk(center.x()+x,center.z()+z),0.0)*(x==0||z==0?cardinal:diagonal);
        }
        return total;
    }
}
