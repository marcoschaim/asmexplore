package br.usp.each.saeg.subsumption.analysis;

import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Program;
import br.usp.each.saeg.subsumption.graphdua.CoverageAnalyzer;
import br.usp.each.saeg.subsumption.graphdua.Dua;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ReductionGraphTest extends TestCase {
    private ClassInfo cl;
    private CoverageAnalyzer flowAnalyzer;
    private SubsumptionAnalyzer duaSubAnalyzer;
    private SubsumptionGraph sg;
    private ReductionGraph rg;

    @Test
    public void test0(){
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
        duas.add( new Dua(b7, b4, b8,5, "index"));

        duas.add( new Dua(b3, b6,5, "index"));
        duas.add( new Dua(b3, b7,5, "index"));
        duas.add( new Dua(b3, b8,5, "index"));
        duas.add( new Dua(b7, b6,5, "index"));
        duas.add( new Dua(b7, b7,5, "index"));
        duas.add( new Dua(b7, b8,5, "index"));

        MethodInfo mi = new MethodInfo("SortPaper", program,duas);
        sg = new SubsumptionGraph(mi.getProgram(),mi.getDuas());
        //System.out.println(sg);

        rg = new ReductionGraph(sg);
        rg.setDua2DefUseChains(mi.getDefChainsMap());
        if(!mi.getDefChainsMap().isEmpty())
            rg.setLines(mi.getLines());
        System.out.println(rg);

        System.out.println("#"+ "Unconstrained duas:" + rg.unconstrainedNodes().size());

        Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
        while (it.hasNext()){
            ReductionNode u = it.next();
            System.out.println(u);
        }

        System.out.println("Transitive Clousure:");

        rg.findTransitiveClosure();
        System.out.println(rg.toDot());
        //writeBufferToFile("/Users/marcoschaim/projetos/data/sort/src/main/java/br/usp/each/saeg/",mi.getName()+".dot",rg.toDot());

    }

    @Test
    public void test01(){
        // Variation of the M&B example to test the existence of particular paths to cover particular duas and
        // the subsmption relationship.

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
        duas.add( new Dua(b7, b4, b8,5, "index"));

        duas.add( new Dua(b3, b6,5, "index"));
        duas.add( new Dua(b3, b7,5, "index"));
        duas.add( new Dua(b3, b8,5, "index"));
        duas.add( new Dua(b7, b6,5, "index"));
        duas.add( new Dua(b7, b7,5, "index"));
        duas.add( new Dua(b7, b8,5, "index"));

        MethodInfo mi = new MethodInfo("SortPaper", program,duas);
        sg = new SubsumptionGraph(mi.getProgram(),mi.getDuas());
        //System.out.println(sg);

        rg = new ReductionGraph(sg);
        rg.setDua2DefUseChains(mi.getDefChainsMap());
        if(!mi.getDefChainsMap().isEmpty())
            rg.setLines(mi.getLines());
        System.out.println(rg);

        System.out.println("#"+ "Unconstrained duas:" + rg.unconstrainedNodes().size());

        Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
        while (it.hasNext()){
            ReductionNode u = it.next();
            System.out.println(u);
        }

        System.out.println("Transitive Clousure:");

        rg.findTransitiveClosure();
        System.out.println(rg.toDot());
        writeBufferToFile("/Users/marcoschaim/projetos/data/analysis/sort-mod/","Sort2-grf-anot.dot",mi.graphDefUseToDot());
        writeBufferToFile("/Users/marcoschaim/projetos/data/analysis/sort-mod/","Sort2-red.dot",rg.toDot());

    }
    @Test
    public void test1() {
        System.out.println("Sort");
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/sort/src/main/java/br/usp/each/saeg/", "Sort.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                if (mi.getDuas().isEmpty())
                    continue;

                mi.printMethodCFG();
                System.out.println(mi.graphDefUseToDot());
                mi.printMethodDuas();
                writeBufferToFile("/Users/marcoschaim/projetos/data/sort/src/main/java/br/usp/each/saeg/",mi.getName()+".gz",mi.graphDefUseToDot());

                sg = new SubsumptionGraph(mi.getProgram(),mi.getDuas());
                //System.out.println(sg);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());
                System.out.println(rg);

                System.out.println("#"+mi.getName()+ "Unconstrained duas:" + rg.unconstrainedNodes().size());

                Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
                while (it.hasNext()){
                    ReductionNode u = it.next();
                    System.out.println(u);
                }

                System.out.println("Transitive Clousure:");

                rg.findTransitiveClosure();
                System.out.println(rg.toDot());
                writeBufferToFile("/Users/marcoschaim/projetos/data/sort/src/main/java/br/usp/each/saeg/",mi.getName()+".dot",rg.toDot());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void test2() {
        sg = new SubsumptionGraph();
        for(int i = 1; i <= 8; ++i){
            Dua<Node> d = new Dua(new Block(i),new Block(i), i,"dummy");
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
                mi.printMethodDuas();
                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".gz", mi.graphDefUseToDot());

                sg = new SubsumptionGraph(mi.getProgram(),mi.getDuas());
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

                writeBufferToFile("/Users/marcoschaim/projetos/data/max/", mi.getName() + ".dot", rg.toDot());

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test4(){
        System.out.println("Max");
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/weka-3.8-master/weka/build/classes/weka/core/", "Matrix.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                if (mi.getDuas().isEmpty())
                    continue;

                mi.printMethodCFG();
                mi.printMethodDuas();
                writeBufferToFile("/Users/marcoschaim/projetos/data/weka-3.8-master/weka/build/classes/weka/core/",mi.getName()+".gz",mi.graphDefUseToDot());
                sg = new SubsumptionGraph(mi.getProgram(),mi.getDuas());
                //System.out.println(sg);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());
                System.out.println(rg);

                System.out.println("#"+mi.getName()+ "Unconstrained duas:" + rg.unconstrainedNodes().size());

                Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
                while (it.hasNext()){
                    ReductionNode u = it.next();
                    System.out.println(u);
                }

                System.out.println("Transitive Clousure:");

                rg.findTransitiveClosure();
                writeBufferToFile("/Users/marcoschaim/projetos/data/weka-3.8-master/weka/build/classes/weka/core/",mi.getName()+".dot",rg.toDot());

                System.out.println(rg.toDot());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test5(){
        System.out.println("BOBYQAOptimizer");
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/BOBYQAOptimizer/", "BOBYQAOptimizer.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                if (mi.getDuas().isEmpty())
                    continue;

                //mi.printMethodCFG();
                //mi.printMethodDuas();
                writeBufferToFile("/Users/marcoschaim/projetos/data/BOBYQAOptimizer/",mi.getName()+".gz",mi.graphDefUseToDot());

                sg = new SubsumptionGraph(mi.getProgram(),mi.getDuas());
                //System.out.println(sg);

                rg = new ReductionGraph(sg);
                rg.setDua2DefUseChains(mi.getDefChainsMap());
                rg.setLines(mi.getLines());
                //System.out.println(rg);

                System.out.println("#"+mi.getName()+ "Data method on "+ mi.getName() + ":");
                System.out.println("#"+mi.getName()+ "# duas:" + mi.getDuas().size());
                System.out.println("#"+mi.getName()+ "# Unconstrained duas:" + rg.unconstrainedNodes().size());
                System.out.println("#"+mi.getName()+ "# Reduction nodes:" + rg.size());

//                Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();
//                while (it.hasNext()){
//                    ReductionNode u = it.next();
//                    System.out.println(u);
//                }
//
//                System.out.println("Transitive Clousure:");

                rg.findTransitiveClosure();
                writeBufferToFile("/Users/marcoschaim/projetos/data/BOBYQAOptimizer/", mi.getName()+".dot",rg.toDot());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        }

    void writeBufferToFile(String dir, String name, String s) {
        // Convert the string to a
        // byte array.

        byte data[] = s.getBytes();
        Path p = Paths.get(dir+name);

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println("Cannot open file "+(dir+name));
        }
    }

}
