package br.usp.each.saeg.subsumption.cli;

import br.usp.each.saeg.commons.time.TimeWatch;
import br.usp.each.saeg.subsumption.analysis.SubsumptionAnalyzer;
import br.usp.each.saeg.subsumption.analysis.SubsumptionGraph;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.input.ClassInfo;
import br.usp.each.saeg.subsumption.input.MethodInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class EdgeSubsumer {
    static private SubsumptionGraph sg;

    public static int edgeSubsumeAll(File src, InputStream input, String path) {
        int n = 0; // # of methods analyzed
        boolean printLocalDuaEdgeFile = false;
        boolean printDuaJsonEdgeFile = true;
        try {
            ClassInfo ci = new ClassInfo(input);
            path = path + File.separator;

            for (MethodInfo mi : ci.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                // Create a name for the files based on the class and method names

                String methodname = ci.getName().replace(File.separator, ".") + "." + mi.getName();
//                System.out.println(methodname+":");
                if (mi.getDuas().isEmpty())
                    continue;

                if (mi.getHasIncomingEdges()) {
                    System.out.println("Warning: Method:" + methodname + " has incoming edges.");
//                    System.out.println(mi.graphDefUseToDot());
                    continue;
                }

                if (mi.getHasAutoEdge()) {
                    System.out.println("Warning: Method:" + methodname + " has auto edges.");
                    //System.out.println(mi.graphDefUseToDot());
                    //System.out.println(mi.toJsonDuas(new StringBuffer()));
                    continue;
                }

                final TimeWatch tw = TimeWatch.start();
                SubsumptionAnalyzer duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(), mi.getDuas());
                Graphdua grd = duaSubAnalyzer.findEdge2DuasSubsumption();
//                System.out.println(grd.toDot());
                final long milliseconds;
                milliseconds = tw.time(TimeUnit.MILLISECONDS);

                mi.setGraphDua(grd);
                mi.setSubsumptionAnalyzer(duaSubAnalyzer);

                System.out.println("\n#" + ci.getName() + File.separator + mi.getName() + ":");

                if (printLocalDuaEdgeFile && !printDuaJsonEdgeFile) {
                    writeBufferToFile(path, methodname + ".es", grd.toDotEdgeSubsumption(duaSubAnalyzer));
                } else {
                    if (!printLocalDuaEdgeFile && !printDuaJsonEdgeFile) {
                        int noSubmedDuas = grd.getAllDuasSubsumedEdge(duaSubAnalyzer).cardinality();
//                        milliseconds = tw.time(TimeUnit.MILLISECONDS);
                        System.out.println("## nodes: " + mi.getProgram().getGraph().size());
                        System.out.println("## edges: " + mi.getProgram().getGraph().sizeEdges());
                        System.out.println("## DUAs: " + mi.getDuas().size());
                        System.out.println("## Subsumed DUAs: " + noSubmedDuas);
                        System.out.println("## Edge DUA coverage: " + ((double) noSubmedDuas / mi.getDuas().size()) * 100);
                        System.out.println("@@ " + methodname + "," + mi.getProgram().getGraph().size() + "," + mi.getProgram().getGraph().sizeEdges() + "," + mi.getDuas().size() + "," + noSubmedDuas + "," + ((double) noSubmedDuas / mi.getDuas().size()) * 100 + "," + milliseconds / 1000 + "," + milliseconds + "\n");
                    }
                }

                System.out.println("\n#" + ci.getName() + File.separator + mi.getName() + ":");
                System.out.println(MessageFormat.format(
                        "Local DUA-Edge subsumption of method {0}  calculated in {1} minutes, {2} seconds, and {3} milliseconds", methodname, (milliseconds / 1000) / 60, (milliseconds / 1000) % 60, milliseconds));
                n++;
            }
            if (printDuaJsonEdgeFile) {

                writeBufferToFile(path, ci.getName().replace(File.separator, ".") + ".edges.json", ci.toJsonEdges());
                writeBufferToFile(path, ci.getName().replace(File.separator, ".") + ".duas2edges.json", ci.toJsonDuas2Edges());
                writeBufferToFile(path, ci.getName().replace(File.separator, ".") + ".edgesub.json", ci.toJsonEdgeSubsumption());
            }
        } catch (Exception e) {
            String failfile = src.getPath();
            if (failfile.contains(".class"))
                System.out.println("Fail to analyze: " + failfile);
        }
        return n;
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
            System.err.println("Cannot open file "+(dir+name));
        }
    }
}
