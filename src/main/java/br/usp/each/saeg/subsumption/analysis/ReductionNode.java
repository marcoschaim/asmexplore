package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.opal.Identifiable;
import br.usp.each.saeg.subsumption.graphdua.Dua;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ReductionNode implements Identifiable {
    private static int idcounter = 0;
    private int id;
    private List<Dua> listDuas;

    @Override
    public int id() {
        return id;
    }

    public ReductionNode(){
        listDuas = new LinkedList<>();
        this.id = idcounter++;
    }

    public void addDua(Dua d) {
        listDuas.add(d);
    }

    public List<Dua> getListDuas() { return listDuas; }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Dua> i = this.getListDuas().iterator();

        sb.append("Reduction Node("+this.id()+"):\n");


        while (i.hasNext()) {
            Dua k = i.next();
            sb.append("\t");
            sb.append(k);
            sb.append('\n');
        }

        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SubsumptionNode)) return false;

        ReductionNode r = (ReductionNode) obj;
        return this.id() == r.id();
    }
}
