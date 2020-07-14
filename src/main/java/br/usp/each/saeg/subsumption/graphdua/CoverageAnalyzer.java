package br.usp.each.saeg.subsumption.graphdua;

import br.usp.each.saeg.opal.Block;

import org.apache.commons.collections4.ListUtils;

import java.util.*;

public class CoverageAnalyzer {

    private final Flowgraph<Block> graph;
    private final Dua<Block> dua;

    private Subgraph<Node> sg1, sg2, sg3, sg4, sg5;

    public CoverageAnalyzer(Flowgraph<Block> graph, Dua<Block> dua) {
        this.graph = graph;
        this.dua = dua;
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

    public Subgraph<Node> findSubgraph(Block ni, Block nj, int i, boolean mandatory) {
        final List<Block> suc = new LinkedList<>();
        final Queue<Block> w = new LinkedList<>();
        final Map<Integer, Set<Integer>> edges = new LinkedHashMap<>();
        final Iterator<Block> it = graph.iterator();

        // Initialize edges set

        while (it.hasNext()) {
            Block b = it.next();
            edges.put(b.id(), new LinkedHashSet<Integer>());
        }

        // Find successors and the edges from ni to nj

        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block wsuc : successors) {
                if (ni.equals(nj) || !wsuc.equals(ni)) {
                    edges.get(n.id()).add(wsuc.id());
                }

                if (!suc.contains(wsuc)) {
                    suc.add(wsuc);

                    if (!wsuc.equals(nj)) {
                        w.add(wsuc);
                    }
                }
            }
        }

        // Find the predecessors

        final List<Block> pred = new LinkedList<>();

        w.add(nj);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> predecessors = getPredecessors(n);

            for (Block wpred : predecessors) {
                if (!pred.contains(wpred)) {
                    pred.add(wpred);

                    if (!wpred.equals(ni)) {
                        w.add(wpred);
                    }
                }
            }
        }

        // Build the subgraph

        final List<Block> nodes = ListUtils.intersection(suc, pred);

        if (!mandatory)
            if (nodes.isEmpty()) return null; // Subgraph may not exist

        nodes.add(ni);
        nodes.add(nj);

        final Subgraph<Node> sg = new Subgraph<>(i);
        final Node nodeI = new Node(ni, i);

        sg.add(nodeI);
        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block nsuc : successors) {
                if (nodes.contains(nsuc) && nodes.contains(n) && edges.get(n.id()).contains(nsuc.id())) {
                    int hash = Node.hash(nsuc.id(), i);
                    Node newNode = sg.get(hash);

                    if (newNode == null) {
                        newNode = new Node(nsuc, i);
                        sg.add(newNode);
                    }

                    hash = Node.hash(n.id(), i);
                    sg.addEdge(hash, newNode.id());
                    edges.get(n.id()).remove(nsuc.id());
                    w.add(nsuc);
                }
            }
        }

        sg.setEntry(nodeI);
        sg.setExit(new Node(nj, i));

        return sg;
    }

    public Subgraph<Node> findSubgraphDF(Block ni, Block nj, int x, int i, boolean mandatory) {
        final List<Block> suc = new LinkedList<>();
        final Queue<Block> w = new LinkedList<>();
        final List<Integer> defNodes = new LinkedList<>();
        final Map<Integer, Set<Integer>> edges = new LinkedHashMap<>();
        final Iterator<Block> it = graph.iterator();

        // Initialize edges set && def nodes set

        while (it.hasNext()) {
            Block b = it.next();
            edges.put(b.id(), new LinkedHashSet<Integer>());

            if (b.isDef(x)) {
                defNodes.add(b.id());
            }
        }

        // Find successors and the edges from ni to nj

        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block wsuc : successors) {
                if (!wsuc.equals(ni) && !wsuc.equals(nj) && defNodes.contains(wsuc.id())) {
                    continue;
                }

                if (ni.equals(nj) || !wsuc.equals(ni)) {
                    edges.get(n.id()).add(wsuc.id());
                }

                if (!suc.contains(wsuc)) {
                    suc.add(wsuc);

                    if (wsuc.id() != nj.id()) {
                        w.add(wsuc);
                    }
                }
            }
        }

        // Find the predecessors

        final List<Block> pred = new LinkedList<>();

        w.add(nj);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> predecessors = getPredecessors(n);

            for (Block wpred : predecessors) {
                if (!wpred.equals(ni) && !wpred.equals(nj) && defNodes.contains(wpred.id())) {
                    continue;
                }

                if (!pred.contains(wpred)) {
                    pred.add(wpred);

                    if (!wpred.equals(ni)) {
                        w.add(wpred);
                    }
                }
            }
        }

        // Build the subgraph

        final List<Block> nodes = ListUtils.intersection(suc, pred);

        if (!mandatory)
            if (nodes.isEmpty()) return null;

        nodes.add(ni);
        nodes.add(nj);

        // Weed out def nodes

        final Subgraph<Node> sg = new Subgraph<>(i);
        final Node nodeI = new Node(ni, i);

        sg.add(nodeI);
        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block nsuc : successors) {
                if (nodes.contains(nsuc) && nodes.contains(n) && edges.get(n.id()).contains(nsuc.id())) {
                    int hash = Node.hash(nsuc.id(), i);
                    Node newNode = sg.get(hash);

                    if (newNode == null) {
                        newNode = new Node(nsuc, i);
                        sg.add(newNode);
                    }

                    hash = Node.hash(n.id(), i);
                    sg.addEdge(hash, newNode.id());
                    edges.get(n.id()).remove(nsuc.id());
                    w.add(nsuc);
                }
            }
        }

        sg.setEntry(nodeI);
        sg.setExit(new Node(nj, i));

        return sg;
    }

    public Subgraph<Node> findSubgraphDF(Block ni, Block nj, Block nk, int x, int i, boolean mandatory) {
        final List<Block> suc = new LinkedList<>();
        final Queue<Block> w = new LinkedList<>();
        final List<Integer> defNodes = new LinkedList<>();
        final Map<Integer, Set<Integer>> edges = new LinkedHashMap<>();

        // Create new nk

        Block newnk = new Block(-nk.id());

        // Modify the graph by removing edge (nj, nk) and adding edge (nj, -nk).

        graph.removeEdge(nj.id(), nk.id());
        graph.add(newnk);
        graph.addEdge(nj.id(), newnk.id());

        // Initialize edges set && def nodes set

        final Iterator<Block> it = graph.iterator();

        while (it.hasNext()) {
            Block b = it.next();
            edges.put(b.id(), new LinkedHashSet<Integer>());

            if (b.isDef(x)) {
                defNodes.add(b.id());
            }
        }

        // Find successors and the edges from ni to newnk

        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block wsuc : successors) {
                if (!wsuc.equals(ni) && !wsuc.equals(newnk) && defNodes.contains(wsuc.id())) {
                    continue;
                }

                if (ni.equals(newnk) || !wsuc.equals(ni)) {
                    edges.get(n.id()).add(wsuc.id());
                }

                if (!suc.contains(wsuc)) {
                    suc.add(wsuc);

                    if (wsuc.id() != newnk.id()) {
                        w.add(wsuc);
                    }
                }
            }
        }

        // Find the predecessors

        final List<Block> pred = new LinkedList<>();

        w.add(newnk);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> predecessors = getPredecessors(n);

            for (Block wpred : predecessors) {
                if (!wpred.equals(ni) && !wpred.equals(newnk) && defNodes.contains(wpred.id())) {
                    continue;
                }

                if (!pred.contains(wpred)) {
                    pred.add(wpred);

                    if (!wpred.equals(ni)) {
                        w.add(wpred);
                    }
                }
            }
        }

        // Build the subgraph
        Subgraph<Node> sg = null;

        final List<Block> nodes = ListUtils.intersection(suc, pred);

        if (!mandatory)
            if (nodes.isEmpty())
                return null;

		nodes.add(ni);
		nodes.add(newnk);

        sg = new Subgraph<>(i);

        final Node nodeI = new Node(ni, i);
        sg.add(nodeI);
        w.add(ni);

        while (!w.isEmpty()) {
            Block n = w.remove();
            Set<Block> successors = getSuccessors(n);

            for (Block nsuc : successors) {
                if (nodes.contains(nsuc) && nodes.contains(n) && edges.get(n.id()).contains(nsuc.id())) {
                    int hash = Node.hash(nsuc.id(), i);
                    Node newNode = sg.get(hash);

                    if (newNode == null) {
                        newNode = new Node(nsuc, i);
                        sg.add(newNode);
                    }

                    hash = Node.hash(n.id(), i);
                    sg.addEdge(hash, newNode.id());
                    edges.get(n.id()).remove(nsuc.id());
                    w.add(nsuc);
                }
            }
        }

        sg.setEntry(nodeI);
        sg.setExit(new Node(newnk, i));


        // Recover original graph

        graph.removeEdge(nj.id(), newnk.id());
        graph.addEdge(nj.id(), nk.id());
        graph.remove(newnk.id());

        return sg;
    }

    private Set<Block> getSuccessors(Block b) {
        return graph.neighbors(b.id());
    }

    private Set<Block> getPredecessors(Block b) {
        return graph.inverse().neighbors(b.id());
    }

    // --- Getters

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