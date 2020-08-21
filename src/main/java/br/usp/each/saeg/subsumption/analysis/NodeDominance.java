package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.opal.Identifiable;
import br.usp.each.saeg.subsumption.graphdua.Flowgraph;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class NodeDominance<K extends Identifiable> {
    private final Flowgraph<K> g;
    private final Flowgraph<K> invg;
    private final Map<Integer, K> id2K = new LinkedHashMap<Integer, K>();
    private final Map<K, Integer> k2Id = new LinkedHashMap<K, Integer>();
    private BitSet[] dom;
    private BitSet[] posdom;

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

        invg.findReversePostOrder();
    }

    public BitSet[] findDominance(Flowgraph<K> g) {
        // Size of the graph
        final int n = g.size();
        BitSet temp = new BitSet(n);
        BitSet[] dom;

        int firstnode = k2Id.get(g.entry());

        dom = new BitSet[n];

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

    public BitSet[] findDominators() {
        dom = findDominance(g);
        return dom;
    }

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

    public K getKFromId(int n) {
        return id2K.get(n);
    }

    public int getIdFromK(K b) {
        return k2Id.get(b);
    }
}
