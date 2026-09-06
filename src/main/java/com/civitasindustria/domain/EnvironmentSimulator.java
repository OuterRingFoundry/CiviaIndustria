package com.civitasindustria.domain;

import java.util.*;
import java.util.function.Function;

/** Two-phase transport: iteration order cannot create or destroy transported mass. */
public final class EnvironmentSimulator {
    public record Climate(boolean rain,double recovery,double altitude,boolean surfaceWater) {}
    private static final int[][] DIRECTIONS={{1,0},{-1,0},{0,1},{0,-1}};
    public int step(Map<CellPos,CellData> cells,Collection<CellPos> active,
                    Function<CellPos,Climate> climate,SimulationSettings s,long time) {
        Map<CellPos,double[]> deltas=new TreeMap<>();
        List<CellPos> work=active.stream().filter(cells::containsKey).sorted().limit(s.maxCells()).toList();
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
                    delta[i]-=transport;
                    for(int[] direction:DIRECTIONS) {
                        boolean downwind=direction[0]==s.windX()&&direction[1]==s.windZ();
                        double weight=(1-s.windBias())/4+(downwind?s.windBias():0);
                        if(s.windX()==0&&s.windZ()==0) weight=.25;
                        double amount=transport*weight;
                        if(amount>0) deltas.computeIfAbsent(pos.offset(direction[0],direction[1]),k->new double[Pollutant.values().length])[i]+=amount;
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
                        delta[i]-=runoff;
                        for(CellPos next:downstream) deltas.computeIfAbsent(next,k->new double[Pollutant.values().length])[i]+=runoff/downstream.size();
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
        return work.size();
    }
    private static double approach(double value,double target,double down,double up) {
        double result=value+(target-value)*(target<value?down:up);
        return Math.abs(result-target)<.00001?target:Math.clamp(result,0,1);
    }
}
