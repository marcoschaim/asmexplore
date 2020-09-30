package br.usp.each.saeg.subsumption.graphdua;

import br.usp.each.saeg.opal.Graph;
import br.usp.each.saeg.opal.Identifiable;

import java.util.*;

public class Flowgraph<K extends Identifiable> extends Graph<K> {

    protected K entry;
    protected K exit;
    private final Map<Integer, K> localnodes = new HashMap<Integer, K>();
    private final Map<K, Set<K>> localedges = new HashMap<K, Set<K>>();
    private final Map<K, Set<K>> revedges = new HashMap<K, Set<K>>();
    private final Stack<K> rPostOrderListing = new Stack<>();
    int[] rPostOrder;
    int[] rPostOrder2;

    public void setEntry(K k) {
        if (get(k.id()) == null) {
            add(k);
        }
        this.entry = k;
    }

    public void setEntry(int id) {
        K k = get(id);
        if (k == null) return;

        this.entry = k;
    }

    public void setExit(K k) {
        if (get(k.id()) == null) {
            add(k);
        }

        this.exit = k;
    }

    public void setExit(int id) {
        K k = get(id);
        if (k == null) return;

        this.exit = k;
    }

    public K entry() {
        return this.entry;
    }

    public K exit() {
        return this.exit;
    }

    @Override
    public K get(final int id) {
        return localnodes.get(id);
    }

    @Override
    public boolean add(final K k) {
        if (!localnodes.containsKey(k.id())) {
            localnodes.put(k.id(), k);
            localedges.put(k, new LinkedHashSet<K>());
            revedges.put(k, new LinkedHashSet<K>());
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(final int id) {
        final K k = localnodes.get(id);

        if (k != null) {
            // verify  edges
            if (localedges.get(k).isEmpty() && revedges.get(k).isEmpty()) {
                localnodes.remove(id);
                localedges.remove(k);
                revedges.remove(k);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean adjacent(final int from, final int to) {
        final K fromNode, toNode;

        fromNode = localnodes.get(from);
        if (fromNode == null) {
            return false;
        }

        toNode = localnodes.get(to);
        if (toNode == null) {
            return false;
        }

        return localedges.get(fromNode).contains(toNode);
    }

    @Override
    public Set<K> neighbors(final int id) {
        final K k = localnodes.get(id);

        if (k == null) {
            return null;
        }

        return localedges.get(k);
    }

    public Set<K> revNeighbors(final int id) {
        final K k = localnodes.get(id);

        if (k == null) {
            return null;
        }

        return revedges.get(k);
    }

    @Override
    public boolean addEdge(final int from, final int to) {
        final K fromNode, toNode;

        fromNode = localnodes.get(from);
        if (fromNode == null) {
            return false;
        }

        toNode = localnodes.get(to);
        if (toNode == null) {
            return false;
        }

        return localedges.get(fromNode).add(toNode) && revedges.get(toNode).add(fromNode);
    }

    @Override
    public boolean removeEdge(final int from, final int to) {
        final K fromNode, toNode;

        fromNode = localnodes.get(from);
        if (fromNode == null) {
            return false;
        }

        toNode = localnodes.get(to);
        if (toNode == null) {
            return false;
        }

        return (localedges.get(fromNode).remove(toNode) && revedges.get(toNode).remove(fromNode));
    }

    @Override
    public Flowgraph<K> inverse() {
        final Flowgraph<K> g = new Flowgraph<K>();
        for (final K node : this) {
            g.add(node);
        }
        for (final K from : this) {
            for (final K to : localedges.get(from)) {
                g.addEdge(from.id(), to.id());
            }
        }
        return g;
    }

    @Override
    public int size() {
        return localnodes.size();
    }

    public int sizeEdges() {
        int total = 0;
        for (Set<K> list : localedges.values()) {
            total += list.size();
        }
        return total;
    }

    @Override
    public Iterator<K> iterator() {
        return localedges.keySet().iterator();
    }

    public Iterator<K> rPostOrderIterator() {
        if (rPostOrder == null) {
            findReversePostOrder();
        }
        return rPostOrderListing.iterator();
    }

    // Find rPostOrder
    public void findReversePostOrder() {

        final int n = size();
        rPostOrder = new int[n];
        rPostOrder2 = new int[n];

        BitSet visited = new BitSet(n);

        for (int j : rPostOrder)
            rPostOrder[j] = Integer.MIN_VALUE; // This value indicates unreachable block
        for (int j : rPostOrder2)
            rPostOrder2[j] = Integer.MIN_VALUE; // This value indicates unreachable block

        DFS(rPostOrder, rPostOrder2, entry(), n - 1, visited);
    }

    private int DFS(int[] rpo, int[] rpo2, K blk, int i, BitSet visited) {

        int x = 0;

        if (blk != null) {
            x = blk.id();
        }

        visited.set(x);
        for (K blksuc : neighbors(blk.id())) {
            int suc = blksuc.id();
            if (!visited.get(suc)) {
                i = DFS(rpo, rpo2, blksuc, i, visited);
            }
        }

        rpo[x] = i;
        rpo2[i] = x;
        if (blk != null)
            rPostOrderListing.push(blk); // TODO: check if the iterator will work ok.

        return i - 1;
    }


    public Stack<K> getrPostOrderListing() {
        return rPostOrderListing;
    }

    public boolean isRetreatingEdge(K from, K to) {
        return rPostOrder[from.id()] >= rPostOrder[to.id()];
    }

    public void printReversePostOrder() {
        if (rPostOrder != null) {
            for (int i : rPostOrder) {
                System.out.print("[" + i + "]" + " = " + rPostOrder[i] + "; ");
            }
            System.out.println();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Flowgraph<?>)) return false;

        Flowgraph<K> graph = (Flowgraph<K>) obj;
        return Objects.equals(graph.entry, this.entry) &&
                Objects.equals(graph.exit, this.exit) &&
                Objects.equals(graph.localnodes, this.localnodes) &&
                Objects.equals(graph.localedges, this.localedges) &&
                Objects.equals(graph.revedges, this.revedges)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entry, this.exit
                , this.localnodes, this.localedges, this.revedges
        );
    }

}