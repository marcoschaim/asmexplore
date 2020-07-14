package br.usp.each.saeg.subsumption.cli;

import br.usp.each.saeg.commons.time.TimeWatch;
import br.usp.each.saeg.subsumption.analysis.SubsumptionGraph;
import br.usp.each.saeg.subsumption.analysis.SubsumptionAnalyzer;
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

    public static int edgeSubsumeAll(InputStream input, String path) {
        int n = 0; // # of methods analyzed
        try {
            ClassInfo ci = new ClassInfo(input);
            path = path + File.separator;
            for (MethodInfo mi : ci.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                if (mi.getDuas().isEmpty())
                    continue;

                // Create a name for the files based on the class and method names

                String methodname = ci.getName().substring(0).replace(File.separator,".")+"." + mi.getName();

                final TimeWatch tw = TimeWatch.start();
                SubsumptionAnalyzer duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(),mi.getDuas());
                Graphdua grd = duaSubAnalyzer.findEdge2DuasSubsumption();
                writeBufferToFile(path, methodname + ".es",grd.toDotEdgeSubsumption(duaSubAnalyzer));
                final long seconds = tw.time(TimeUnit.SECONDS);

                System.out.println("\n#"+ ci.getName() +File.separator+ mi.getName() + ":");
                System.out.println(MessageFormat.format(
                        "Edge Method {0} subsumption of duas calculated in {1} minutes and {2} seconds", methodname, seconds/60,seconds % 60));

                n++;
            }
        } catch (Exception e) {
            System.err.println("Failed to analyze: " + path);
        }
        return n;
    }

    static void writeBufferToFile(String dir, String name, String s) {
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
