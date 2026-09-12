package com.civitasindustria.domain;

import java.util.*;
/** Rebuilt only after node topology changes, never once per tick. */
public final class CivilizationGraph {
    public record Edge(CellPos cell,int dx,int dz) {
        public Edge {if(Math.abs(dx)+Math.abs(dz)!=1)throw new IllegalArgumentException("Cardinal edge required");}
        /** Four to twelve blocks outside the cell, with a four-block corner margin. */
        public int blockX(int along,int distance){return Math.addExact(Math.multiplyExact(cell.x(),64),dx<0?-distance:dx>0?63+distance:along);}
        public int blockZ(int along,int distance){return Math.addExact(Math.multiplyExact(cell.z(),64),dz<0?-distance:dz>0?63+distance:along);}
    }
    public record Network(Set<CellPos> cells,int perimeter,Map<CellPos,Integer> boundaryDepth,List<Edge> edges) {}
    public static List<Edge> isolatedEdges(CellPos cell){return List.of(new Edge(cell,1,0),new Edge(cell,-1,0),new Edge(cell,0,1),new Edge(cell,0,-1));}
    public List<Network> rebuild(Set<CellPos> occupied) {
        Set<CellPos> unseen=new HashSet<>(occupied);
        List<Network> networks=new ArrayList<>();
        while(!unseen.isEmpty()) {
            CellPos first=unseen.iterator().next();
            ArrayDeque<CellPos> queue=new ArrayDeque<>(); queue.add(first); unseen.remove(first);
            Set<CellPos> component=new HashSet<>(); List<Edge> edges=new ArrayList<>(); int perimeter=0;
            while(!queue.isEmpty()) {
                CellPos p=queue.remove(); component.add(p);
                for(CellPos next:List.of(p.offset(1,0),p.offset(-1,0),p.offset(0,1),p.offset(0,-1))) {
                    if(!occupied.contains(next)){perimeter++;edges.add(new Edge(p,next.x()-p.x(),next.z()-p.z()));}
                    else if(unseen.remove(next)) queue.add(next);
                }
            }
            networks.add(new Network(Set.copyOf(component),perimeter,boundaryDepth(component),List.copyOf(edges)));
        }
        return List.copyOf(networks);
    }
    /** Resumable topology work; each advance visits at most its explicit cell budget. */
    public static final class RebuildJob {
        private final Set<CellPos> occupied,unseen;
        private final List<Network> result=new ArrayList<>();
        private final ArrayDeque<CellPos> queue=new ArrayDeque<>(),depthQueue=new ArrayDeque<>();
        private Set<CellPos> component=new HashSet<>();private Map<CellPos,Integer> depth=new HashMap<>();
        private int perimeter;private boolean depths,done;private List<Edge> edges=new ArrayList<>();
        public RebuildJob(Set<CellPos> cells){occupied=Set.copyOf(cells);unseen=new HashSet<>(cells);}
        public boolean advance(int budget){
            if(budget<1)throw new IllegalArgumentException("Graph budget");
            while(budget>0&&!done){
                if(depths){
                    if(depthQueue.isEmpty()){
                        result.add(new Network(Set.copyOf(component),perimeter,Map.copyOf(depth),List.copyOf(edges)));edges=new ArrayList<>();component=new HashSet<>();depth=new HashMap<>();perimeter=0;depths=false;continue;
                    }
                    var p=depthQueue.remove();for(var n:neighbors(p))if(component.contains(n)&&!depth.containsKey(n)){depth.put(n,depth.get(p)+1);depthQueue.add(n);}budget--;continue;
                }
                if(queue.isEmpty()){
                    if(!component.isEmpty()){depths=true;continue;}
                    if(unseen.isEmpty()){done=true;break;}
                    var p=unseen.iterator().next();unseen.remove(p);queue.add(p);
                }
                var p=queue.remove();component.add(p);boolean boundary=false;
                for(var n:neighbors(p)){if(!occupied.contains(n)){perimeter++;boundary=true;edges.add(new Edge(p,n.x()-p.x(),n.z()-p.z()));}else if(unseen.remove(n))queue.add(n);}
                if(boundary){depth.put(p,0);depthQueue.add(p);}budget--;
            }
            return done;
        }
        public List<Network> result(){if(!done)throw new IllegalStateException("Graph still building");return List.copyOf(result);}
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
