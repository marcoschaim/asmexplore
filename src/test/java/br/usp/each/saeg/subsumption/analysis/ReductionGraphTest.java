package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Program;
import br.usp.each.saeg.subsumption.graphdua.CoverageAnalyzer;
import br.usp.each.saeg.subsumption.graphdua.Dua;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.graphdua.Node;
import br.usp.each.saeg.subsumption.input.ClassInfo;
import br.usp.each.saeg.subsumption.input.MethodInfo;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ReductionGraphTest extends TestCase {
    private ClassInfo cl;
    private CoverageAnalyzer flowAnalyzer;
    private SubsumptionAnalyzer duaSubAnalyzer;
    private SubsumptionGraph sg;
    private ReductionGraph rg;

    @Ignore


    public void test0() {
        final Program program = new Program();
        final Block b1 = new Block(0);
        final Block b2 = new Block(1);
        final Block b3 = new Block(2);
        final Block b4 = new Block(3);
        final Block b5 = new Block(4);
        final Block b6 = new Block(5);
        final Block b7 = new Block(6);
        final Block b8 = new Block(7);
        final Block b9 = new Block(8);

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

        program.getGraph().addEdge(0, 1);
        program.getGraph().addEdge(1, 8);
        program.getGraph().addEdge(1, 2);
        program.getGraph().addEdge(2, 3);
        program.getGraph().addEdge(3, 7);
        program.getGraph().addEdge(7, 1);
        program.getGraph().addEdge(3, 4);
        program.getGraph().addEdge(4, 5);
        program.getGraph().addEdge(5, 6);
        program.getGraph().addEdge(4, 6);
        program.getGraph().addEdge(6, 3);

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
        b8.cuse(5); //program.variable("index"));

        program.getGraph().setExit(b9);
        // analyzer (graph, start, end, def, use, var)

        List<Dua> duas = new LinkedList<>();
        // 10 duas of a
        duas.add( new Dua(b1, b3,0, "a"));
        duas.add( new Dua(b1, b6,0, "a"));
        duas.add( new Dua(b1, b8,0, "a"));
        duas.add( new Dua(b1, b5, b7,0, "a"));
        duas.add( new Dua(b1, b5, b6,0, "a"));
        duas.add( new Dua(b8, b3,0, "a"));
        duas.add( new Dua(b8, b8,0, "a"));
        duas.add( new Dua(b8, b6,0, "a"));
        duas.add( new Dua(b8, b5, b7,0, "a"));
        duas.add( new Dua(b8, b5, b6,0, "a"));

        // 4 duas of n
        duas.add( new Dua(b1, b2, b3,1, "n"));
        duas.add( new Dua(b1, b2, b9,1, "n"));
        duas.add( new Dua(b1, b4, b5,1, "n"));
        duas.add( new Dua(b1, b4, b8,1, "n"));

        // 8 duas of sortupto
        duas.add( new Dua(b8, b2, b3,2, "sortupto"));
        duas.add( new Dua(b8, b2, b9,2, "sortupto"));
        duas.add( new Dua(b1, b2, b3,2, "sortupto"));
        duas.add( new Dua(b1, b2, b9,2, "sortupto"));
        duas.add( new Dua(b8, b3,2, "sortupto"));
        duas.add( new Dua(b8, b8,2, "sortupto"));
        duas.add( new Dua(b1, b8,2, "sortupto"));
        duas.add( new Dua(b1, b3,2, "sortupto"));

        // 2 duas maxpos
        duas.add( new Dua(b1, b8,3, "maxpos"));
        duas.add( new Dua(b6, b8,3, "maxpos"));

        // 6 duas mymax
        duas.add( new Dua(b3, b5, b7,4, "mymax"));
        duas.add( new Dua(b3, b5, b6,4, "mymax"));
        duas.add( new Dua(b6, b5, b7,4, "mymax"));
        duas.add( new Dua(b6, b5, b6,4, "mymax"));
        duas.add( new Dua(b3, b8,4, "mymax"));
        duas.add( new Dua(b6, b8,4, "mymax"));

        // 14 duas of index
        duas.add( new Dua(b3, b5, b6,5, "index"));
        duas.add( new Dua(b3, b5, b7,5, "index"));
        duas.add( new Dua(b3, b4, b5,5, "index"));
        duas.add( new Dua(b3, b4, b8,5, "index"));
        duas.add( new Dua(b7, b5, b6,5, "index"));
        duas.add( new Dua(b7, b5, b7,5, "index"));
        duas.add( new Dua(b7, b4, b5,5, "index"));
        duas.add(new Dua(b7, b4, b8, 5, "index"));

        duas.add(new Dua(b3, b6, 5, "index"));
        duas.add(new Dua(b3, b7, 5, "index"));
        duas.add(new Dua(b3, b8, 5, "index"));
        duas.add(new Dua(b7, b6, 5, "index"));
        duas.add(new Dua(b7, b7, 5, "index"));
        duas.add(new Dua(b7, b8, 5, "index"));

        MethodInfo mi = new MethodInfo("SortPaper", program, duas);
        mi.getProgram().computeDataFlowSets(duas);

        sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
        //System.out.println(sg);

        rg = new ReductionGraph(sg);
        rg.setDua2DefUseChains(mi.getDefChainsMap());
        if (!mi.getDefChainsMap().isEmpty())
            rg.setLines(mi.getLines());
        System.out.println(rg);

        System.out.println("#" + "Unconstrained duas:" + rg.unconstrainedNodes().size());
        writeBufferToFile("/Users/marcoschaim/projetos/data/analysis/sort/", "Sort.gdu", mi.graphDefUseToDot());

        Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
        while (it.hasNext()){
            ReductionNode u = it.next();
            System.out.println(u);
        }

        System.out.println("Transitive Clousure:");

        rg.findTransitiveClosure();
        System.out.println(rg.toDot());
        writeBufferToFile("/Users/marcoschaim/projetos/data/analysis/sort/", "Sort.red", rg.toDot());

        duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
        Graphdua graphdua = duaSubAnalyzer.findNode2DuasSubsumption();
        System.out.println(graphdua.toDotNodeSubsumption(duaSubAnalyzer));
    }

    @Ignore
    @Test
    public void test01() {
        // Variation of the M&B example to test the existence of particular paths to cover particular duas and
        // the subsmption relationship.

        final Program program = new Program();
        final Block b1 = new Block(0);
        final Block b2 = new Block(1);
        final Block b3 = new Block(2);
        final Block b4 = new Block(3);
        final Block b5 = new Block(4);
        final Block b6 = new Block(5);
        final Block b7 = new Block(6);
        final Block b8 = new Block(7);
        final Block b9 = new Block(8);

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

        program.getGraph().addEdge(0, 1);
        program.getGraph().addEdge(1, 8);
        program.getGraph().addEdge(1, 2);
        program.getGraph().addEdge(2, 3);
        program.getGraph().addEdge(3, 7);
        program.getGraph().addEdge(7, 1);
        program.getGraph().addEdge(3, 4);
        program.getGraph().addEdge(4, 5);
        program.getGraph().addEdge(5, 6);
        program.getGraph().addEdge(4, 6);
        program.getGraph().addEdge(6, 3);

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
        //b6.def(3); //program.variable("maxpos")); // deleted from the original example
        b6.cuse(0); //program.variable("a"));
        b6.cuse(5); //program.variable("index"));

        /**
         * index++
         */

        b7.def(5); //program.variable("index"));
        b7.def(3); //program.variable("maxpos")); // Added to the original example
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
        b8.cuse(5); //program.variable("index"));

        program.getGraph().setExit(b9);
        // analyzer (graph, start, end, def, use, var)

        List<Dua> duas = new LinkedList<>();
        // 10 duas of a
        duas.add( new Dua(b1, b3,0, "a"));
        duas.add( new Dua(b1, b6,0, "a"));
        duas.add( new Dua(b1, b8,0, "a"));
        duas.add( new Dua(b1, b5, b7,0, "a"));
        duas.add( new Dua(b1, b5, b6,0, "a"));
        duas.add( new Dua(b8, b3,0, "a"));
        duas.add( new Dua(b8, b8,0, "a"));
        duas.add( new Dua(b8, b6,0, "a"));
        duas.add( new Dua(b8, b5, b7,0, "a"));
        duas.add( new Dua(b8, b5, b6,0, "a"));

        // 4 duas of n
        duas.add( new Dua(b1, b2, b3,1, "n"));
        duas.add( new Dua(b1, b2, b9,1, "n"));
        duas.add( new Dua(b1, b4, b5,1, "n"));
        duas.add( new Dua(b1, b4, b8,1, "n"));

        // 8 duas of sortupto
        duas.add( new Dua(b8, b2, b3,2, "sortupto"));
        duas.add( new Dua(b8, b2, b9,2, "sortupto"));
        duas.add( new Dua(b1, b2, b3,2, "sortupto"));
        duas.add( new Dua(b1, b2, b9,2, "sortupto"));
        duas.add( new Dua(b8, b3,2, "sortupto"));
        duas.add( new Dua(b8, b8,2, "sortupto"));
        duas.add( new Dua(b1, b8,2, "sortupto"));
        duas.add( new Dua(b1, b3,2, "sortupto"));

        // 2 duas maxpos
        duas.add( new Dua(b1, b8,3, "maxpos"));
        // duas.add( new Dua(b6, b8,3, "maxpos"));  // Deleted from the original example
        duas.add( new Dua(b7, b8,3, "maxpos")); // Added from the original example

        // 6 duas mymax
        duas.add( new Dua(b3, b5, b7,4, "mymax"));
        duas.add( new Dua(b3, b5, b6,4, "mymax"));
        duas.add( new Dua(b6, b5, b7,4, "mymax"));
        duas.add( new Dua(b6, b5, b6,4, "mymax"));
        duas.add( new Dua(b3, b8,4, "mymax"));
        duas.add( new Dua(b6, b8,4, "mymax"));

        // 14 duas of index
        duas.add( new Dua(b3, b5, b6,5, "index"));
        duas.add( new Dua(b3, b5, b7,5, "index"));
        duas.add( new Dua(b3, b4, b5,5, "index"));
        duas.add( new Dua(b3, b4, b8,5, "index"));
        duas.add( new Dua(b7, b5, b6,5, "index"));
        duas.add( new Dua(b7, b5, b7,5, "index"));
        duas.add( new Dua(b7, b4, b5,5, "index"));
        duas.add(new Dua(b7, b4, b8, 5, "index"));

        duas.add(new Dua(b3, b6, 5, "index"));
        duas.add(new Dua(b3, b7, 5, "index"));
        duas.add(new Dua(b3, b8, 5, "index"));
        duas.add(new Dua(b7, b6, 5, "index"));
        duas.add(new Dua(b7, b7, 5, "index"));
        duas.add(new Dua(b7, b8, 5, "index"));

        MethodInfo mi = new MethodInfo("SortPaper", program, duas);
        mi.getProgram().computeDataFlowSets(duas);

        sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
        //System.out.println(sg);

        rg = new ReductionGraph(sg);
        rg.setDua2DefUseChains(mi.getDefChainsMap());
        if (!mi.getDefChainsMap().isEmpty())
            rg.setLines(mi.getLines());
        System.out.println(rg);

        System.out.println("#"+ "Unconstrained duas:" + rg.unconstrainedNodes().size());

        Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
        while (it.hasNext()) {
            ReductionNode u = it.next();
            System.out.println(u);
        }

        System.out.println("Transitive Clousure:");

        rg.findTransitiveClosure();
        System.out.println(rg.toDot());
        System.out.println(mi.graphDefUseToDot());
//        writeBufferToFile("/Users/marcoschaim/projetos/data/analysis/sort-mod/","Sort2-grf-anot.dot",mi.graphDefUseToDot());
//        writeBufferToFile("/Users/marcoschaim/projetos/data/analysis/sort-mod/","Sort2-red.dot",rg.toDot());

    }

    @Test
    public void test1() {
        System.out.println("Sort");
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/sort/", "Sort.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                mi.printMethodCFG();
                System.out.println(mi.graphDefUseToDot());
                mi.toDuasCSV();
                writeBufferToFile("/Users/marcoschaim/projetos/data/sort/", mi.getName() + ".csv", mi.toDuasCSV());

                sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
                //System.out.println(sg);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());
                System.out.println(rg);

                System.out.println("#" + mi.getName() + "Unconstrained duas: " + rg.unconstrainedNodes().size());

                Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
                while (it.hasNext()) {
                    ReductionNode u = it.next();
                    System.out.println(u);
                }

                System.out.println("Transitive Clousure:");

                rg.findTransitiveClosure();
                System.out.println(rg.toDot());
                writeBufferToFile("/Users/marcoschaim/projetos/data/sort/", mi.getName() + ".gdu", mi.graphDefUseToDot());
                writeBufferToFile("/Users/marcoschaim/projetos/data/sort/", mi.getName() + ".dot", rg.toDot());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test2() {
        sg = new SubsumptionGraph();
        for (int i = 1; i <= 8; ++i) {
            Dua<Node> d = new Dua(new Block(i), new Block(i), i, "dummy");
            SubsumptionNode s = new SubsumptionNode(d);
            s.setId(i);
            sg.add(s);
        }

        // Build graph

        sg.addEdge(1,2);
        sg.addEdge(3,1);
        sg.addEdge(2,3);

        sg.addEdge(4,2);
        sg.addEdge(6,3);
        sg.addEdge(4,3);

        sg.addEdge(4,5);
        sg.addEdge(5,4);
        sg.addEdge(5,6);

        sg.addEdge(6,7);
        sg.addEdge(7,6);
        sg.addEdge(8,7);

        sg.addEdge(8,5);
        sg.addEdge(8,8);


        rg = new ReductionGraph(sg,true);
        Iterator<ReductionNode> it = rg.iterator();
        while (it.hasNext())
            System.out.println(it.next());
    }


    @Test
    public void test3(){
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
                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".gz", mi.graphDefUseToDot());

                sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
                //System.out.println(sg);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());
                //System.out.println(rg);

                System.out.println("#"+mi.getName()+ "Unconstrained duas:" + rg.unconstrainedNodes().size());

                Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
                while (it.hasNext()){
                    ReductionNode u = it.next();
                    System.out.println(u);
                }

                System.out.println("Transitive Clousure:");

                rg.findTransitiveClosure();

                System.out.println(rg.toDot());

                //writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".dot", rg.toDot());

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test4() {
        System.out.println("MaxRogue");
        String dir = "/Users/marcoschaim/projetos/data/max/";
        String clazzname = "MaxRogue.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                mi.printMethodCFG();
                mi.toDuasCSV();
                writeBufferToFile(dir, mi.getName() + ".gdu", mi.graphDefUseToDot());
                sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
                //System.out.println(sg);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());
                System.out.println(rg);

                System.out.println("#"+mi.getName()+ "Unconstrained duas:" + rg.unconstrainedNodes().size());

                Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
                while (it.hasNext()) {
                    ReductionNode u = it.next();
                    System.out.println(u);
                }

                System.out.println("Transitive Clousure:");

                rg.findTransitiveClosure();
                writeBufferToFile(dir, mi.getName() + ".red", rg.toDot());
                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());
                System.out.println(rg.toDot());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5() {
        System.out.println("BOBYQAOptimizer");
        String dir = "/Users/marcoschaim/projetos/data/bobyqb/";
        String clazz = "BOBYQAOptimizer.class";
        try {
            cl = new ClassInfo(dir, clazz);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());

                System.out.println("#" + mi.getName() + "Data method on " + mi.getName() + ":");
                System.out.println("#" + mi.getName() + "# duas:" + mi.getDuas().size());
                System.out.println("#" + mi.getName() + "# Unconstrained duas:" + rg.unconstrainedNodes().size());
                System.out.println("#" + mi.getName() + "# Reduction nodes:" + rg.size());


                rg.findTransitiveClosure();
                //writeBufferToFile(dir, mi.getName()+".dot",rg.toDot());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5_1() {
        System.out.println("LovinsStemmer");
        String methodname = null;
        try {
            String dir = "/Users/marcoschaim/projetos/data/LovinsStemmer/";
            String clazz = "LovinsStemmer.class";
            cl = new ClassInfo(dir, clazz);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                methodname = mi.getName();

                if (mi.getHasIncomingEdges()) {
                    System.out.println("Warning: Method:" + methodname + " has incoming edges.");
                    continue;
                }

                if (mi.getHasAutoEdge()) {
                    System.out.println("Warning: Method:" + methodname + " has auto edges.");
                    continue;
                }

                writeBufferToFile(dir, mi.getName() + ".gdu", mi.graphDefUseToDot());
                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());

                sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());

                mi.setReductionGraph(rg);
                mi.setSubsumptionGraph(sg);

                System.out.println("#" + mi.getName() + "Data method on " + mi.getName() + ":");
                System.out.println("#" + mi.getName() + "# duas:" + mi.getDuas().size());
                System.out.println("#" + mi.getName() + "# Unconstrained duas:" + rg.unconstrainedNodes().size());
                System.out.println("#" + mi.getName() + "# Reduction nodes:" + rg.size());

                rg.findTransitiveClosure();
                writeBufferToFile(dir, mi.getName() + ".red", rg.toDot());
                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());
            }

            writeBufferToFile(dir, "LovinsStemmer.sub.json", cl.toJsonSubsumption());
            writeBufferToFile(dir, "LovinsStemmer.duas.json", cl.toJsonDuas());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5_2() {
        System.out.println("ReaderToTextPane");
        HashSet<String> methodNames = new HashSet<>();
        int methodId = 0;
        String dir = "/Users/marcoschaim/projetos/data/ReaderToTextPane/";
        String clazz = "ReaderToTextPane$1.class";
        String methodname = null;
        try {
            cl = new ClassInfo(dir, clazz);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {


                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;
                methodname = mi.getName();

                if (mi.getHasIncomingEdges()) {
                    System.out.println("Warning: Method:" + methodname + " has incoming edges.");
                    continue;
                }

                if (mi.getHasAutoEdge()) {
                    System.out.println("Warning: Method:" + methodname + " has auto edges.");
                    continue;
                }

                writeBufferToFile(dir, mi.getName() + ".gdu", mi.graphDefUseToDot());
                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());
                sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());

                System.out.println("#" + mi.getName() + "Data method on " + mi.getName() + ":");
                System.out.println("#" + mi.getName() + "# duas:" + mi.getDuas().size());
                System.out.println("#" + mi.getName() + "# Unconstrained duas:" + rg.unconstrainedNodes().size());
                System.out.println("#" + mi.getName() + "# Reduction nodes:" + rg.size());

                rg.findTransitiveClosure();
                if (methodNames.contains(mi.getName())) {
                    writeBufferToFile(dir, mi.getName() + methodId + ".red", rg.toDot());
                    methodId++;
                } else
                    writeBufferToFile(dir, mi.getName() + ".red", rg.toDot());
            }

//            writeBufferToFile(dir, "ResizableDoubleArray.sub.json", cl.toJsonSubsumption());
//            writeBufferToFile(dir, "ResizableDoubleArray.duas.json", cl.toJsonDuas());
        } catch (Exception e) {
            e.printStackTrace();
            String failfile = dir + clazz;
            System.out.println("Fail to analyze: " + failfile + ":" + methodname);
        }
    }

    public void test5_3() {
        System.out.println("PiePlot");
        try {
            String dir = "/Users/marcoschaim/projetos/data/PiePlot/";
            String clazz = "PiePlot.class";
            cl = new ClassInfo(dir, clazz);
            cl.genAllMethodInfo();
            System.out.println(cl.getName());

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                if (mi.getDuas().isEmpty())
                    continue;

                writeBufferToFile(dir, mi.getName() + ".gdu", mi.graphDefUseToDot());
                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());
                sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());

                System.out.println("#" + mi.getName() + "Data method on " + mi.getName() + ":");
                System.out.println("#" + mi.getName() + "# duas:" + mi.getDuas().size());
                System.out.println("#" + mi.getName() + "# Unconstrained duas:" + rg.unconstrainedNodes().size());
                System.out.println("#" + mi.getName() + "# Reduction nodes:" + rg.size());

                rg.findTransitiveClosure();
                writeBufferToFile(dir, mi.getName() + ".dot", rg.toDot());
            }

            writeBufferToFile(dir, "PiePlot.sub.json", cl.toJsonSubsumption());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test6() {
        System.out.println("searchForPath");
        String dir = "/Users/marcoschaim/projetos/data/searchForPath/";
        String clazzname = "Path.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                writeBufferToFile(dir, mi.getName() + ".csv", mi.toDuasCSV());
                writeBufferToFile(dir, mi.getName() + ".gdu", mi.graphDefUseToDot());
                sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas(), false);
                //System.out.println(sg);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());
                rg.findTransitiveClosure();
                mi.setSubsumptionGraph(sg);
                mi.setReductionGraph(rg);
                writeBufferToFile(dir, mi.getName() + ".dot", rg.toDot());
            }

            System.out.println(cl.toJsonSubsumption());
            writeBufferToFile(dir, "JDOMNodePointer.sub.json", cl.toJsonSubsumption());
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
            System.err.println("Cannot open file "+(dir+name));
        }
    }

}
