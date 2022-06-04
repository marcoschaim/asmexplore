package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.subsumption.graphdua.CoverageAnalyzer;
import br.usp.each.saeg.subsumption.graphdua.Dua;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.graphdua.Node;
import br.usp.each.saeg.subsumption.input.ClassInfo;
import br.usp.each.saeg.subsumption.input.MethodInfo;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Iterator;

public class DuaSubsumptionTest extends TestCase {
    ClassInfo cl;
    private CoverageAnalyzer flowAnalyzer;
    private SubsumptionAnalyzer duaSubAnalyzer;


    @Test
    public void test1() {
        System.out.println("Sort");
        String dir = "/Users/marcoschaim/projetos/data/sort/";
        String clazzname = "Sort.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                Dua d;
                int counter = 1;
                Iterator<Dua> itdua = mi.getDuas().iterator();

                mi.printMethodCFG();
                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());
                writeBufferToFile(dir, mi.getName() + ".gdu", mi.graphDefUseToDot());

                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua graphdua = duaSubAnalyzer.findNode2DuasSubsumption();
                System.out.println(graphdua.toDotNodeSubsumption(duaSubAnalyzer));

                while (itdua.hasNext()) {
                    d = itdua.next();
                    flowAnalyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                    //System.out.println(counter+":" + d.toString());
                    Graphdua grf = flowAnalyzer.findGraphdua();

                    System.out.println(grf.toDot());
                    System.out.println(counter + ":" + d.toString());
                    BitSet subsumed = duaSubAnalyzer.findDua2DuasSubsumption(d);
                    if (!subsumed.isEmpty()) {
                        int idDua = -1;
                        while ((idDua = subsumed.nextSetBit(idDua + 1)) != -1) {
                            Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                            System.out.println("\t" + subDua.toString());
                        }
                    } else
                        System.out.println("\tUnconstrained");
                    ++counter;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        System.out.println("Max");
        String dir = "/Users/marcoschaim/projetos/data/max/";
        String clazzname = "Max.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                Dua d;
                int counter = 1;
                Iterator<Dua> itdua = mi.getDuas().iterator();

                mi.printMethodCFG();
                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());

                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());


                while (itdua.hasNext()) {
                    d = itdua.next();
                    flowAnalyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                    //System.out.println(counter+":" + d.toString());
                    Graphdua grf = flowAnalyzer.findGraphdua();
                    System.out.println("forward graphdua:\n" + grf);
                    System.out.println(counter + ":" + d.toString());
                    BitSet subsumed = duaSubAnalyzer.findDua2DuasSubsumption(d);
                    if (!subsumed.isEmpty()) {
                        int idDua = -1;
                        while ((idDua = subsumed.nextSetBit(idDua + 1)) != -1) {
                            Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                            System.out.println("\t" + subDua.toString());
                        }
                    } else
                        System.out.println("\tUnconstrained");
                    ++counter;
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3() {
        System.out.println("Max");

        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/max/", "Max.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                mi.printMethodCFG();
                mi.toDuasCSV();
//                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".csv", mi.printMethodDuas());
//                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".gz", mi.graphDefUseToDot());

                System.out.println(mi.graphDefUseToDot());
                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());

                Dua d;
                int counter = 1;
                Iterator<Dua> itdua = mi.getDuas().iterator();


                while (itdua.hasNext()) {
                    d = itdua.next();
                    System.out.println(counter + ":" + d.toString());
                    BitSet subsumed = duaSubAnalyzer.findDua2DuasSubsumption(d);
                    if (!subsumed.isEmpty()) {
                        int idDua = -1;
                        while ((idDua = subsumed.nextSetBit(idDua + 1)) != -1) {
                            Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                            System.out.println("\t" + subDua.toString());
                        }
                    } else
                        System.out.println("\tUnconstrained");
                    ++counter;
                }

                System.out.println();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test31() {
        System.out.println("Max");

        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/max/", "Max.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                mi.printMethodCFG();
                mi.toDuasCSV();
//                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".csv", mi.printMethodDuas());
//                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".gz", mi.graphDefUseToDot());

                System.out.println(mi.graphDefUseToDot());

                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua graphdua = duaSubAnalyzer.findNode2DuasSubsumption();
//                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".ns",graphdua.toDotNodeSubsumption(duaSubAnalyzer));

                System.out.println(graphdua.toDotNodeSubsumption(duaSubAnalyzer));

                graphdua = duaSubAnalyzer.findEdge2DuasSubsumption();
//                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".es",graphdua.toDotEdgeSubsumption(duaSubAnalyzer));

                System.out.println(graphdua.toDotEdgeSubsumption(duaSubAnalyzer));
                Iterator<Node> itNode = graphdua.iterator();

                while (itNode.hasNext()) {
                    Node n = itNode.next();

                    BitSet coveredInNode = n.getCovered();
                    System.out.println("Duas covered in node " + n.block().id() + ":");
                    if (!coveredInNode.isEmpty()) {
                        int idDua = -1;
                        while ((idDua = coveredInNode.nextSetBit(idDua + 1)) != -1) {
                            Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                            System.out.println("\t" + subDua.toString());
                        }
                    } else
                        System.out.println("\tNo dua is mandatorily covered in node " + n.block().id() + ".");
                }

                System.out.println();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test32() {
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

                mi.printMethodCFG();
                mi.toDuasCSV();

                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua graphdua = duaSubAnalyzer.findNode2DuasSubsumption();
                mi.setGraphDua(graphdua);
                mi.setSubsumptionAnalyzer(duaSubAnalyzer);
                writeBufferToFile("/Users/marcoschaim/projetos/data/sort/", mi.getName() + ".ns", graphdua.toDotNodeSubsumption(duaSubAnalyzer));
                System.out.println(mi.toJsonNodes(sb));
                System.out.println(mi.toJsonNodeSubsumption(sb));
                System.out.println(graphdua.toDotNodeSubsumption(duaSubAnalyzer));
//                graphdua = duaSubAnalyzer.findEdge2DuasSubsumption();

//                mi.setGraphDua(graphdua);
//                mi.setSubsumptionAnalyzer(duaSubAnalyzer);
//                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".es",graphdua.toDotEdgeSubsumption(duaSubAnalyzer));

//                System.out.println(graphdua.toDotEdgeSubsumption(duaSubAnalyzer));
//                System.out.println(mi.toJsonEdgeSubsumption(sb));

                Iterator<Node> itNode = graphdua.iterator();

                while (itNode.hasNext()) {
                    Node n = itNode.next();

                    BitSet coveredInNode = n.getCovered();
                    System.out.println("Duas covered in node " + n.block().id() + ":");
                    if (!coveredInNode.isEmpty()) {
                        int idDua = -1;
                        while ((idDua = coveredInNode.nextSetBit(idDua + 1)) != -1) {
                            Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                            System.out.println("\t" + subDua.toString());
                        }
                    } else
                        System.out.println("\tNo dua is mandatorily covered in node " + n.block().id() + ".");
                }

                System.out.println();
            }

            System.out.println("Nodes");
            System.out.println(cl.toJsonNodes());
            System.out.println("Duas2Nodes0");
            System.out.println(cl.toJsonNodeSubsumption());
            System.out.println("Edges");
            System.out.println(cl.toJsonEdges());
            System.out.println("Duas2edges");
            System.out.println(cl.toJsonEdgeSubsumption());
            System.out.println("Duas2nodes0");
            System.out.println(cl.toJsonNodeSubsumption());
            System.out.println("Duas");
            System.out.println(cl.toJsonDuas());
            System.out.println("Duas2Edges1");
            System.out.println(cl.toJsonDuas2Edges());
            System.out.println("Duas2Nodes1");
            System.out.println(cl.toJsonDuas2Nodes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test4() {
        System.out.println("SortMod");
        String dir = "/Users/marcoschaim/projetos/data/sort-mod/";
        String clazzname = "SortMod.class";

        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                Dua d;
                int counter = 1;
                Iterator<Dua> itdua = mi.getDuas().iterator();

                mi.printMethodCFG();
                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());
                writeBufferToFile(dir, mi.getName() + ".gdu", mi.graphDefUseToDot());

                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua graphdua = duaSubAnalyzer.findNode2DuasSubsumption();

                BitSet allSubsumed = new BitSet(mi.getDuas().size());
                allSubsumed.clear();
                Iterator<Node> i = graphdua.iterator();
                while (i.hasNext()) {
                    Node k = i.next();
                    allSubsumed.or(k.getCovered());
                }
                System.out.println("Total of duas covered by touring only nodes of "+mi.getName()+":"+allSubsumed.cardinality());
                writeBufferToFile(dir, mi.getName() + ".ns", graphdua.toDotNodeSubsumption(duaSubAnalyzer));

                while (itdua.hasNext()) {
                    d = itdua.next();
                    flowAnalyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                    //System.out.println(counter+":" + d.toString());
                    Graphdua grf = flowAnalyzer.findGraphdua();

                    System.out.println(grf.toDot());
                    System.out.println(counter + ":" + d.toString());
                    BitSet subsumed = duaSubAnalyzer.findDua2DuasSubsumption(d);

                    if (!subsumed.isEmpty()) {
                        int idDua = -1;
                        while ((idDua = subsumed.nextSetBit(idDua + 1)) != -1) {
                            Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                            System.out.println("\t" + subDua.toString());
                        }

                    } else
                        System.out.println("\tUnconstrained");
                    ++counter;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5() {
        System.out.println("PiePlot");
        String dir = "/Users/marcoschaim/projetos/data/PiePlot/";
        String clazzname = "PiePlot.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());
                writeBufferToFile(dir, mi.getName() + ".gdu", mi.graphDefUseToDot());

                SubsumptionGraph sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
                ReductionGraph rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());

                System.out.println(rg);

                System.out.println("#"+ "Unconstrained duas:" + rg.unconstrainedNodes().size());

                System.out.println("Transitive Clousure:");

                rg.findTransitiveClosure();
                System.out.println(mi.getName() + ":");
                System.out.println(rg.toDot());
                writeBufferToFile(dir, mi.getName() + ".red", rg.toDot());

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test6() {
        System.out.println("SortMod");
        String dir = "/Users/marcoschaim/projetos/data/sort-mod/";
        String clazzname = "SortMod.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                Dua d;
                int counter = 1;
                Iterator<Dua> itdua = mi.getDuas().iterator();


                //mi.printMethodCFG();
                //writeBufferToFile(dir,mi.getName()+".csv", mi.printMethodDuas());
                System.out.println(mi.graphDefUseToDot());

                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua graphdua = duaSubAnalyzer.findEdge2DuasSubsumption();

                System.out.println(graphdua.toDot());

                BitSet allSubsumed = new BitSet(mi.getDuas().size());
                allSubsumed.clear();
                Iterator<Node> i = graphdua.iterator();
                while (i.hasNext()) {
                    Node k = i.next();

                    for (Node suc : graphdua.neighbors(k.id())) {
                        System.out.println("Edge(" + k.block().id() + "," + suc.block().id() + "):");
                        BitSet subsumed = graphdua.getDuasSubsumedEdge(k, suc);
                        allSubsumed.or(subsumed);
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
                System.out.println(graphdua.toDotEdgeSubsumption(duaSubAnalyzer));
                System.out.println("Total of duas covered by touring only nodes of " + mi.getName() + ":" + allSubsumed.cardinality());
                //writeBufferToFile(dir, mi.getName() + ".ns", graphdua.toDotEdgeSubsumption(duaSubAnalyzer));
                System.out.println(graphdua.toDotEdgeSubsumption(duaSubAnalyzer));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test7() {
        System.out.println("RegEx");
        String dir = "/Users/marcoschaim/projetos/data/RegEx/";
        String clazzname = "Main.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();
            System.out.println(cl.getName());

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                System.out.println(mi.getName());


                Dua d;
                int counter = 0;
                Iterator<Dua> itdua = mi.getDuas().iterator();


                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());

                while (itdua.hasNext()) {
                    d = itdua.next();
                    if (counter == 17) {
                        flowAnalyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                        System.out.println(counter + ":" + d.toString());
                        Graphdua grf = flowAnalyzer.findGraphdua();
                        System.out.println("forward graphdua:\n" + grf.toDot());
                        BitSet subsumed = duaSubAnalyzer.findDua2DuasSubsumption(d);
//
                        System.out.println(counter + ":" + d.toString() + ":" + subsumed);

                        if (!subsumed.isEmpty()) {
                            int idDua = -1;
                            while ((idDua = subsumed.nextSetBit(idDua + 1)) != -1) {
                                Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                                System.out.println("\t" + idDua + ":" + subDua.toString());
                            }
                        } else
                            System.out.println("\tUnconstrained");
                    }
                    ++counter;
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test8() {
        System.out.println("CollectionUtils");
        StringBuffer sb = new StringBuffer();
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/AbstractReferenceMap/", "CollectionUtils.class");
            cl.genAllMethodInfo();


            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                mi.printMethodCFG();
                mi.toDuasCSV();

                duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua graphdua = duaSubAnalyzer.findNode2DuasSubsumption();
                mi.setGraphDua(graphdua);
                mi.setSubsumptionAnalyzer(duaSubAnalyzer);
                writeBufferToFile("/Users/marcoschaim/projetos/data/AbstractReferenceMap/", mi.getName() + ".ns", graphdua.toDotNodeSubsumption(duaSubAnalyzer));
//                System.out.println(mi.toJsonNodes(sb));
//                System.out.println(mi.toJsonNodeSubsumption(sb));
//                System.out.println(graphdua.toDotNodeSubsumption(duaSubAnalyzer));
//                graphdua = duaSubAnalyzer.findEdge2DuasSubsumption();

//                mi.setGraphDua(graphdua);
//                mi.setSubsumptionAnalyzer(duaSubAnalyzer);
//                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".es",graphdua.toDotEdgeSubsumption(duaSubAnalyzer));

//                System.out.println(graphdua.toDotEdgeSubsumption(duaSubAnalyzer));
//                System.out.println(mi.toJsonEdgeSubsumption(sb));

                Iterator<Node> itNode = graphdua.iterator();

                while (itNode.hasNext()) {
                    Node n = itNode.next();

                    BitSet coveredInNode = n.getCovered();
                    System.out.println("Duas covered in node " + n.block().id() + ":");
                    if (!coveredInNode.isEmpty()) {
                        int idDua = -1;
                        while ((idDua = coveredInNode.nextSetBit(idDua + 1)) != -1) {
                            Dua subDua = duaSubAnalyzer.getDuaFromId(idDua);
                            System.out.println("\t" + subDua.toString());
                        }
                    } else
                        System.out.println("\tNo dua is mandatorily covered in node " + n.block().id() + ".");
                }

                System.out.println();
            }

//            System.out.println("Nodes");
            System.out.println(cl.toJsonNodes());
//            System.out.println("Duas2Nodes0");
            System.out.println(cl.toJsonNodeSubsumption());
//            System.out.println("Edges");
            System.out.println(cl.toJsonEdges());
//            System.out.println("Duas2edges");
            System.out.println(cl.toJsonEdgeSubsumption());
//            System.out.println("Duas2nodes0");
//            System.out.println(cl.toJsonNodeSubsumption());
            System.out.println("Duas");
            System.out.println(cl.toJsonDuas());
            System.out.println("Duas2Edges1");
            System.out.println(cl.toJsonDuas2Edges());
            System.out.println("Duas2Nodes1");
            System.out.println(cl.toJsonDuas2Nodes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void writeBufferToFile(String dir, String name, String s) {
        // Convert the string to a
        // byte array.

        byte[] data = s.getBytes();
        Path p = Paths.get(dir + name);

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
