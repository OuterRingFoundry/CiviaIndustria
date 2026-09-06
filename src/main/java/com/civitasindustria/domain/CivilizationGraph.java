package com.civitasindustria.domain;

import java.util.*;
/** Rebuilt only after node topology changes, never once per tick. */
public final class CivilizationGraph {
    public record Network(Set<CellPos> cells,int perimeter) {}
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
            networks.add(new Network(Set.copyOf(component),perimeter));
        }
        return List.copyOf(networks);
    }
    public static double maintenance(Network n,double base,double edge,double area) {
        return base+n.perimeter()*edge+n.cells().size()*area;
    }
}
