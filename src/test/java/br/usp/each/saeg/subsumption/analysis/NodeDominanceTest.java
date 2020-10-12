package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.subsumption.graphdua.CoverageAnalyzer;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.graphdua.Node;
import br.usp.each.saeg.subsumption.input.ClassInfo;
import br.usp.each.saeg.subsumption.input.MethodInfo;
import junit.framework.TestCase;

import java.util.Iterator;

public class NodeDominanceTest extends TestCase {
    ClassInfo cl;
    private CoverageAnalyzer flowAnalyzer;
    private NodeDominance<Node> dominanceGraphdua;

    public void test1() {
        System.out.println("BOBYQAOptimizer");
        String dir = "/Users/marcoschaim/projetos/data/bobyqb/";
        String clazzname = "BOBYQAOptimizer.class";
        int backarcs, retreatarcs;
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;
                System.out.println(mi.getName() + ":");
                SubsumptionAnalyzer duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua grf = duaSubAnalyzer.findNode2DuasSubsumption();

                System.out.println("forward graphdua:\n" + grf.toDot());
                dominanceGraphdua = new NodeDominance<Node>(grf, null);
                dominanceGraphdua.findDominanceGraphdua();
                System.out.println(dominanceGraphdua.toStringGraphduaDominance());

                Iterator<Node> itNode = grf.iterator();

                backarcs = retreatarcs = 0;
                while (itNode.hasNext()) {
                    Node n = itNode.next();
                    for (Node suc : grf.sucessors(n)) {
                        if (dominanceGraphdua.isDominatorInGraphdua(suc, n)) {
                            backarcs++;
                            System.out.println("Back arc(" + n.block().id() + "(" + n.idSubgraph() + ")" + "," + suc.block().id() + "(" + suc.idSubgraph() + "))");
                        }

                        if (grf.isRetreatingEdge(n, suc)) {
                            retreatarcs++;
                            System.out.println("Retreating arc(" + n.block().id() + "(" + n.idSubgraph() + ")" + "," + suc.block().id() + "(" + suc.idSubgraph() + "))");
                        }
                    }
                }
                System.out.println(mi.getName() + ":# of Backarcs:" + backarcs);
                System.out.println(mi.getName() + ":# of Retreatarcs:" + retreatarcs);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test2() {
        System.out.println("SingularValueDecomposition");
        String dir = "/Users/marcoschaim/projetos/data/SingularValueDecomposition/";
        String clazzname = "SingularValueDecomposition.class";
        int backarcs, retreatarcs;
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;
                System.out.println(mi.getName() + ":");
                SubsumptionAnalyzer duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua grf = duaSubAnalyzer.findNode2DuasSubsumption();

                System.out.println("forward graphdua:\n" + grf.toDot());
                dominanceGraphdua = new NodeDominance<Node>(grf, null);
                dominanceGraphdua.findDominanceGraphdua();
                System.out.println(dominanceGraphdua.toStringGraphduaDominance());

                Iterator<Node> itNode = grf.iterator();

                backarcs = retreatarcs = 0;
                while (itNode.hasNext()) {
                    Node n = itNode.next();
                    for (Node suc : grf.sucessors(n)) {
                        if (dominanceGraphdua.isDominatorInGraphdua(suc, n)) {
                            backarcs++;
                            System.out.println("Back arc(" + n.block().id() + "(" + n.idSubgraph() + ")" + "," + suc.block().id() + "(" + suc.idSubgraph() + "))");
                        }

                        if (grf.isRetreatingEdge(n, suc)) {
                            retreatarcs++;
                            System.out.println("Retreating arc(" + n.block().id() + "(" + n.idSubgraph() + ")" + "," + suc.block().id() + "(" + suc.idSubgraph() + "))");
                        }
                    }
                }
                System.out.println(mi.getName() + ":# of DUAs:" + mi.getDuas().size());
                System.out.println(mi.getName() + ":# of Backarcs:" + backarcs);
                System.out.println(mi.getName() + ":# of Retreatarcs:" + retreatarcs);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test3() {
        System.out.println("Metaphone");
        String dir = "/Users/marcoschaim/projetos/data/Metaphone/";
        String clazzname = "Metaphone.class";
        int backarcs, retreatarcs;
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;
                System.out.println(mi.getName() + ":");
                SubsumptionAnalyzer duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua grf = duaSubAnalyzer.findNode2DuasSubsumption();

                System.out.println("forward graphdua:\n" + grf.toDot());
                dominanceGraphdua = new NodeDominance<Node>(grf, null);
                dominanceGraphdua.findDominanceGraphdua();
                System.out.println(dominanceGraphdua.toStringGraphduaDominance());

                Iterator<Node> itNode = grf.iterator();

                backarcs = retreatarcs = 0;
                while (itNode.hasNext()) {
                    Node n = itNode.next();
                    for (Node suc : grf.sucessors(n)) {
                        if (dominanceGraphdua.isDominatorInGraphdua(suc, n)) {
                            backarcs++;
                            System.out.println("Back arc(" + n.block().id() + "(" + n.idSubgraph() + ")" + "," + suc.block().id() + "(" + suc.idSubgraph() + "))");
                        }

                        if (grf.isRetreatingEdge(n, suc)) {
                            retreatarcs++;
                            System.out.println("Retreating arc(" + n.block().id() + "(" + n.idSubgraph() + ")" + "," + suc.block().id() + "(" + suc.idSubgraph() + "))");
                        }
                    }
                }
                System.out.println(mi.getName() + ":# of Backarcs:" + backarcs);
                System.out.println(mi.getName() + ":# of Retreatarcs:" + retreatarcs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
