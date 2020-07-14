package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Graph;
import br.usp.each.saeg.opal.Program;
import br.usp.each.saeg.subsumption.graphdua.CoverageAnalyzer;
import br.usp.each.saeg.subsumption.graphdua.Dua;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.graphdua.Node;
import br.usp.each.saeg.subsumption.graphdua.*;

import java.util.BitSet;

import java.util.*;

public class SubsumptionAnalyzer {
    List<Dua> duas;
    Program program;
    Map<Dua, Integer> dua2id = new HashMap<>();
    Dua id2Duas[];
    BitSet id2Subsumed[];
    CoverageAnalyzer analyzer;

    public SubsumptionAnalyzer(Program p, List<Dua> listDuas) {
        this.duas = listDuas;
        program = p;
        id2Duas = new Dua[listDuas.size()];
        id2Subsumed = new BitSet[listDuas.size()];
        int id = 0;

        Iterator<Dua> itDua = duas.iterator();

        while (itDua.hasNext()) {
            Dua d = itDua.next();
            id2Duas[id] = d;
            id2Subsumed[id] = new BitSet(duas.size());
            dua2id.put(d, id);
            ++id;
        }
    }

    BitSet getSubsumedBy(Dua d) {
        return id2Subsumed[dua2id.get(d)];
    }

    public int getDuaId(Dua d) {
        return dua2id.get(d);
    }

    public Dua getDuaFromId(int id) {
        return id2Duas[id];
    }

    public Graphdua getGraphdua(Dua d) {
        analyzer = new CoverageAnalyzer(program.getGraph(), d);
        return analyzer.findGraphdua();
    }


    private void computeNodeSets(Graphdua graphdua) {

        Iterator<Node> itNode = graphdua.iterator();
        while (itNode.hasNext())
            itNode.next().initNodeSets(duas.size());

        itNode = graphdua.iterator();

        while (itNode.hasNext()) {
            Node n = itNode.next();
            int blkId = (n.block().id() >= 0) ? (n.block().id()) : (-n.block().id());
            for (int i = 0; i < duas.size(); i++) {
                Dua dua = id2Duas[i];

                if (dua.isCUse()) {
                    if (dua.use().id() == blkId) {
                        n.setGen(i);
                    }
                }
                if (!dua.isCUse()) {
                    if (dua.to().id() == blkId)
                        n.setGen(i);
                    else {
                        int from = dua.from().id();
                        if (from != blkId)
                            n.setSleepy(i);
                    }
                }


                if (dua.def().id() == blkId) {
                    n.setBorn(i);
                }
                if (dua.def().id() != blkId && program.getGraph().get(blkId).isDef(dua.var())) {
                    n.setKill(i);
                }

            }
        }


    }

    private void computeInAndOutSubmission(Graphdua graphdua) {
        BitSet temp = new BitSet(duas.size());
        BitSet temp2 = new BitSet(duas.size());
        BitSet oldout = new BitSet(duas.size());
        BitSet sleepy = new BitSet(duas.size());
        BitSet covered;
        Iterator<Node> itNode;

        Graph<Node> inverse = graphdua.inverse();

        itNode = graphdua.iterator();

        while (itNode.hasNext()) {
            Node n = itNode.next();
            if (n.block().id() == program.getGraph().entry().id()) {
                n.getOut().clear();
                // No dua is covered at node 0
                n.getCovered().clear();
                // only the first node of the graph should receive BORN.
                n.getOut().or(n.getBorn());
            } else {
                // all other nodes (dangling nodes included) receive all duas.
                n.getOut().set(0, duas.size(), true);
                n.getCovered().set(0, duas.size(), true);
            }
//            System.out.println("Node "+n.block().id()+"("+n.idSubgraph()+")");
//            System.out.println("Out:"+n.getOut());
//            System.out.println("Covered:"+n.getCovered());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            itNode = graphdua.iterator();

            while (itNode.hasNext()) {
                Node n = itNode.next();
                //System.out.println("Node "+printNode(n));
                if (inverse.neighbors(n.id()).isEmpty()) {
                    if (program.getGraph().entry().id() != n.block().id())
                        continue; // Graphnode's dangling node
                    temp.clear();
                    temp.or(n.getIn());
                    temp2.clear();
                } else {
                    temp.set(0, duas.size(), true);
                    temp2.set(0, duas.size(), true);
                }

                for (Node pred : inverse.neighbors(n.id())) {
                    temp.and(pred.getOut());
                    //System.out.println("Pred Out "+printNode(pred)+ ":"+pred.getOut());
                }

                //System.out.println("Out: "+temp);

                for (Node pred : inverse.neighbors(n.id())) {
                    temp2.and(pred.getCovered());
                    //System.out.println("Pred Covered "+printNode(pred)+":"+pred.getCovered());
                }


                n.getIn().clear();
                n.getIn().or(temp);

                n.getLiveDuas().clear();
                n.getLiveDuas().or(n.getIn());

                sleepy.clear();

                for (Node pred : inverse.neighbors(n.id()))
                    sleepy.or(pred.getSleepy());

                n.getCovered().clear();

                n.getCovered().or(n.getLiveDuas());
                n.getCovered().andNot(sleepy);
                n.getCovered().and(n.getGen());

                temp2.or(n.getCovered());
                n.getCovered().clear();
                n.getCovered().or(temp2);

                oldout.clear();
                oldout.or(n.getOut());

                // out[B] := born[B] U (in[B] - kill[B])
                temp.clear();
                temp.or(n.getIn());
                // temp := in[B] - kill[B] ;)
                temp.andNot(n.getKill());

                // out[B] := born[B] U temp U Covered
                n.getOut().clear();
                n.getOut().or(n.getBorn());
                n.getOut().or(temp);
                n.getOut().or(n.getCovered());

                //covered = n.getCovered();
                //System.out.println("Covered "+printNode(n)+": "+covered);

                if (!n.getOut().equals(oldout))
                    changed = true;

            }

        }
    }


    public BitSet findDua2DuasSubsumption(Dua d) {
        analyzer = new CoverageAnalyzer(program.getGraph(), d);
        Graphdua graphdua = analyzer.findGraphdua();

        computeNodeSets(graphdua);
        computeInAndOutSubmission(graphdua);

        return graphdua.exit().getCovered();
    }

    public Graphdua findNode2DuasSubsumption() {
        Block entry, exit;

        // Create fake dua
        entry = program.getGraph().entry();
        exit = program.getGraph().exit();
        Dua fakedua = new Dua(entry, exit, 0, "this");

        // Create graphdua equals to CFG
        analyzer = new CoverageAnalyzer(program.getGraph(), fakedua);
        Graphdua graphdua = analyzer.findGraphdua();

        computeNodeSets(graphdua);
        computeInAndOutSubmission(graphdua);
        // Covered bitset contains the duas covered at each node
        return graphdua;
    }

    public Graphdua findEdge2DuasSubsumption() {
        Block entry, exit;
        // Create fake dua
        entry = program.getGraph().entry();
        exit = program.getGraph().exit();
        Dua fakedua = new Dua(entry, exit, 0, "this");

        // Create graphdua equals to CFG
        analyzer = new CoverageAnalyzer(program.getGraph(), fakedua);
        Graphdua graphdua = analyzer.findGraphdua();

        computeNodeSets(graphdua);
        computeInAndOutSubmission(graphdua);
        // Covered bitset contains the duas covered at each node

        // This method just find the nodes covered in each node; to get the subsumed by an edge
        // use getDuasSubsumedEdge()

        return graphdua;
    }


    BitSet[] findAllDuaSubsumption() {
        Iterator<Dua> itDua = duas.iterator();

        while (itDua.hasNext()) {
            Dua d = itDua.next();
            id2Subsumed[getDuaId(d)] = findDua2DuasSubsumption(d);
        }

        return id2Subsumed;
    }

    String printNode(Node n) {
        final StringBuilder sb = new StringBuilder();
        sb.append(n.block().id);
        sb.append("(" + n.idSubgraph() + ")");
        return sb.toString();
    }
}
