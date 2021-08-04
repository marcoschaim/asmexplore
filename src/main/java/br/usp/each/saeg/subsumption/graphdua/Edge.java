package br.usp.each.saeg.subsumption.graphdua;

import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Identifiable;

public class Edge implements Identifiable {

    static int id;
    private final Block org;
    private final Block trg;

    public Edge(Block org, Block trg) {
        this.org = org.clone();
        this.trg = trg.clone();
        id = hashCode();
    }

    @Override

    public int id() {
        return id;
    }

    @Override

    public int hashCode() {
        return (org.id() + 1031 * trg.id()) / 511;
    }

    @Override
    public boolean equals(Object o) {

        Edge e = (Edge) o;
        if (o == null)
            return false;
        else
            return org.id() == e.getOrg().id() &&
                    trg.id() == e.getTrg().id();
    }

    @Override
    public String toString() {
        return "(" + org.id() + "," + trg.id() + ")";
    }

    public Block getOrg() {
        return org;
    }

    public Block getTrg() {
        return trg;
    }
}
