package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.opal.Identifiable;
import br.usp.each.saeg.subsumption.graphdua.Dua;

public class SubsumptionNode implements Identifiable {
    private int id = 0;
    private int index = -1;
    private int lowindex = -1;

    private Dua dua;

    @Override
    public int id() {
        return id;
    }

    public SubsumptionNode(Dua d) {
        this.id = d.hashCode();
        dua = d;
    }

    public SubsumptionNode() {
    }

    public void setId(int id) { this.id = id; }

    public void setDua(Dua d) {
        dua = d;
        id = d.hashCode();
    }

    public Dua getDua() {
        return dua;
    }

    public String toString() {
        return dua.toString();
    }

    public boolean isUndefined() {
        return (index == -1);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public void setLowIndex(int index) {
        this.lowindex = index;
    }

    public int getLowIndex() {
        return this.lowindex;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SubsumptionNode)) return false;

        SubsumptionNode s = (SubsumptionNode) obj;
        return this.id() == s.id();
    }
}