package com.civitasindustria.domain;

import java.util.*;
/** Rebuilt only after node topology changes, never once per tick. */
public final class CivilizationGraph {
    public record Network(Set<CellPos> cells,int perimeter,Map<CellPos,Integer> boundaryDepth) {}
    public List<Network> rebuild(Set<CellPos> occupied) {
        Set<CellPos> unseen=new HashSet<>(occupied);
        List<Network> networks=new ArrayList<>();
        while(!unseen.isEmpty()) {
            CellPos first=unseen.iterator().next();
            ArrayDeque<CellPos> queue=new ArrayDeque<>(); queue.add(first); unseen.remove(first);
            Set<CellPos> component=new HashSet<>(); int perimeter=0;
            while(!queue.isEmpty()) {
                CellPos p=queue.remove(); component.add(p);
                for(CellPos next:List.of(p.offset(1,0),p.offset(-1,0),p.offset(0,1),p.offset(0,-1))) {
                    if(!occupied.contains(next)) perimeter++;
                    else if(unseen.remove(next)) queue.add(next);
                }
            }
            networks.add(new Network(Set.copyOf(component),perimeter,boundaryDepth(component)));
        }
        return List.copyOf(networks);
    }
    private static Map<CellPos,Integer> boundaryDepth(Set<CellPos> cells) {
        Map<CellPos,Integer> depth=new HashMap<>();ArrayDeque<CellPos> queue=new ArrayDeque<>();
        for(CellPos p:cells)if(neighbors(p).stream().anyMatch(n->!cells.contains(n))) {depth.put(p,0);queue.add(p);}
        while(!queue.isEmpty()) {
            CellPos p=queue.remove();
            for(CellPos n:neighbors(p))if(cells.contains(n)&&!depth.containsKey(n)){depth.put(n,depth.get(p)+1);queue.add(n);}
        }
        return Map.copyOf(depth);
    }
    private static List<CellPos> neighbors(CellPos p) {
        return List.of(p.offset(1,0),p.offset(-1,0),p.offset(0,1),p.offset(0,-1));
    }
    public static CellData.Civilization maintenanceState(int missed,int depth) {
        if(missed==Integer.MAX_VALUE)return CellData.Civilization.FRONTIER;
        long delay=3L*depth;
        return missed<2+delay?CellData.Civilization.CIVILIZED:
            missed<5+delay?CellData.Civilization.FRONTIER:CellData.Civilization.WILDERNESS;
    }
    public static double maintenance(Network n,double base,double edge,double area) {
        return base+n.perimeter()*edge+n.cells().size()*area;
    }
}
