package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.opal.Identifiable;
import br.usp.each.saeg.subsumption.graphdua.Flowgraph;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.graphdua.Node;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodeDominance<K extends Identifiable> {
    private final Map<Integer, Node> id2Node = new LinkedHashMap<Integer, Node>();
    private final Map<Node, Integer> node2Id = new LinkedHashMap<Node, Integer>();
    private final Map<Integer, K> id2K = new LinkedHashMap<Integer, K>();
    private final Map<K, Integer> k2Id = new LinkedHashMap<K, Integer>();
    private BitSet[] dom;
    private BitSet[] posdom;
    private Flowgraph<K> g;
    private Flowgraph<K> invg;
    private Graphdua graphdua;
    private Graphdua invgraphdua;

    // TODO: code below needs to be tested
    public NodeDominance(Flowgraph<K> graph, Flowgraph<K> invgraph) {
        this.g = graph;
        this.invg = invgraph;

        g.findReversePostOrder();

        Iterator<K> it = g.iterator();

        int i = 0;

        while (it.hasNext()) {
            K k = it.next();
            id2K.put(i, k);
            k2Id.put(k, i);
            ++i;
        }

        if (invg != null)
            invg.findReversePostOrder();
    }

    public NodeDominance(Graphdua graph, Graphdua invgraph) {
        this.graphdua = graph;
        this.invgraphdua = invgraph;

        if (graphdua.getrPostOrderArray() == null)
            graphdua.findReversePostOrder();

        Iterator<Node> it = graphdua.iterator();

        int i = 0;

        while (it.hasNext()) {
            Node k = it.next();
            id2Node.put(i, k);
            node2Id.put(k, i);
            ++i;
        }

        if (invgraph != null)
            if (invgraph.getrPostOrderArray() == null)
                invgraphdua.findReversePostOrder();
    }

    // TODO: code below needs to be tested
    public BitSet[] findDominance(Flowgraph<K> g) {
        // Size of the graph
        final int n = g.size();
        BitSet temp = new BitSet(n);
        dom = new BitSet[n];

        int firstnode = k2Id.get(g.entry());

        dom[firstnode] = new BitSet(n);
        dom[firstnode].set(firstnode);

        for (int i = 0; i < n; i++) {

            if (i != firstnode) {
                dom[i] = new BitSet(n);
                dom[i].set(0, n);
            }
        }

        boolean change = true;

        while (change) {
            change = false;
            Iterator<K> it = g.iterator();

            while (it.hasNext()) {
                K k = it.next();
                int i = k2Id.get(k);

                if (i == firstnode) {
                    continue;
                }
                temp.set(0, n);

                for (K nodeK : g.revNeighbors(id2K.get(i).id())) {
                    int p = k2Id.get(nodeK);
                    temp.and(dom[p]);
                }
                temp.set(i);

                if (!temp.equals(dom[i])) {
                    dom[i].clear();
                    dom[i].or(temp);
                    change = true;
                }
            }
        }
        return dom;
    }

    // TODO: code below needs to be tested
    public BitSet[] findDominators() {
        dom = findDominance(g);
        return dom;
    }

    // TODO: test the code below
    public BitSet[] findPostDominators() {
        posdom = findDominance(invg);
        // A node doesn't post dominate itself. They should be removed from the posdom sets.
        Iterator<K> it = invg.iterator();
        while (it.hasNext()) {
            K nodeK = it.next();
            posdom[getIdFromK(nodeK)].clear(getIdFromK(nodeK));
        }
        return posdom;
    }

    public BitSet[] findDominanceGraphdua() {
        // Size of the graph
        Graphdua gd = this.graphdua;
        final int n = gd.size();

        BitSet temp = new BitSet(n);
        dom = new BitSet[n];

        int firstnode = node2Id.get(gd.entry());

        dom[firstnode] = new BitSet(n);
        dom[firstnode].set(firstnode);

        for (int i = 0; i < n; i++) {
            if (i != firstnode) {
                dom[i] = new BitSet(n);
                dom[i].set(0, n);
            }
        }

        boolean change = true;

        while (change) {
            change = false;
            Iterator<Node> it = gd.iterator();

            while (it.hasNext()) {
                Node k = it.next();
                int i = node2Id.get(k);

                if (i == firstnode) {
                    continue;
                }
                temp.set(0, n);

                for (Node nodeK : gd.predecessors(k)) {
                    int p = node2Id.get(nodeK);
                    temp.and(dom[p]);
                }
                temp.set(i);

                if (!temp.equals(dom[i])) {
                    dom[i].clear();
                    dom[i].or(temp);
                    change = true;
                }
            }
        }
        return dom;
    }

    public boolean isDominatorInGraphdua(Node dominator, Node dominated) {
        int idDominator = node2Id.get(dominator);
        int idDominated = node2Id.get(dominated);
        return dom[idDominated].get(idDominator);
    }

    public boolean isDominatedInGraphdua(Node dominated, Node dominator) {
        int idDominator = node2Id.get(dominator);
        int idDominated = node2Id.get(dominated);
        return dom[idDominated].get(idDominator);
    }

    public String toStringGraphduaDominance() {
        StringBuffer sb = new StringBuffer();
        Iterator<Node> it = graphdua.iterator();

        while (it.hasNext()) {
            Node n = it.next();
            sb.append(n.block().id + "(" + n.idSubgraph() + ") : ");
            int idDominated = node2Id.get(n);
            BitSet dominator = dom[idDominated];
            int idDominator = -1;
            while ((idDominator = dominator.nextSetBit(idDominator + 1)) != -1) {
                Node d = id2Node.get(idDominator);
                sb.append(d.block().id + "(" + d.idSubgraph() + "); ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    // TODO: code below needs to be tested
    public K getKFromId(int n) {
        return id2K.get(n);
    }

    // TODO: code below needs to be tested
    public int getIdFromK(K b) {
        return k2Id.get(b);
    }
}
