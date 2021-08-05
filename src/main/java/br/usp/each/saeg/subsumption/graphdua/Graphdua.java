package br.usp.each.saeg.subsumption.graphdua;


import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Graph;
import br.usp.each.saeg.subsumption.analysis.SubsumptionAnalyzer;

import java.util.*;

public class Graphdua extends Graph<Node> {

    private final Dua<Block> dua;
    private final Node entryNode;
    private final Node exitNode;
    private final Map<Node, Set<Node>> outedges = new LinkedHashMap<Node, Set<Node>>();
    private final Map<Node, Set<Node>> outrevedges = new LinkedHashMap<Node, Set<Node>>();
    private final Map<Node, Integer> node2bit = new HashMap<>();
    private final List<Node> rPostOrderList = new LinkedList<Node>();
    Subgraph<Node> sgn1, sgn2, sgn3, sgn4, sgn5;
    int size = 0;
    Node[] rPostOrderArray;
    private int[] rPostOrder;

    public Graphdua(Dua<Block> dua, Subgraph<Node> sg1, Subgraph<Node> sg2, Subgraph<Node> sg3, Subgraph<Node> sg4, Subgraph<Node> sg5) {
        this.dua = dua;
        this.sgn1 = sg1;
        this.sgn2 = sg2;
        this.sgn3 = sg3;
        this.sgn4 = sg4;
        this.sgn5 = sg5;

        mapNodesFromSubgraph(sgn1);

        mapNodesFromSubgraph(sgn2);

        mapNodesFromSubgraph(sgn3);

        mapNodesFromSubgraph(sgn4);

        mapNodesFromSubgraph(sg5);

        connectSubgraphs(sgn1, sgn2);
        connectSubgraphs(sgn1, sgn3);
        connectSubgraphs(sgn2, sgn3);
        connectSubgraphs(sgn3, sgn4);
        connectSubgraphs(sgn3, sgn5);
        connectSubgraphs(sgn4, sgn5);


        if (sg1 != null)
            entryNode = sgn1.entry();
        else
            entryNode = sgn3.entry();

        if (sg5 != null)
            exitNode = sgn5.exit();
        else
            exitNode = sgn3.exit();

        findReversePostOrder();
    }

    private void mapNodesFromSubgraph(Subgraph<Node> sg) {
        if (sg == null) return;

        final Iterator<Node> it = sg.iterator();
        while (it.hasNext()) {
            final Node ni = it.next();
            node2bit.put(ni, size);
            size++;
        }
    }

    private void connectSubgraphs(Subgraph<Node> sg1, Subgraph<Node> sg2) {
        if (sg1 == null || sg2 == null) return;

        final Node exitNode = sg1.exit();
        final Node entryNode = sg2.entry();

//        System.out.println("Forward edges:");
        final Set<Node> successors = sg2.neighbors(entryNode.id());
        for (Node n : successors) {

            if (outedges.get(exitNode) == null) {
                exitNode.setOutSuc(true);
                outedges.put(exitNode, new LinkedHashSet<Node>());
                if (!sg1.neighbors(exitNode.id()).isEmpty()) {
                    for (Node suc : sg1.neighbors((exitNode.id()))) {
                        outedges.get(exitNode).add(suc);
                        //System.out.println("("+exitNode+","+suc+")");
//                        System.out.println("("+exitNode+"(" + exitNode.idSubgraph()+") -> "+suc+"("+ suc.idSubgraph()+"))");
//                        System.out.println("("+exitNode.hashCode()+" -> "+suc.hashCode()+"))");
                    }
                }
            }

            outedges.get(exitNode).add(n);
            //System.out.println("("+exitNode+","+n+")");
//            System.out.println("("+exitNode+"(" + exitNode.idSubgraph()+") -> "+n+"("+ n.idSubgraph()+"))");
//            System.out.println("("+exitNode+" -> "+n+"))");

//            System.out.println("Backward edges:");
            if (outrevedges.get(n) == null) {
                n.setOutPred(true);
                outrevedges.put(n, new LinkedHashSet<Node>());
                if (!sg2.revNeighbors(n.id()).isEmpty()) {
                    for (Node pred : sg2.revNeighbors((n.id()))) {
                        if (!predecessors(pred).isEmpty())
                            outrevedges.get(n).add(pred);
                        //System.out.println("("+n+","+pred+")");
//                        System.out.println("("+pred+"("+ pred.idSubgraph()+") <- "+n+"(" + n.idSubgraph()+"))");
//                        System.out.println("("+pred+" <- "+n+"))");
                    }
                }
            }

            outrevedges.get(n).add(exitNode);
//            System.out.println("("+exitNode+"("+ exitNode.idSubgraph()+") <- "+n+"(" + n.idSubgraph()+"))");
//            System.out.println("("+exitNode+" <- "+n+"))");
        }
    }

    @Override
    public Set<Node> neighbors(final int id) {
        Node n = null;

        if (sgn1 != null) {
            n = sgn1.get(id);
        }

        if (n == null && sgn2 != null) {
            n = sgn2.get(id);
        }

        if (n == null && sgn3 != null) {
            n = sgn3.get(id);
        }

        if (n == null && sgn4 != null) {
            n = sgn4.get(id);
        }

        if (n == null && sgn5 != null) {
            n = sgn5.get(id);
        }

        return sucessors(n);
    }

    public Set<Node> revNeighbors(final int id) {
        Node n = null;

        if (sgn1 != null) {
            n = sgn1.get(id);
        }

        if (n == null && sgn2 != null) {
            n = sgn2.get(id);
        }

        if (n == null && sgn3 != null) {
            n = sgn3.get(id);
        }

        if (n == null && sgn4 != null) {
            n = sgn4.get(id);
        }

        if (n == null && sgn5 != null) {
            n = sgn5.get(id);
        }

        return predecessors(n);
    }

    public Set<Node> sucessors(Node n) {
        if (n == null)
            return null;

        if (n.getOutSuc()) {
            return outedges.get(n);
        } else {
            switch (n.idSubgraph()) {
                case 1:
                    return sgn1.neighbors(n.id());
                case 2:
                    return sgn2.neighbors(n.id());
                case 3:
                    return sgn3.neighbors(n.id());
                case 4:
                    return sgn4.neighbors(n.id());
                case 5:
                    return sgn5.neighbors(n.id());
            }
        }

        return null;
    }

    public Set<Node> predecessors(Node n) {
        if (n == null)
            return null;

        if (n.getOutPred()) {
            return outrevedges.get(n);
        } else {
            switch (n.idSubgraph()) {
                case 1:
                    return sgn1.revNeighbors(n.id());
                case 2:
                    return sgn2.revNeighbors(n.id());
                case 3:
                    return sgn3.revNeighbors(n.id());
                case 4:
                    return sgn4.revNeighbors(n.id());
                case 5:
                    return sgn5.revNeighbors(n.id());
            }
        }

        return null;
    }


    public Node[] getrPostOrderArray() {
        return rPostOrderArray;
    }

    public boolean isRetreatingEdge(Node from, Node to) {
        return rPostOrder[node2bit.get(from)] >= rPostOrder[node2bit.get(to)];
    }

    public Iterator<Node> iterator() {
        return rPostOrderList.iterator();
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

    public Node exitSG3() {
        return sgn3.exit();
    }

    @Override
    public int size() {
        return size;
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

    public BitSet getAllDuasSubsumedNode(SubsumptionAnalyzer analyzer) {
        BitSet allSubsumed = new BitSet(entryNode.getCovered().size());
        allSubsumed.clear();

        Iterator<Node> i = this.iterator();

        while (i.hasNext()) {
            Node k = i.next();
            allSubsumed.or(k.getCovered());
        }
        return allSubsumed;
    }

    public BitSet getDuasSubsumedEdge(Node org, Node trg) {
        BitSet subsumed = new BitSet(org.getCovered().size());
        subsumed.clear();

        if (this.sucessors(org).contains(trg)) {

            if (this.sucessors(org).size() > 1) {
                subsumed.or(org.getOut());
                subsumed.andNot(org.getSleepy());
                subsumed.and(trg.getGen());
                subsumed.or(trg.getCovered());
            } else {
                subsumed.or(org.getCovered());
                subsumed.or(trg.getCovered());
            }

        }

        return subsumed;
    }

    public BitSet getAllDuasSubsumedEdge(SubsumptionAnalyzer analyzer) {
        BitSet allSubsumed = new BitSet(entryNode.getCovered().size());
        allSubsumed.clear();

        Iterator<Node> i = this.iterator();

        while (i.hasNext()) {
            Node k = i.next();

            Set<Node> neighbors = this.neighbors(k.id());
            for (Node kn : neighbors) {
                BitSet coveredInEdge = getDuasSubsumedEdge(k, kn);
                if (!coveredInEdge.isEmpty()) {
                    allSubsumed.or(coveredInEdge);
                }
            }
        }

        return allSubsumed;
    }


    // Find rPostOrder
    public void findReversePostOrder() {

        final int n;
        n = size();
        rPostOrder = new int[n];
        rPostOrderArray = new Node[n];

        BitSet visited = new BitSet(n);

        for (int j : rPostOrder)
            rPostOrder[j] = Integer.MIN_VALUE; // This value indicates unreachable block

//        System.out.println("rPostOrder");
        DFS(entry(), n - 1, visited);

        // Clean up unreacheable node. They values are null in the rPostOrderArray
        int i = 0;
        Node node = rPostOrderArray[i];
        while (node == null) {
            ++i;
            node = rPostOrderArray[i];
        }

        while (i < rPostOrderArray.length) {
//            System.out.println("rPosterOrderArray["+i+"] = "+rPostOrderArray[i]);
            rPostOrderList.add(rPostOrderArray[i]);
            ++i;
        }
    }

    private int DFS(Node node, int i, BitSet visited) {

        int x = 0;

        if (node != null) {
            x = node2bit.get(node);
        }

        visited.set(x);
        for (Node suc : sucessors(node)) {
            int sucid = node2bit.get(suc);
            if (!visited.get(sucid)) {
//                System.out.println(node+"("+ node.idSubgraph()+") -> "+suc+"(" + suc.idSubgraph()+")");
//                System.out.println(node.hashCode()+" -> "+suc.hashCode());
                i = DFS(suc, i, visited);
            }
        }

        rPostOrder[x] = i;
        rPostOrderArray[i] = node;
        return i - 1;
    }


    public void printReversePostOrder() {
        if (rPostOrder != null) {
            for (int i : rPostOrder) {
                System.out.print("[" + i + "]" + " = " + rPostOrder[i] + "; ");
            }
            System.out.println();
        }
    }

}