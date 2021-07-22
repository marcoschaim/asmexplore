package br.usp.each.saeg.subsumption.graphdua;

import br.usp.each.saeg.commons.BitSetIterator;
import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Graph;
import br.usp.each.saeg.opal.Program;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GraphduaTest extends TestCase {
    ClassInfo cl;
    private CoverageAnalyzer analyzer;

    @Test
    public void test1() {
        System.out.println("Sort");

        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/sort/", "Sort.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                Dua d;
                int counter = 1;
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().size() == 0) continue;

                Iterator<Dua> itdua = mi.getDuas().iterator();


                mi.printMethodCFG();
                //printGraphDefUse(mi.getProgram().getGraph());


                if (mi.getDuas().isEmpty())
                    continue;

                System.out.println(mi.graphDefUseToDot());

                while (itdua.hasNext()) {
                    d = itdua.next();
                    analyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                    System.out.println(counter + ":" + d.toString());
                    Graphdua grf = analyzer.findGraphdua();
//                    System.out.println("forward graphdua:\n"+grf);
//                    System.out.println("backward graphdua:\n"+printInverse(grf.inverse()));
//                    System.out.println(grf.toDotSubGraph(1));
//                    System.out.println(grf.toDotSubGraph(2));
//                    System.out.println(grf.toDotSubGraph(3));
//                    System.out.println(grf.toDotSubGraph(5));

                    System.out.println(grf.toDot());
                    ++counter;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        System.out.println("MaxComplicated");

        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/max/", "MaxComplicated.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                Dua d; int counter = 1;
                mi.createMethodCFG();
                mi.createMethodDuas();

                Iterator<Dua> itdua = mi.getDuas().iterator();

                printGraphDefUse(mi.getProgram().getGraph());

                if (mi.getDuas().isEmpty())
                    continue;
                while (itdua.hasNext()) {
                    d = itdua.next();
                    analyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                    System.out.println(counter + ":" + d.toString());
                    System.out.println(analyzer.findGraphdua().toDot());
                    ++counter;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test21() {
        System.out.println("Max");

        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/max/", "Max.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                Dua d;
                int counter = 1;
                mi.createMethodCFG();
                mi.createMethodDuas();

                Iterator<Dua> itdua = mi.getDuas().iterator();

                printGraphDefUse(mi.getProgram().getGraph());

                if (mi.getDuas().isEmpty())
                    continue;
                while (itdua.hasNext()) {
                    d = itdua.next();
                    analyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                    System.out.println(counter + ":" + d.toString());
                    System.out.println(analyzer.findGraphdua().toDot());
                    ++counter;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3() {
        System.out.println("filterForAdjacentSiblings");
        String dir = "/Users/marcoschaim/projetos/data/filterForAdjacentSiblings/";

        try {
            cl = new ClassInfo(dir, "Selector.class");
            cl.genAllMethodInfo();

            System.out.println("Number of Methods:" + cl.getMethodsInfo().size());
            for (MethodInfo mi : cl.getMethodsInfo()) {
                Dua d;
                int counter = 1;
                mi.createMethodCFG();
                mi.createMethodDuas();

                Iterator<Dua> itdua = mi.getDuas().iterator();

                mi.printMethodCFG();
                printGraphDefUse(mi.getProgram().getGraph());

                if (mi.getDuas().isEmpty())
                    continue;
                while (itdua.hasNext()) {
                    d = itdua.next();
                    analyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                    System.out.println(counter + ":" + d.toString());
                    System.out.println(analyzer.findGraphdua().toDot());
                    ++counter;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test3_1() {
        System.out.println("AggregateSummaryStatistics");
        String dir = "/Users/marcoschaim/projetos/data/AggregateSummaryStatistics/";
        String clazz = "AggregateSummaryStatistics.class";

        try {
            cl = new ClassInfo(dir, clazz);
            cl.genAllMethodInfo();

            System.out.println("Number of Methods:" + cl.getMethodsInfo().size());
            for (MethodInfo mi : cl.getMethodsInfo()) {
                Dua d;
                int counter = 1;
                mi.createMethodCFG();
                mi.createMethodDuas();

                Iterator<Dua> itdua = mi.getDuas().iterator();

                mi.printMethodCFG();
                printGraphDefUse(mi.getProgram().getGraph());

                if (mi.getDuas().isEmpty())
                    continue;
                while (itdua.hasNext()) {
                    d = itdua.next();
                    analyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), d);
                    System.out.println(counter + ":" + d.toString());
                    System.out.println(analyzer.findGraphdua().toDot());
                    ++counter;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void writeBufferToFile(String dir, String name, String s) {
        // Convert the string to a
        // byte array.

        byte[] data = s.getBytes();
        Path p = Paths.get(dir + name);

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println("Cannot open file " + (dir + name));
        }
    }

    //@Test
    public void test4() {
        final Program program = new Program();
        final Block b1 = new Block(1);
        final Block b2 = new Block(2);
        final Block b3 = new Block(3);
        final Block b4 = new Block(4);
        final Block b5 = new Block(5);
        final Block b6 = new Block(6);
        final Block b7 = new Block(7);
        final Block b8 = new Block(8);
        final Block b9 = new Block(9);

        program.getGraph().add(b1);
        program.getGraph().add(b2);
        program.getGraph().add(b3);
        program.getGraph().add(b4);
        program.getGraph().add(b5);
        program.getGraph().add(b6);
        program.getGraph().add(b7);
        program.getGraph().add(b8);
        program.getGraph().add(b9);

        program.getGraph().setEntry(b1);
        program.getGraph().setExit(b9);

        program.getGraph().addEdge(1, 2);
        program.getGraph().addEdge(2, 9);
        program.getGraph().addEdge(2, 3);
        program.getGraph().addEdge(3, 4);
        program.getGraph().addEdge(4, 8);
        program.getGraph().addEdge(8, 2);
        program.getGraph().addEdge(4, 5);
        program.getGraph().addEdge(5, 6);
        program.getGraph().addEdge(6, 7);
        program.getGraph().addEdge(5, 7);
        program.getGraph().addEdge(7, 4);

        program.addVariable("a", 0);
        program.addVariable("n", 1);
        program.addVariable("sortupto", 2);
        program.addVariable("maxpos", 3);
        program.addVariable("mymax", 4);
        program.addVariable("index", 5);

        /**
         * public void sort(int a[], int n)
         *     int sortupto = 1;
         *     int maxpos = 1;
         */

        b1.def(0); //,program.variable("a"));
        b1.def(1); //,program.variable("n"));
        b1.def(2); //,program.variable("sortupto"));
        b1.def(3); //program.variable("maxpos"));

        /**
         * while (sortupto < n)
         */

        b2.puse(2); //program.variable("sortupto"));
        b2.puse(1); //program.variable("n"));

        /**
         * mymax = a[sortupto]
         * index = sortupto + 1
         */

        b3.def(4); //program.variable("mymax"));
        b3.def(5); //program.variable("index"));
        b3.cuse(0); //program.variable("a"));
        b3.cuse(2); //program.variable("sortupto"));

        /**
         * while (index <= n)
         */

        b4.puse(5); //program.variable("index"));
        b4.puse(1); //program.variable("n"));

        /**
         * if (a[index] > mymax)
         */

        b5.puse(0); //program.variable("a"));
        b5.puse(5); //program.variable("index"));
        b5.puse(4); //program.variable("mymax"));

        /**
         * mymax = a[index]
         * maxpos = index
         */

        b6.def(4); //program.variable("mymax"));
        b6.def(3); //program.variable("maxpos"));
        b6.cuse(0); //program.variable("a"));
        b6.cuse(5); //program.variable("index"));

        /**
         * index++
         */

        b7.def(5); //program.variable("index"));
        b7.cuse(5); //program.variable("index"));

        /**
         * index = a[sortupto]
         * a[sortupto] = mymax
         * a[maxpos] = index
         * sortupto++
         */

        //b8.def(program.variable("index"));
        b8.def(0); //program.variable("a"));
        b8.def(2); //program.variable("sortupto"));
        b8.cuse(0); //program.variable("a"));
        b8.cuse(2); //program.variable("sortupto"));
        b8.cuse(4); //program.variable("mymax"));
        b8.cuse(3); //program.variable("maxpos"));
        b8.cuse(8); //program.variable("index"));

        program.getGraph().setExit(b9);
        // analyzer (graph, start, end, def, use, var)

        Dua<Block> d = new Dua(b3, b5, b7,4, "mymax");
        Dua<Node> d2 = new Dua(b3, b5,b7,4,"mymax");
        List<Dua> duas = new LinkedList<>();
        duas.add(d2);
        MethodInfo mi = new MethodInfo("SortPaper", program,duas);
        analyzer = new CoverageAnalyzer(program.getGraph(), d);
        analyzer.findAllSubgraphs();
        System.out.println("SG1 dot :");
        System.out.println(analyzer.toDot(analyzer.sg1()));

        System.out.println("SG2 dot :");
        System.out.println(analyzer.toDot(analyzer.sg2()));

        System.out.println("SG3 dot :");
        System.out.println(analyzer.toDot(analyzer.sg3()));

        System.out.println("SG4 dot :");
        //System.out.println(analyzer.toDot(analyzer.sg4()));

        System.out.println("SG5 dot :");
        System.out.println(analyzer.toDot(analyzer.sg5()));

        Graphdua grf = analyzer.findGraphdua();
        System.out.println("forward graphdua:\n" + grf);
        System.out.println("graph defuse:\n" + mi.graphDefUseToDot());
        System.out.println("Graphdua:\n" + grf.toDot());
        //writeBufferToFile("/Users/marcoschaim/projetos/data/sort/", "SortPaper"+ ".grd"+d.toString()+".gz",grf.toDot());
    }

    @Test
    public void test5() {
        System.out.println("MaxRogue");

        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/max/", "MaxRogue.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                Dua d;
                int counter = 1;
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().size() == 0) continue;

                Iterator<Dua> itdua = mi.getDuas().iterator();

                mi.printMethodCFG();
                System.out.println(mi.graphDefUseToDot());

                if (mi.getDuas().isEmpty())
                    continue;
                while (itdua.hasNext()) {
                    d = itdua.next();
                    analyzer = new CoverageAnalyzer(mi.getProgram(), d);
                    System.out.println(counter + ":" + d.toString());
                    Graphdua grf = analyzer.findGraphdua();
                    //System.out.println("forward graphdua:\n"+grf);
                    //System.out.println("backward graphdua:\n"+printInverse(grf.inverse()));
                    //writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName()+ ".grd"+d.toString()+".gz",grf.toDot());
                    System.out.println("GraphDua(" + d.toString() + "):");
                    System.out.println(grf.toDot());
                    ++counter;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void printGraphDefUse(Flowgraph<Block> gfc) {

        for (int i = 0; i <= gfc.exit().id(); ++i) {
            Block b = gfc.get(i);
            System.out.println("Node: " + b.id());
            System.out.println("\tDefs:" + printBitSetIterator(b.defs()));
            System.out.println("\tCuses:" + printBitSetIterator(b.cuses()));
            System.out.println("\tPuses:" + printBitSetIterator(b.puses()));
        }

    }

    private String printBitSetIterator(BitSetIterator bset){
        final StringBuilder sb = new StringBuilder();
        sb.append(" ");
        while(bset.hasNext())
        {
            sb.append(bset.next());
            sb.append(" ");
        }
        return sb.toString();
    }

    public String printInverse(Graph<Node> grf) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Node> i = grf.iterator();

        sb.append(":\n");

        while (i.hasNext()) {
            Node k = i.next();
            sb.append(k);
            sb.append("(");
            sb.append(k.idSubgraph());
            sb.append(")");
            sb.append(" -> ");

            Set<Node> neighbors = grf.neighbors(k.id());
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


}
