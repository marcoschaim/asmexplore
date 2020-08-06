package br.usp.each.saeg.subsumption.graphdua;


import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Graph;
import br.usp.each.saeg.subsumption.analysis.SubsumptionAnalyzer;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class Graphdua extends Graph<Node> {

    private final Dua<Block> dua;
    private final Node entryNode;
    private final Node exitNode;


    public Graphdua(Dua<Block> dua, Subgraph<Node> sg1, Subgraph<Node> sg2, Subgraph<Node> sg3, Subgraph<Node> sg4, Subgraph<Node> sg5) {
        this.dua = dua;
        //System.out.println(sg1);
//        createNodesEdgesGraphDua(sg1);
//        //System.out.println(sg2);
//        createNodesEdgesGraphDua(sg2);
//        //System.out.println(sg3);
//        createNodesEdgesGraphDua(sg3);
//        //System.out.println(sg4);
//        createNodesEdgesGraphDua(sg4);
//        //System.out.println(sg5);
//        createNodesEdgesGraphDua(sg5);


        addNodesFromSubgraph(sg1);

        addNodesFromSubgraph(sg2);

        addNodesFromSubgraph(sg3);
        //cleanUpDanglingPaths(sg3);

        addNodesFromSubgraph(sg4);

        addNodesFromSubgraph(sg5);
        //cleanUpDanglingPaths(sg5);

        connectSubgraphs(sg1, sg2);
        connectSubgraphs(sg1, sg3);
        connectSubgraphs(sg2, sg3);
        connectSubgraphs(sg3, sg4);
        connectSubgraphs(sg3, sg5);
        connectSubgraphs(sg4, sg5);


        if (sg1 != null)
            entryNode = this.getNode(sg1.entry().block().id(), 1);
        else
            entryNode = this.getNode(sg3.entry().block().id(), 3);

        if (sg5 != null)
            exitNode = this.getNode(sg5.exit().block().id(), 5);
        else
            exitNode = this.getNode(sg3.exit().block().id(), 3);
    }


    private void addNodesFromSubgraph(Subgraph<Node> sg) {
        if (sg == null) return;

        final Iterator<Node> it = sg.iterator();
        while (it.hasNext()) {
            final Node ni = it.next();
            this.add(ni.clone());

            final Set<Node> neighbors = sg.neighbors(ni.id());
            for (Node nj : neighbors) {
                this.add(nj.clone());
                this.addEdge(ni.id(), nj.id());
            }
        }
    }

    private void connectSubgraphs(Subgraph<Node> sg1, Subgraph<Node> sg2) {
        if (sg1 == null || sg2 == null) return;

        final Node exitNode = sg1.exit();
        final Node entryNode = sg2.entry();

        final Set<Node> successors = sg2.neighbors(entryNode.id());
        for (Node n : successors) {
            this.addEdge(exitNode.id(), n.id());
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(this);
        //Objects.hash(this.nodes, this.edges);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Node> i = this.iterator();
        sb.append("Graphdua");
        sb.append(dua);
        sb.append(":\n");

        while (i.hasNext()) {
            Node k = i.next();
            sb.append(k);
            sb.append("(");
            sb.append(k.idSubgraph());
            sb.append(")");
            sb.append(" -> ");

            Set<Node> neighbors = this.neighbors(k.id());
            for (Node kn : neighbors) {
                sb.append(kn);
                sb.append("(");
                sb.append(kn.idSubgraph());
                sb.append(")");
                sb.append(" ");
            }

            sb.append('\n');
        }

        return sb.toString();
    }

    public String toDot() {
        final StringBuilder sb = new StringBuilder();
        Iterator<Node> i = this.iterator();

        sb.append("digraph { /* ");
        sb.append(dua);
        sb.append(" */\n");

        while (i.hasNext()) {
            Node k = i.next();
            sb.append(k.id());
            sb.append(" [label=\"");
            sb.append(k.block().id());
            sb.append("(");
            sb.append(k.idSubgraph());
            sb.append(")\"];");
            sb.append("\n");
        }

        i = this.iterator();
        while (i.hasNext()) {
            Node k = i.next();

            Set<Node> neighbors = this.neighbors(k.id());
            for (Node kn : neighbors) {
                sb.append(" ");
                sb.append(k.id());
                sb.append(" -> ");
                sb.append(kn.id());
                sb.append(";\n");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    public String toDotNodeSubsumption(SubsumptionAnalyzer analyzer) {
        final StringBuilder sb = new StringBuilder();
        BitSet allSubsumed = new BitSet(entryNode.getCovered().size());
        allSubsumed.clear();

        Iterator<Node> i = this.iterator();

        sb.append("digraph { /* ");
        sb.append("Duas covered at duas");
        sb.append(" */\n");

        while (i.hasNext()) {
            Node k = i.next();
            BitSet coveredInNode = k.getCovered();
            sb.append(k.id());
            sb.append(" [label=\"");
            sb.append(k.block().id());
            sb.append("\\n");
            if (!coveredInNode.isEmpty()) {
                int idDua = -1;
                while ((idDua = coveredInNode.nextSetBit(idDua + 1)) != -1) {
                    Dua subDua = analyzer.getDuaFromId(idDua);
                    sb.append(subDua.toString());
                    sb.append("\\n");
                }
                allSubsumed.or(coveredInNode);
            }
            sb.append("\"];");
            sb.append("\n");
        }

        i = this.iterator();
        while (i.hasNext()) {
            Node k = i.next();

            Set<Node> neighbors = this.neighbors(k.id());
            for (Node kn : neighbors) {
                sb.append(" ");
                sb.append(k.id());
                sb.append(" -> ");
                sb.append(kn.id());
                sb.append(";\n");
            }
        }
        sb.append('}');

        sb.append("\n/*\n");
        sb.append("#Covered Duas by nodes: ");
        sb.append(allSubsumed.cardinality());
        sb.append("\n*/");

        return sb.toString();
    }

    public BitSet getDuasSubsumedEdge(Node org, Node trg) {
        BitSet subsumed = new BitSet(org.getCovered().size());
        subsumed.clear();

        for (Node suc : this.neighbors(org.id())) {
            if (suc.equals(trg)) {
                subsumed.or(org.getOut());
                subsumed.andNot(org.getSleepy());
                subsumed.and(trg.getGen());
                subsumed.or(trg.getCovered());
            }
        }


        return subsumed;
    }

    public String toDotEdgeSubsumption(SubsumptionAnalyzer analyzer) {
        final StringBuilder sb = new StringBuilder();
        BitSet allSubsumed = new BitSet(entryNode.getCovered().size());
        allSubsumed.clear();

        Iterator<Node> i = this.iterator();

        sb.append("digraph { /* ");
        sb.append("Duas covered at edges");
        sb.append(" */\n");

        while (i.hasNext()) {
            Node k = i.next();

            sb.append(k.id());
            sb.append(" [label=\"");
            sb.append(k.block().id());
            sb.append("\"];");
            sb.append("\n");
        }

        i = this.iterator();
        while (i.hasNext()) {
            Node k = i.next();

            Set<Node> neighbors = this.neighbors(k.id());
            for (Node kn : neighbors) {
                BitSet coveredInEdge = getDuasSubsumedEdge(k, kn);
                sb.append(" ");
                sb.append(k.id());
                sb.append(" -> ");
                sb.append(kn.id());
                if (!coveredInEdge.isEmpty()) {
                    sb.append(" [label=\"");
                    int idDua = -1;
                    while ((idDua = coveredInEdge.nextSetBit(idDua + 1)) != -1) {
                        Dua subDua = analyzer.getDuaFromId(idDua);
                        sb.append(subDua.toString());
                        sb.append("\\n");
                    }
                    sb.append("\"];\n");
                    allSubsumed.or(coveredInEdge);
                } else
                    sb.append(";\n");
            }
        }
        sb.append('}');

        sb.append("\n/*\n");
        sb.append("#Covered Duas by edges: ");
        sb.append(allSubsumed.cardinality());
        sb.append("\n*/");

        return sb.toString();
    }

    public Node getNode(int id, int idsubgrph) {
        int idNode = Node.hash(id, idsubgrph);
        return this.get(idNode);
    }

    public Node entry() {
        return entryNode;
    }

    public Node exit() {
        return exitNode;
    }
}