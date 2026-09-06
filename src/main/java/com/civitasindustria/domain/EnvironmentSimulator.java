package com.civitasindustria.domain;

import java.util.*;
import java.util.function.Function;

/** Two-phase transport: iteration order cannot create or destroy transported mass. */
public final class EnvironmentSimulator {
    public record Climate(boolean rain,double recovery,double altitude,boolean surfaceWater) {}
    private static final int[][] DIRECTIONS={{1,0},{-1,0},{0,1},{0,-1}};
    public int step(Map<CellPos,CellData> cells,Collection<CellPos> active,
                    Function<CellPos,Climate> climate,SimulationSettings s,long time) {
        return step(cells,active,climate,s,time,DataMigrationManager.MAX_RECORDS);
    }
    public int step(Map<CellPos,CellData> cells,Collection<CellPos> active,
                    Function<CellPos,Climate> climate,SimulationSettings s,long time,int recordLimit) {
        if(recordLimit<cells.size())throw new IllegalArgumentException("Record limit below stored cell count");
        Set<CellPos> reserved=new HashSet<>();
        Map<CellPos,double[]> deltas=new TreeMap<>();
        List<CellPos> work=active.stream().distinct().filter(cells::containsKey).sorted().limit(s.maxCells()).toList();
        for(CellPos pos:work) {
            CellData c=cells.get(pos);
            Climate weather=climate.apply(pos);
            double[] delta=deltas.computeIfAbsent(pos,k->new double[Pollutant.values().length]);
            for(Pollutant p:Pollutant.values()) {
                int i=p.ordinal();
                double value=c.get(p);
                double decay=i<4?s.airDecay():(p==Pollutant.HEAT||p==Pollutant.NOISE?Math.min(1,s.airDecay()*8):s.soilRecovery());
                delta[i]-=value*decay;
                double remaining=value*(1-decay);
                double deposition=weather.rain()&&i<4?remaining*s.wetDeposition():0;
                delta[i]-=deposition;
                if(p==Pollutant.SOX||p==Pollutant.NOX) {
                    delta[Pollutant.SOIL_ACIDITY.ordinal()]+=deposition*.6;
                    delta[Pollutant.W_ACIDITY.ordinal()]+=deposition*.4;
                } else if(p==Pollutant.TOX||p==Pollutant.PM) {
                    delta[Pollutant.SOIL_TOXICITY.ordinal()]+=deposition*.6;
                    delta[Pollutant.W_TOXICITY.ordinal()]+=deposition*.4;
                }
                remaining-=deposition;
                if(i<4) {
                    double transport=remaining*s.diffusion();

                    for(int[] direction:DIRECTIONS) {
                        boolean downwind=direction[0]==s.windX()&&direction[1]==s.windZ();
                        double weight=(1-s.windBias())/4+(downwind?s.windBias():0);
                        if(s.windX()==0&&s.windZ()==0) weight=.25;
                        double amount=transport*weight;
                        if(amount>0) transfer(cells,deltas,reserved,recordLimit,pos,pos.offset(direction[0],direction[1]),i,amount);
                    }
                } else if(i>=4&&i<=7&&weather.surfaceWater()) {
                    List<CellPos> downstream=new ArrayList<>(4);
                    for(int[] d:DIRECTIONS) {
                        CellPos neighbor=pos.offset(d[0],d[1]);
                        Climate n=climate.apply(neighbor);
                        if(n.surfaceWater()&&n.altitude()<=weather.altitude()) downstream.add(neighbor);
                    }
                    if(!downstream.isEmpty()) {
                        double runoff=remaining*s.waterRunoff();
                        for(CellPos next:downstream) transfer(cells,deltas,reserved,recordLimit,pos,next,i,runoff/downstream.size());
                    }
                }
            }
        }
        for(var entry:deltas.entrySet()) {
            CellData c=cells.computeIfAbsent(entry.getKey(),k->new CellData());
            for(Pollutant p:Pollutant.values()) {
                c.add(p,entry.getValue()[p.ordinal()]);
                if(c.get(p)<s.epsilon()) c.pollutants[p.ordinal()]=0;
            }
            c.acidPrecursorLoad=c.get(Pollutant.SOX)+c.get(Pollutant.NOX);
        }
        for(CellPos pos:work) {
            CellData c=cells.get(pos);
            Climate weather=climate.apply(pos);
            double stress=Math.clamp((c.aqi()+c.get(Pollutant.SOIL_ACIDITY)+c.get(Pollutant.SOIL_TOXICITY))/200,0,1);
            double waterStress=1-c.waterQuality()/100;
            c.vegetationHealth=approach(c.vegetationHealth,1-stress,s.injury(),s.ecologyRecovery()*weather.recovery());
            c.aquaticHealth=approach(c.aquaticHealth,1-waterStress,s.injury(),s.ecologyRecovery()*weather.recovery());
            c.biodiversity=approach(c.biodiversity,Math.min(c.vegetationHealth,c.aquaticHealth),s.injury(),s.ecologyRecovery()/2);
            c.cropSuitability=c.vegetationHealth;
            c.degradation=Math.clamp(1-(c.vegetationHealth+c.aquaticHealth+c.biodiversity)/3,0,1);
            c.lastEnvironmentUpdate=time; c.lastEcologyUpdate=time;
            if(c.pristine()) cells.remove(pos);
        }
        // Transport recipients below epsilon must not leave pristine historical records behind.
        for(CellPos pos:deltas.keySet()) {
            CellData c=cells.get(pos);if(c!=null&&c.pristine())cells.remove(pos);
        }
        return work.size();
    }
    /** Refused transport stays at the source when record or concentration limits are reached. */
    private static void transfer(Map<CellPos,CellData> cells,Map<CellPos,double[]> deltas,
                                 Set<CellPos> reserved,int limit,CellPos source,CellPos target,int pollutant,double requested) {
        if(requested<=0)return;
        if(!cells.containsKey(target)&&!reserved.contains(target)) {
            if(cells.size()+reserved.size()>=limit)return;
            reserved.add(target);
        }
        double[] next=deltas.computeIfAbsent(target,k->new double[Pollutant.values().length]);
        CellData existing=cells.get(target);
        double value=existing==null?0:existing.pollutants[pollutant];
        // Negative deltas are ignored for capacity: later source processing cannot overfill a target.
        double accepted=Math.min(requested,Math.max(0,1_000_000-value-Math.max(0,next[pollutant])));
        next[pollutant]+=accepted;
        deltas.get(source)[pollutant]-=accepted;
    }
    private static double approach(double value,double target,double down,double up) {
        double result=value+(target-value)*(target<value?down:up);
        return Math.abs(result-target)<.00001?target:Math.clamp(result,0,1);
    }
}
