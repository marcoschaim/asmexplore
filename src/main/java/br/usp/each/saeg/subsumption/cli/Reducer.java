package br.usp.each.saeg.subsumption.cli;

import br.usp.each.saeg.commons.time.TimeWatch;
import br.usp.each.saeg.subsumption.analysis.ReductionGraph;
import br.usp.each.saeg.subsumption.analysis.SubsumptionGraph;
import br.usp.each.saeg.subsumption.input.ClassInfo;
import br.usp.each.saeg.subsumption.input.MethodInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class Reducer {
    static private SubsumptionGraph sg;
    static private ReductionGraph rg;

    public static int reduceAll(InputStream input, String path) {
        int n = 0; // # of methods analyzed
        StringBuffer sb = new StringBuffer();
        boolean printReductionInfo = false;
        try {
            ClassInfo ci = new ClassInfo(input);
            path = path + File.separator;

            if (ci.getName().replaceAll(File.separator, ".").equals("org.apache.commons.math3.genetics.RandomKey"))
                System.out.println();

            for (MethodInfo mi : ci.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                if (mi.getDuas().isEmpty())
                    continue;
                else if (mi.getName().equals("zzUnpackAction"))
                    System.out.println();

                if (printReductionInfo) {
                    // Create a name for the files based on the class and method names

                    String methodname = ci.getName().replace(File.separator, ".") + "." + mi.getName();

                    System.out.println("\n#" + ci.getName() + File.separator + mi.getName() + ":");

                    final TimeWatch tw = TimeWatch.start();
                    sg = new SubsumptionGraph(mi.getProgram(), mi.getDuas());

                    rg = new ReductionGraph(sg);
                    rg.setDua2DefUseChains(mi.getDefChainsMap());
                    rg.setLines(mi.getLines());

                    rg.findTransitiveClosure();
                    final long milliseconds = tw.time(TimeUnit.MILLISECONDS);

                    mi.setReductionGraph(rg);

                    System.out.println(MessageFormat.format(
                            "Method {0} reduced in {1} minutes and {2} seconds. Total em milliseconds {3}", mi.getName(), (milliseconds / 1000) / 60, (milliseconds / 1000) % 60, milliseconds));
                    System.out.println("## duas: " + mi.getDuas().size());
                    System.out.println("## Unconstrained duas: " + rg.unconstrainedNodes().size());
                    System.out.println("## Reduction nodes: " + rg.size());
                    System.out.println("@@ " + methodname + mi.getProgram().getGraph().size() + "," + mi.getDuas().size() + "," + rg.unconstrainedNodes().size() + "," + rg.size() + "," + ((double) rg.unconstrainedNodes().size() / mi.getDuas().size()) * 100 + "," + ((double) rg.size() / mi.getDuas().size()) * 100 + "," + milliseconds / 1000 + "," + milliseconds + "\n");
                    sb.append(methodname + ";" + mi.getDuas().size() + ";" + rg.unconstrainedNodes().size() + ";" + rg.size() + ";" + milliseconds + ";\n");
                    System.out.println("sb:" + sb.toString());

                    //writeBufferToFile(path, methodname + ".red", rg.toDot());
                }
                n++;
            }
            if (!printReductionInfo) {
                writeBufferToFile(path, ci.getName().replace(File.separator, ".") + ".duas.json", ci.toJsonDuas());
                writeBufferToFile(path, ci.getName().replace(File.separator, ".") + ".sub.json", ci.toJsonSubsumption());
            }
            // writeBufferToFile(path,"reduce.csv", sb.toString());
        } catch (Exception e) {
            System.err.println("Failed to analyze: " + path);
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
            System.err.println("Cannot open file " + (dir + name));
        }
    }
}
