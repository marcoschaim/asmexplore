package br.usp.each.saeg.subsumption.graphdua;

import java.util.Objects;

import br.usp.each.saeg.opal.Identifiable;
import br.usp.each.saeg.opal.Graph;

public class Flowgraph<K extends Identifiable> extends Graph<K> {

    protected K entry;
    protected K exit;

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
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Flowgraph<?>)) return false;

        Flowgraph<K> graph = (Flowgraph<K>) obj;
        return Objects.equals(graph.entry, this.entry) &&
                Objects.equals(graph.exit, this.exit)
//                &&
//                Objects.equals(graph.nodes, this.nodes) &&
//                Objects.equals(graph.edges, this.edges)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entry, this.exit
                //, this.nodes, this.edges
        );
    }

}