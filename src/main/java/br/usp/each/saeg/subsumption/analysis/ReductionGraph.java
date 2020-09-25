package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.asm.defuse.DefUseChain;
import br.usp.each.saeg.opal.Graph;
import br.usp.each.saeg.subsumption.graphdua.Dua;

import java.util.*;

public class ReductionGraph extends Graph<ReductionNode> {
    private final SubsumptionGraph sbg;
    private Graph<ReductionNode> inv;
    private int index;
    private final Stack<SubsumptionNode> stack = new Stack<>();
    private BitSet[] subsumptionVector;
    private HashMap<Integer, List<DefUseChain>> dua2DefUseChains;
    private int [] lines;

    public ReductionGraph(SubsumptionGraph g) {
        sbg = g;
        subsumptionVector = sbg.getSubsumptionVector();
        findStrongConnectedWithTarjan();
        connectStronglyConnectedComponents();
    }

    public ReductionGraph(SubsumptionGraph g, boolean test) {
        sbg = g;
        if (!test)
            subsumptionVector = sbg.getSubsumptionVector();
        findStrongConnectedWithTarjan();
        if (!test)
            connectStronglyConnectedComponents();
    }

    private void findStrongConnectedWithTarjan() {
        index = 0;
        stack.clear();
        ReductionNode r;
        Iterator<SubsumptionNode> it = sbg.iterator();

        while (it.hasNext()) {
            SubsumptionNode v = it.next();
            if (v.isUndefined()) {
                strongConnect(v);
            }
        }

        return;
    }

    private ReductionNode strongConnect(SubsumptionNode v) {
        ReductionNode r = null;
        v.setIndex(index);
        v.setLowIndex(index);
        index++;
        stack.push(v);

        for (SubsumptionNode w : sbg.neighbors(v.id())) {
            int min;
            if (w.isUndefined()) {
                strongConnect(w);
                min = java.lang.Math.min(v.getLowIndex(), w.getLowIndex());
                v.setLowIndex(min);
            } else if (stack.contains(w)) {
                min = java.lang.Math.min(v.getLowIndex(), w.getIndex());
                v.setLowIndex(min);
            }
        }

        if (v.getIndex() == v.getLowIndex()) {
            SubsumptionNode w;
            r = new ReductionNode();
            do {
                w = stack.pop();
                r.addDua(w.getDua());
            } while (!v.equals(w));
            this.add(r);
            //System.out.println(r);
        }

        return r;
    }

    private void connectStronglyConnectedComponents() {
        Iterator<ReductionNode> it = this.iterator();

        while (it.hasNext()) {
            ReductionNode r = it.next();

            Iterator<ReductionNode> it2 = this.iterator();
            while (it2.hasNext()) {
                ReductionNode w = it2.next();
                if (r.equals(w))
                    continue;

                Dua d1, d2;
                int id1, id2;

                d1 = r.getListDuas().get(0);
                d2 = w.getListDuas().get(0);
                id1 = sbg.getDuaId(d1);
                id2 = sbg.getDuaId(d2);

                if (subsumptionVector[id1] != null && subsumptionVector[id1].get(id2)) {
                    this.addEdge(w.id(), r.id());
                }
            }
        }
    }

    public List<ReductionNode> unconstrainedNodes() {
        List<ReductionNode> unconstrained = new LinkedList<>();
        Iterator<ReductionNode> it = this.iterator();

        while (it.hasNext()) {
            ReductionNode r = it.next();
            if (this.neighbors(r.id()).isEmpty())
                unconstrained.add(r);
        }

        return unconstrained;
    }

    public void findTransitiveClosure() {
        List<ReductionNode> u = unconstrainedNodes();
        Map<ReductionNode, Integer> visited = new HashMap<>();

        inv = this.inverse();

        for (ReductionNode ur : u) {
            visited.clear();
            Iterator<ReductionNode> it = this.iterator();

            while (it.hasNext()) {
                ReductionNode rr = it.next();
                visited.put(rr, 0);
            }

            for (ReductionNode pred : inv.neighbors(ur.id())) {
                trimNotNeededEdges(pred, visited, 0);
            }
            //System.out.println("Unc "+ur.id());
            for (ReductionNode pred : inv.neighbors(ur.id())) {
                //System.out.println("Pred:"+pred.id()+":"+ visited.get(pred));
                if(visited.get(pred) > 1)
                    this.removeEdge(pred.id(), ur.id());
            }
        }

    }

    private void trimNotNeededEdges(ReductionNode p, Map<ReductionNode, Integer> vis, int pathlength) {
        int newlength=pathlength+1;

        if(vis.get(p) == 0) {
            vis.put(p, newlength);
            for (ReductionNode pp : inv.neighbors(p.id())) {
               trimNotNeededEdges(pp, vis, newlength);
            }
        }
        //System.out.println("\nnode "+p.id());
        for (ReductionNode pp : inv.neighbors(p.id())) {
            //System.out.println("pp:"+pp.id()+":"+ (vis.get(pp) - vis.get(p)));
            //System.out.println("diff:"+ vis.get(pp) + "," + vis.get(p));
            if(vis.get(pp) - vis.get(p) > 1)
                this.removeEdge(pp.id(), p.id());
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        Iterator<ReductionNode> i = this.iterator();

        sb.append("Reduction Graph Nodes: \n");

        while (i.hasNext()) {
            ReductionNode k = i.next();
            sb.append(k);
            sb.append("\n");
        }

        sb.append("Reduction Graph : \n");
        i = this.iterator();

        while (i.hasNext()) {
            ReductionNode k = i.next();
            sb.append(k.id());
            sb.append(" -> \n\t");

            Set<ReductionNode> neighbors = this.neighbors(k.id());
            for (ReductionNode kn : neighbors) {
                sb.append(kn.id());
                sb.append(" ");
            }

            sb.append('\n');
        }

        return sb.toString();
    }

    public String toDot() {
        final StringBuilder sb = new StringBuilder();
        Iterator<ReductionNode> it = this.iterator();
        final boolean nodePrint = true;

        sb.append("digraph { \n");
        while(it.hasNext()) {
                ReductionNode r = it.next();
                sb.append(r.id());

                if (this.unconstrainedNodes().contains(r))
                    sb.append(" [shape=box,style=filled,color=\".0 .0 .83\",label=\"");
                else
                    sb.append(" [label=\"");

                Iterator<Dua> itDua = r.getListDuas().iterator();
                while (itDua.hasNext()) {
                    Dua d = itDua.next();

                    if(!this.dua2DefUseChains.isEmpty() && !nodePrint)
                        sb.append(toStringLineDua(d));
                    else
                        sb.append(d.toString()); // For examples written by hand

                    if (itDua.hasNext())
                        sb.append("\\n");
                }

                sb.append("\"];\n");
            }

        it = this.iterator();

        while (it.hasNext()) {
            ReductionNode k = it.next();

            Set<ReductionNode> neighbors = this.neighbors(k.id());
            for (ReductionNode kn : neighbors) {
                sb.append(k.id());
                sb.append(" -> ");
                sb.append(kn.id());
                sb.append(";\n");
            }

        }
        sb.append("}");

        return sb.toString();
    }


    public void setDua2DefUseChains(HashMap<Integer,List<DefUseChain>> m) { dua2DefUseChains = m;}

    public void setLines(int [] lines) { this.lines = lines;}

    private boolean isDefUseChainsMapSet() {
        return (dua2DefUseChains != null);
    }

    public List<DefUseChain> getDefUseChains(Dua d) {
        if (isDefUseChainsMapSet()) {
            return dua2DefUseChains.get(d.hashCode());
        }
        return null;
    }

    public SubsumptionGraph getSubsumptionGraph() {
        return sbg;
    }

    private String toStringLineDua(Dua d) {
        final StringBuilder sb = new StringBuilder();

        if (isDefUseChainsMapSet() && lines != null) {
            DefUseChain dc;
            Iterator<DefUseChain> itDefChain = getDefUseChains(d).iterator();
            while (itDefChain.hasNext()) {
                dc = itDefChain.next();
                if (dc.isComputationalChain())
                    sb.append("[" + lines[dc.def] + "," + lines[dc.use] + "," + d.varName() + "]");
                else
                    sb.append("[" + lines[dc.def] + ",(" + lines[dc.use] + "," + lines[dc.target] + ")," + d.varName() + "]");
            }
        }
        else
            sb.append(d);

        return sb.toString();
    }
}
