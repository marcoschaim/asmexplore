package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.subsumption.graphdua.CoverageAnalyzer;
import br.usp.each.saeg.subsumption.graphdua.Dua;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.graphdua.Node;
import br.usp.each.saeg.subsumption.input.ClassInfo;
import br.usp.each.saeg.subsumption.input.MethodInfo;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.BitSet;
import java.util.Iterator;

public class SubsumptionGraphTest extends TestCase {
    ClassInfo cl;
    private CoverageAnalyzer flowAnalyzer;
    private SubsumptionAnalyzer duaSubAnalyzer;
    private SubsumptionGraph subduagraph;

    //@Test
    public void test1() {
        System.out.println("Sort");
        StringBuffer sb = new StringBuffer();
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/sort/", "Sort.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                if (mi.getDuas().isEmpty())
                    continue;

                //mi.printMethodCFG();
                mi.toDuasCSV();
                //System.out.println(mi.toJsonEdges(sb));
                subduagraph = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
                System.out.println(subduagraph);
            }
            System.out.println(cl.toJsonNodes());
            System.out.println(cl.toJsonEdges());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        System.out.println("SortMod");
        String dir = "/Users/marcoschaim/projetos/data/sort-mod/";
        String classname = "SortMod.class";

        try {
            cl = new ClassInfo(dir, classname);
            cl.genAllMethodInfo();
            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                if (mi.getDuas().isEmpty())
                    continue;
                BitSet totalSubsumed = new BitSet(mi.getDuas().size());

                SubsumptionAnalyzer duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(),mi.getDuas());
                Graphdua grd = duaSubAnalyzer.findEdge2DuasSubsumption();
                System.out.println("CFG(" + mi.getName() + "):");
                System.out.println("Edges:");

                final Iterator<Node> it = grd.iterator();

                while (it.hasNext()) {
                    Node pred = it.next();
                    if(grd.neighbors(pred.id()).size() == 0)
                        continue;

                    duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(),mi.getDuas());
                    totalSubsumed.clear();

                    for (Node suc : grd.neighbors(pred.id())) {
                        System.out.println("Edge(" + pred.block().id() + ","+suc.block().id()+ "):");
                        BitSet subsumed = grd.getDuasSubsumedEdge(pred,suc);
                        totalSubsumed.or(subsumed);

                        if (!subsumed.isEmpty()) {
                            int idDua = -1;
                            while ((idDua = subsumed.nextSetBit(idDua + 1)) != -1) {
                                Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                                System.out.println("\t" + subDua.toString());
                            }

                        }
                        System.out.println();
                    }
                }
                System.out.println("Total of duas covered by edges: " + totalSubsumed.cardinality());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test3() {
        System.out.println("Selector");
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/filterForAdjacentSiblings/", "Selector.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                if (mi.getDuas().isEmpty())
                    continue;

                mi.printMethodCFG();
                mi.toDuasCSV();

                subduagraph = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
                System.out.println(subduagraph);
            }

            System.out.println(cl.toJsonNodes());
            System.out.println(cl.toJsonEdges());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test4() {
        System.out.println("RegEx");
        String dir = "/Users/marcoschaim/docencia/ach2003/exemplos/exemplo9/src/br/usp/each/si/ach2003/";
        String classname = "Main.class";

        try {
            cl = new ClassInfo(dir, classname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                if (mi.getDuas().isEmpty())
                    continue;

                if (mi.getHasDanglingNodes()) {
                    System.out.println("Method " + mi.getName() + " has dangling nodes.");
                    continue;
                }

                BitSet totalSubsumed = new BitSet(mi.getDuas().size());

//                SubsumptionAnalyzer duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(),mi.getDuas());
                subduagraph = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
                System.out.println(subduagraph);
//                Graphdua grd = duaSubAnalyzer.findEdge2DuasSubsumption();
//                System.out.println("CFG(" + mi.getName() + "):");
//                System.out.println("Edges:");
//
//                final Iterator<Node> it = grd.iterator();
//
//                while (it.hasNext()) {
//                    Node pred = it.next();
//                    if(grd.neighbors(pred.id()).size() == 0)
//                        continue;
//
//                    duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(),mi.getDuas());
//                    totalSubsumed.clear();
//
//                    for (Node suc : grd.neighbors(pred.id())) {
//                        System.out.println("Edge(" + pred.block().id() + ","+suc.block().id()+ "):");
//                        BitSet subsumed = grd.getDuasSubsumedEdge(pred,suc);
//                        totalSubsumed.or(subsumed);
//
//                        if (!subsumed.isEmpty()) {
//                            int idDua = -1;
//                            while ((idDua = subsumed.nextSetBit(idDua + 1)) != -1) {
//                                Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
//                                System.out.println("\t" + subDua.toString());
//                            }
//
//                        }
//                        System.out.println();
//                    }
//                }
//                System.out.println("Total of duas covered by edges: " + totalSubsumed.cardinality());
            }

            System.out.println(cl.toJsonDuas());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
