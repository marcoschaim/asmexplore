package br.usp.each.saeg.subsumption.graphdua;

import br.usp.each.saeg.opal.Identifiable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class Subgraph<K extends Identifiable> extends Flowgraph<K> implements Identifiable {

    public final int id;

    public Subgraph(int id) {
        this.id = id;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Subgraph<?>)) return false;

        Subgraph<K> sg = (Subgraph<K>) obj;
        return sg.id() == this.id &&
                Objects.equals(sg.entry(), this.entry) &&
                Objects.equals(sg.exit(), this.exit)
//                &&
//                Objects.equals(sg.nodes, this.nodes) &&
//                Objects.equals(sg.edges, this.edges)
                ;
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.id, this.entry, this.exit
                //, this.nodes, this.edges
                );
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<K> i = iterator();

        sb.append("SG");
        sb.append(this.id);
        sb.append("\n");
//        sb.append("(");
//        sb.append(this.entry);
//        sb.append(",");
//        sb.append(this.exit);
//        sb.append("):\n");

        while (i.hasNext()) {
            K k = i.next();
            sb.append(k);
            sb.append(" -> ");

            Set<K> neighbors = neighbors(k.id());
            for (K kn : neighbors) {
                sb.append(kn);
                sb.append(" ");
            }

            sb.append('\n');
        }

        return sb.toString();
    }
    
}