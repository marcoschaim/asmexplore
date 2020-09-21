package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.opal.Graph;
import br.usp.each.saeg.opal.Program;
import br.usp.each.saeg.subsumption.graphdua.Dua;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SubsumptionGraph extends Graph<SubsumptionNode> {
    private SubsumptionAnalyzer analyzer;
    private BitSet[] subsumptionVector;

    public SubsumptionGraph() {

    }
    public SubsumptionGraph(Program p, List<Dua> listDuas) {
        analyzer = new SubsumptionAnalyzer(p, listDuas);
        subsumptionVector = analyzer.findAllDuaSubsumption();

        Iterator<Dua> itDua = listDuas.iterator();

        while (itDua.hasNext()) {
            addNode(itDua.next());
        }

        itDua = listDuas.iterator();

        while (itDua.hasNext()) {
            Dua d = itDua.next();

            int idDua = analyzer.getDuaId(d);
            BitSet subsumed = subsumptionVector[idDua];

            if (subsumed != null) {
                if (!subsumed.isEmpty()) {
                    int idSubDua = -1;
                    while ((idSubDua = subsumed.nextSetBit(idSubDua + 1)) != -1) {
                        Dua sub = analyzer.getDuaFromId(idSubDua);
                        addEgde(sub, d);
                    }
                } else
                    System.out.println("Warning: dua does not subsume itself:" + d.toString());
            } else
                System.out.println("Warning: Subsumption vector is null for dua:" + d.toString());
        }
    }

    private void addNode(Dua d) {
        this.add(new SubsumptionNode(d));
    }

    private void addEgde(Dua fromDua, Dua toDua) {
        int from, to;
        from = fromDua.hashCode();
        to = toDua.hashCode();
        this.addEdge(from, to);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<SubsumptionNode> i = this.iterator();

        sb.append("Subsumption Graph: \n");


        while (i.hasNext()) {
            SubsumptionNode k = i.next();
            sb.append(k);
            sb.append(" -> \n\t");

            Set<SubsumptionNode> neighbors = this.neighbors(k.id());
            for (SubsumptionNode kn : neighbors) {
                sb.append(kn);
                sb.append(" ");
            }

            sb.append('\n');
        }

        return sb.toString();
    }

    public BitSet[] getSubsumptionVector() {
        return subsumptionVector;
    }

    public int getDuaId(Dua d) {
        return analyzer.getDuaId(d);
    }

    public SubsumptionAnalyzer getSubsumptionAnalyzer() {
        return analyzer;
    }

}
