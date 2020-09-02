package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.subsumption.graphdua.CoverageAnalyzer;
import br.usp.each.saeg.subsumption.graphdua.Dua;
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

    public void test7() {
        System.out.println("AggregateSummaryStatistics");
        String dir = "/Users/marcoschaim/projetos/data/AggregateSummaryStatistics/";
        String clazzname = "AggregateSummaryStatistics.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    if (!mi.getName().equals("Aggregate"))
                        continue;

                Dua d;
                int counter = 1;
                Iterator<Dua> itdua = mi.getDuas().iterator();

                while (itdua.hasNext()) {
                    d = itdua.next();
                    if (counter == 34) {
                        flowAnalyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                        System.out.println(counter + ":" + d.toString());
                        Graphdua grf = flowAnalyzer.findGraphdua();
                        System.out.println("forward graphdua:\n" + grf.toDot());
                        dominanceGraphdua = new NodeDominance<Node>(grf, null);
                        dominanceGraphdua.findDominanceGraphdua();
                        System.out.println(dominanceGraphdua.toStringGraphduaDominance());

                        Iterator<Node> itNode = grf.iterator();
                        while (itNode.hasNext()) {
                            Node n = itNode.next();
                            for (Node suc : grf.sucessors(n)) {
                                if (dominanceGraphdua.isDominatorInGraphdua(suc, n))
                                    System.out.println("Back arc(" + n.block().id() + "(" + n.idSubgraph() + ")" + "," + suc.block().id() + "(" + suc.idSubgraph() + "))");
                            }
                        }
                    }
                    ++counter;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
