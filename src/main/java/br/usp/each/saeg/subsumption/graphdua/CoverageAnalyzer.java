package br.usp.each.saeg.subsumption.graphdua;

import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Graph;
import br.usp.each.saeg.opal.Program;

import java.util.*;

public class CoverageAnalyzer {

    private final Program program;
    private final Flowgraph<Block> graph;
    private final Graph<Block> invgraph;
    private final Dua<Block> dua;
    private final int size;

    private Subgraph<Node> sg1, sg2, sg3, sg4, sg5;

    public CoverageAnalyzer(Flowgraph<Block> graph, Dua<Block> dua) {
        this.program = null;
        this.graph = graph;
        this.invgraph = graph.inverse();
        this.dua = dua;
        this.size = graph.size();
    }

    public CoverageAnalyzer(Flowgraph<Block> graph, Flowgraph<Block> invgraph, Dua<Block> dua) {
        this.program = null;
        this.graph = graph;
        this.invgraph = invgraph;
        this.dua = dua;
        this.size = graph.size();
    }

    public CoverageAnalyzer(Program program, Dua<Block> dua) {
        this.program = program;
        this.graph = program.getGraph();
        this.invgraph = program.getInvGraph();
        this.dua = dua;
        this.size = graph.size();
    }

    public Graphdua findGraphdua() {
        findAllSubgraphs();
        return new Graphdua(dua, sg1, sg2, sg3, sg4, sg5);
    }

    public void findAllSubgraphs() {
        if (graph.entry().equals(dua.def())) {
            this.sg1 = null;
            this.sg2 = null;
        } else {
            this.sg1 = findSubgraph(graph.entry(), dua.def(), 1, true);
            this.sg2 = findSubgraph(dua.def(), dua.def(), 2, false);
        }

        if (dua.isCUse()) {
            this.sg3 = findSubgraphDF(dua.def(), dua.use(), dua.var(), 3, true);
        } else {
            this.sg3 = findSubgraphDF(dua.def(), dua.from(), dua.to(), dua.var(), 3, true);
        }

        if (graph.exit().equals(dua.use())) {
            this.sg4 = null;
            this.sg5 = null;
        } else {
            this.sg4 = findSubgraph(dua.use(), dua.use(), 4, false);
            this.sg5 = findSubgraph(dua.use(), graph.exit(), 5, true);
        }
    }

    private boolean findNodesEdgesRec(Flowgraph<Block> grph, BitSet visited, BitSet edges, Block ni, Block nj, Block n) {
        boolean keep = false;
        Set<Block> successors = grph.neighbors(n.id());

        for (Block wsuc : successors) {
            if (ni.equals(nj) || !wsuc.equals(ni)) {

                if (!visited.get(wsuc.id())) {
                    visited.set(wsuc.id());

                    if (!wsuc.equals(nj)) {
                        keep |= findNodesEdgesRec(grph, visited, edges, ni, nj, wsuc);
                        if (!keep) {
                            edges.clear(getEdgeBit(n.id(), wsuc.id()));
                        } else {
                            edges.set(getEdgeBit(n.id(), wsuc.id()));
                        }
                    } else // equals nj
                    {
                        keep = true;
                        edges.set(getEdgeBit(n.id(), wsuc.id()));
                    }
                } else {
                    edges.set(getEdgeBit(n.id(), wsuc.id()));
                    keep = true;
                }
            } else { // ni != nj and wsuc == ni (hit the origin node)
                keep = false;
            }
        }
        if (!keep)
            visited.clear(n.id());
        return keep;
    }

    private BitSet findNodesEdges(BitSet sucbit, BitSet edsucbit, Block ni, Block nj) {
        // Find successors and the edges from ni to nj
        final Queue<Block> w = new LinkedList<>();
        BitSet toBeChecked = new BitSet(sucbit.size());

        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block wsuc : successors) {
                if (ni.equals(nj) || !wsuc.equals(ni)) {
                    edsucbit.set(getEdgeBit(n.id(), wsuc.id()));

                    if (!sucbit.get(wsuc.id())) {
                        sucbit.set(wsuc.id());

                        if (!wsuc.equals(nj)) {
                            w.add(wsuc);
                        }

                    }
                } else { // n_i != n_j && wsuc == n_i
                    // n_i is reachable from n_i. Needs a cleanup
                    //edsucbit.set(getEdgeBit(n.id(), wsuc.id()));
                    toBeChecked.set(n.id());
                    //System.out.println("Cleanup is needed");
                }
            }
        }
        return toBeChecked;
    }

//    private BitSet findDFPredNodesPUse(BitSet predbit, Block ni, Block nj, Block newnk, int x) {
//        final Queue<Block> w = new LinkedList<>();
//        BitSet toBeChecked = new BitSet(predbit.size());
//
//        w.add(newnk);
//        boolean newnkVisited = false;
//
//        while (!w.isEmpty()) {
//            Block n = w.remove();
//            Set<Block> predecessors;
//            if (n.id() < 0) {
//                predecessors = new HashSet<>();
//                predecessors.add(nj);
//            } else
//                predecessors = getPredecessors(n);
//
//            for (Block wpred : predecessors) {
//                if (!wpred.equals(ni) && !wpred.equals(newnk) && wpred.isDef(x)) {
//                    if(wpred.isDef(x))
//                        toBeChecked.set(n.id()); // n should be checked for dangling paths
//                    continue;
//                }
//
//                if (wpred.id() >= 0)
//                    if (!predbit.get(wpred.id())) {
//                        predbit.set(wpred.id());
//                        if (!wpred.equals(ni)) {
//                            w.add(wpred);
//                        }
//                    } else {
//                        if (!newnkVisited) {
//                            w.add(wpred);
//                            newnkVisited = true;
//                        }
//                    }
//            }
//        }
//
//
//        return toBeChecked;
//    }


    public Subgraph<Node> findSubgraph(Block ni, Block nj, int i, boolean mandatory) {
        final Queue<Block> w = new LinkedList<>();
        BitSet sucbit = new BitSet(getGraphSize());
        BitSet predbit = new BitSet(getGraphSize());
        BitSet nodesbit;
        BitSet edsucbit = new BitSet(getGraphSize() * getGraphSize());
        BitSet toBeChecked;

        // Find successors and the edges from ni to nj

//        w.add(ni);
//
//        while (!w.isEmpty()) {
//            Block n = w.remove();
//            Set<Block> successors = getSuccessors(n);
//
//            for (Block wsuc : successors) {
//                if (ni.equals(nj) || !wsuc.equals(ni)) {
//                     edsucbit.set(getEdgeBit(n.id(), wsuc.id()));
//
//                    if (!sucbit.get(wsuc.id())) {
//                        sucbit.set(wsuc.id());
//
//                        if (!wsuc.equals(nj)) {
//                            w.add(wsuc);
//                        }
//
//                    }
//                }
//            }
//        }

        toBeChecked = findNodesEdges(sucbit, edsucbit, ni, nj);

        // Find the predecessors

        final List<Block> pred = new LinkedList<>();

        w.add(nj);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> predecessors = getPredecessors(n);

            for (Block wpred : predecessors) {

                if (ni.equals(nj) || !wpred.equals(nj)) {

                    if (!predbit.get(wpred.id())) {
                        predbit.set(wpred.id());

                        if (!wpred.equals(ni)) {
                            w.add(wpred);
                        }
                    }
                }
            }
        }

        // Build the subgraph

        sucbit.and(predbit);
        nodesbit = sucbit;

        if (!mandatory)
            if (nodesbit.isEmpty()) return null; // Subgraph may not exist

        nodesbit.set(ni.id());
        nodesbit.set(nj.id());

        final Subgraph<Node> sg = new Subgraph<>(i);
        final Node nodeI = new Node(ni, i);

        sg.add(nodeI);
        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block nsuc : successors) {
                if (nodesbit.get(nsuc.id()) && nodesbit.get(n.id()) && edsucbit.get(getEdgeBit(n.id(), nsuc.id()))) {
                    int hash = Node.hash(nsuc.id(), i);
                    Node newNode = sg.get(hash);

                    if (newNode == null) {
                        newNode = new Node(nsuc, i);
                        sg.add(newNode);
                    }

                    hash = Node.hash(n.id(), i);
                    sg.addEdge(hash, newNode.id());
                    edsucbit.clear(getEdgeBit(n.id(), nsuc.id()));

                    w.add(nsuc);
                }
            }
        }

        sg.setEntry(nodeI);
        sg.setExit(sg.get(Node.hash(nj.id(), i)));

        toBeChecked.and(nodesbit);
        toBeChecked.clear(ni.id());
        toBeChecked.clear(nj.id());

        if (!toBeChecked.isEmpty()) {
            //System.out.println("Needs cleanup: "+toBeChecked);
            cleanUpDanglingPaths(sg, toBeChecked);
        }

        return sg;
    }

    private BitSet findDFNodesEdgesCUse(BitSet sucbit, BitSet edsucbit, Block ni, Block nj, int x) {
        // Find successors and the edges from ni to nj
        final Queue<Block> w = new LinkedList<>();

        BitSet toBeChecked = new BitSet(sucbit.size());

        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block wsuc : successors) {
                // x == -2 indicates a fake dua
                if (!wsuc.equals(ni) && !wsuc.equals(nj) && (x != -2 && wsuc.isDef(x))) {
                    if (wsuc.isDef(x))
                        toBeChecked.set(n.id()); // n should be checked for dangling paths
                    continue;
                }

                if (ni.equals(nj) || !wsuc.equals(ni)) {
                    edsucbit.set(getEdgeBit(n.id(), wsuc.id()));

                    if (!sucbit.get(wsuc.id())) {
                        sucbit.set(wsuc.id());

                        if (wsuc.id() != nj.id()) {
                            w.add(wsuc);
                        }
                    }
                } else { // n_i != n_j && wsuc == n_i
                    // n_i is reachable from n_i. Needs a cleanup
                    toBeChecked.set(n.id()); // n should be checked for dangling paths
                    // edsucbit.set(getEdgeBit(n.id(), wsuc.id()));
                    //System.out.println("Cleanup is needed");
                }
            }
        }
        return toBeChecked;
    }

    public Subgraph<Node> findSubgraphDF(Block ni, Block nj, int x, int i, boolean mandatory) {
        final List<Block> suc = new LinkedList<>();
        final Queue<Block> w = new LinkedList<>();
        BitSet sucbit = new BitSet(getGraphSize());
        BitSet predbit = new BitSet(getGraphSize());
        BitSet nodesbit;
        BitSet edsucbit = new BitSet(getGraphSize() * getGraphSize());

        // Find successors and the edges from ni to nj

//        w.add(ni);
//
//        while (!w.isEmpty()) {
//            Block n = w.remove();
//            Set<Block> successors = getSuccessors(n);
//
//            for (Block wsuc : successors) {
//                if (!wsuc.equals(ni) && !wsuc.equals(nj) && wsuc.isDef(x)) {
//                    continue;
//                }
//
//                if (ni.equals(nj) || !wsuc.equals(ni)) {
//                   edsucbit.set(getEdgeBit(n.id(), wsuc.id()));
//
//                    if (!sucbit.get(wsuc.id())) {
//                        sucbit.set(wsuc.id());
//
//                        if (wsuc.id() != nj.id()) {
//                            w.add(wsuc);
//                        }
//                    }
//                }
//            }
//        }


        BitSet toBeChecked = findDFNodesEdgesCUse(sucbit, edsucbit, ni, nj, x);

        // Find the predecessors
        // final List<Block> pred = new LinkedList<>();

        w.add(nj);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> predecessors = getPredecessors(n);

            for (Block wpred : predecessors) {
                // x == -2 indicates a fake dua
                if (!wpred.equals(ni) && !wpred.equals(nj) && (x != -2 && wpred.isDef(x))) {
                    continue;
                }

                if (!predbit.get(wpred.id())) {
                    predbit.set(wpred.id());

                    if (!wpred.equals(ni)) {
                        w.add(wpred);
                    }
                }
            }
        }

        // Build the subgraph

        sucbit.and(predbit);
        nodesbit = sucbit;

        if (!mandatory)
            if (nodesbit.isEmpty()) return null;

        nodesbit.set(ni.id());
        nodesbit.set(nj.id());

        // Weed out def nodes

        final Subgraph<Node> sg = new Subgraph<>(i);
        final Node nodeI = new Node(ni, i);

        sg.add(nodeI);
        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block nsuc : successors) {
                if (nodesbit.get(nsuc.id()) && nodesbit.get(n.id()) &&
                        edsucbit.get(getEdgeBit(n.id(), nsuc.id()))) {
                    int hash = Node.hash(nsuc.id(), i);
                    Node newNode = sg.get(hash);

                    if (newNode == null) {
                        newNode = new Node(nsuc, i);
                        sg.add(newNode);
                    }

                    hash = Node.hash(n.id(), i);
                    sg.addEdge(hash, newNode.id());
                    edsucbit.clear(getEdgeBit(n.id(), nsuc.id()));
                    w.add(nsuc);
                }
            }
        }

        sg.setEntry(nodeI);
        sg.setExit(sg.get(Node.hash(nj.id(), i)));
        //sg.setExit(new Node(nj, i));

        toBeChecked.and(nodesbit);
        toBeChecked.clear(ni.id());
        toBeChecked.clear(nj.id());

        if (!toBeChecked.isEmpty()) {
            //System.out.println("Needs cleanup: "+toBeChecked);
            cleanUpDanglingPaths(sg, toBeChecked);
        }
        return sg;
    }

    private BitSet findDFNodesEdgesPUse(BitSet sucbit, BitSet edsucbit, Block ni, Block newnk, int x) {
        // Find successors and the edges from ni to nj
        final Queue<Block> w = new LinkedList<>();
        BitSet toBeChecked = new BitSet(sucbit.size());

        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block wsuc : successors) {
                if (!wsuc.equals(ni) && !wsuc.equals(newnk) && wsuc.isDef(x)) {
                    if (wsuc.isDef(x))
                        toBeChecked.set(n.id()); // n should be checked for dangling paths
                    continue;
                }

                if (ni.equals(newnk) || !wsuc.equals(ni)) {
                    if (wsuc.id() >= 0)
                        edsucbit.set(getEdgeBit(n.id(), wsuc.id()));

                    if (wsuc.id() >= 0 && !sucbit.get(wsuc.id())) {
                        sucbit.set(wsuc.id());
                        sucbit.set(wsuc.id());
                        w.add(wsuc);
                    }
                } else { // n_i != newnk && wsuc == n_i
                    // n_i is reachable from n_i. Needs a cleanup
                    toBeChecked.set(n.id()); // n should be checked for dangling paths
                    // edsucbit.set(getEdgeBit(n.id(), wsuc.id()));
                    //System.out.println("Cleanup is needed");
                }
            }
        }

        return toBeChecked;
    }

    public Subgraph<Node> findSubgraphDF(Block ni, Block nj, Block nk, int x, int i, boolean mandatory) {
        final List<Block> suc = new LinkedList<>();
        BitSet sucbit = new BitSet(getGraphSize());
        BitSet predbit = new BitSet(getGraphSize());
        BitSet nodesbit;
        BitSet edsucbit = new BitSet(getGraphSize() * getGraphSize());

        final Queue<Block> w = new LinkedList<>();

        // Create new nk

        Block newnk = new Block(-nk.id());

        // Modify the graph by removing edge (nj, nk) and adding edge (nj, -nk).

        graph.removeEdge(nj.id(), nk.id());
        graph.add(newnk);
        graph.addEdge(nj.id(), newnk.id());

        // Find successors and the edges from ni to newnk
        BitSet toBeChecked = findDFNodesEdgesPUse(sucbit, edsucbit, ni, newnk, x);

//        w.add(ni);
//
//        while (!w.isEmpty()) {
//            Block n = w.remove();
//            Set<Block> successors = getSuccessors(n);
//
//            for (Block wsuc : successors) {
//                if (!wsuc.equals(ni) && !wsuc.equals(newnk) && wsuc.isDef(x)) {
//                    continue;
//                }
//
//                if (ni.equals(newnk) || !wsuc.equals(ni)) {
//                    if (wsuc.id() >= 0)
//                        edsucbit.set(getEdgeBit(n.id(), wsuc.id()));
//
//                    if (wsuc.id() >= 0 && !sucbit.get(wsuc.id())) {
//                        sucbit.set(wsuc.id());
//                        sucbit.set(wsuc.id());
//                        w.add(wsuc);
//                    }
//                }
//            }
//        }

        // Find the predecessors

        w.add(newnk);
        boolean newnkVisited = false;

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> predecessors;
            if (n.id() < 0) {
                predecessors = new HashSet<>();
                predecessors.add(nj);
            } else
                predecessors = getPredecessors(n);

            for (Block wpred : predecessors) {
                if (!wpred.equals(ni) && !wpred.equals(newnk) && wpred.isDef(x)) {
                    continue;
                }

                if (wpred.id() >= 0)
                    if (!predbit.get(wpred.id())) {
                        predbit.set(wpred.id());
                        if (!wpred.equals(ni)) {
                            w.add(wpred);
                        }
                    } else {
                        if (!newnkVisited) {
                            w.add(wpred);
                            newnkVisited = true;
                        }
                    }
            }
        }

        // Build the subgraph
        Subgraph<Node> sg = null;

        sucbit.and(predbit);
        nodesbit = sucbit;

        if (!mandatory)
            if (nodesbit.isEmpty())
                return null;

        nodesbit.set(ni.id());

        sg = new Subgraph<>(i);

        final Node nodeI = new Node(ni, i);
        sg.add(nodeI);
        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block nsuc : successors) {
                if ((n.equals(nj) && nsuc.equals(newnk)) || (nodesbit.get(nsuc.id) && nodesbit.get(n.id()) &&
                        edsucbit.get(getEdgeBit(n.id(), nsuc.id())))) {
                    int hash = Node.hash(nsuc.id(), i);
                    Node newNode = sg.get(hash);

                    if (newNode == null) {
                        newNode = new Node(nsuc, i);
                        sg.add(newNode);
                    }

                    hash = Node.hash(n.id(), i);
                    sg.addEdge(hash, newNode.id());

                    if (n.id() >= 0 && nsuc.id() >= 0)
                        edsucbit.clear(getEdgeBit(n.id(), nsuc.id()));

                    w.add(nsuc);
                }
            }
        }

        sg.setEntry(nodeI);
        //sg.setExit(new Node(newnk, i));
        sg.setExit(sg.get(Node.hash(newnk.id(), i)));

        // Recover original graph

        graph.removeEdge(nj.id(), newnk.id());
        graph.addEdge(nj.id(), nk.id());
        graph.remove(newnk.id());

        toBeChecked.and(nodesbit);

        toBeChecked.clear(ni.id());
        toBeChecked.clear(nj.id());

        if (!toBeChecked.isEmpty()) {
            //System.out.println("Needs cleanup: "+toBeChecked);
            cleanUpDanglingPaths(sg, toBeChecked);
        }

        return sg;
    }

    private void cleanUpDanglingPaths(Subgraph<Node> sg, BitSet tobechecked) {
        if (sg == null)
            return;

        BitSet visitedSuspect = new BitSet(sg.size());

        if (!tobechecked.isEmpty()) {
            int nodeid = -1;
            while ((nodeid = tobechecked.nextSetBit(nodeid + 1)) != -1) {
                Node n = sg.get(Node.hash(nodeid, sg.id()));
                if (n != null)
                    visitReverseSubgraph(sg, n, visitedSuspect);
            }
        }

        BitSet visitedSure = new BitSet(sg.size());
        visitReverseSubgraph(sg, sg.exit(), visitedSure);
        visitedSuspect.andNot(visitedSure);

        cleanUpNodes(sg, visitedSuspect);
    }

    private void visitReverseSubgraph(Subgraph<Node> sg, Node n, BitSet visited) {
        boolean visnode;

        if (n.block().id() >= 0)
            visited.set(n.block().id());

        for (Node s : sg.revNeighbors(n.id())) {
            if (s.block().id() < 0) {
                visnode = false;
            } else
                visnode = visited.get(s.block().id());

            if (!visnode)
                visitReverseSubgraph(sg, s, visited);
        }
    }

    private void cleanUpNodes(Subgraph<Node> sg, BitSet nodesout) {
        if (!nodesout.isEmpty()) {
            int nodeid = -1;
            while ((nodeid = nodesout.nextSetBit(nodeid + 1)) != -1) {
                Node nn = sg.get(Node.hash(nodeid, sg.id()));
                Node[] neighbors = new Node[sg.neighbors(nn.id()).size()];
                neighbors = sg.neighbors(nn.id()).toArray(neighbors);

                for (Node suc : neighbors) {
                    if (!sg.removeEdge(nn.id(), suc.id()))
                        System.out.println("Edge(" + nodeid + "," + suc.block().id() + ") not removed!");
                }

                Node[] revneighbors = new Node[sg.revNeighbors(nn.id()).size()];
                revneighbors = sg.revNeighbors(nn.id()).toArray(revneighbors);

                for (Node pred : revneighbors) {
                    if (!sg.removeEdge(pred.id(), nn.id()))
                        System.out.println("Edge(" + pred.block().id() + "," + nodeid + ") not removed!");
                }

                sg.remove(nn.id());
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Graphdua)) return false;

        Graphdua graphdua = (Graphdua) obj;
        return Objects.equals(graphdua, this);
        //Objects.equals(graphdua.nodes, this.nodes) &&
        //Objects.equals(graphdua.edges, this.edges);
    }
    // --- Getters

    private Set<Block> getSuccessors(Block b) {
        return graph.neighbors(b.id());
    }

    private Set<Block> getPredecessors(Block b) {
        return graph.revNeighbors(b.id());
    }

    private int getEdgeBit(int from, int to) {
        return (from * getGraphSize() + to);
    }

    private int getGraphSize() {
        return size;
    }

    public Subgraph<Node> sg1() {
        return this.sg1;
    }

    public Subgraph<Node> sg2() {
        return this.sg2;
    }

    public Subgraph<Node> sg3() {
        return this.sg3;
    }

    public Subgraph<Node> sg4() {
        return this.sg4;
    }

    public Subgraph<Node> sg5() {
        return this.sg5;
    }

    // Pretty printer

    public String toDot(Subgraph<Node> sg) {
        final StringBuilder sb = new StringBuilder();
        Iterator<Node> i = sg.iterator();

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

        i = sg.iterator();
        while (i.hasNext()) {
            Node k = i.next();

            Set<Node> neighbors = sg.neighbors(k.id());
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

}