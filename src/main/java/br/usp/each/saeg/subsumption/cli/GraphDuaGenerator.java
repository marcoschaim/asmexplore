package br.usp.each.saeg.subsumption.cli;

import br.usp.each.saeg.commons.time.TimeWatch;
import br.usp.each.saeg.subsumption.graphdua.CoverageAnalyzer;
import br.usp.each.saeg.subsumption.graphdua.Dua;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.input.ClassInfo;
import br.usp.each.saeg.subsumption.input.MethodInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class GraphDuaGenerator {
    public static int generateAll(InputStream input, String path) {
        int n = 0; // # of methods analyzed
        String methodname;
        try {
            ClassInfo ci = new ClassInfo(input);
            path = path + File.separator;
            for (MethodInfo mi : ci.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();

                if (mi.getDuas().isEmpty())
                    continue;

                // Create a name for the files based on the class and method names

                methodname = ci.getName().replace(File.separator, ".") + "." + mi.getName();

                //SubsumptionAnalyzer duaSubAnalyzer = new SubsumptionAnalyzer(mi.getProgram(),mi.getDuas());

                int id = 1;  final TimeWatch tw = TimeWatch.start();
                for (Dua d: mi.getDuas()) {
                    CoverageAnalyzer analyzer = new CoverageAnalyzer(mi.getProgram().getGraph(), mi.getProgram().getInvGraph(), d);
                    Graphdua grd = analyzer.findGraphdua();
                    //writeBufferToFile(path, methodname+ "-grd"+id+".giz",grd.toDot());
                    ++id;
                }
                final long milliseconds = tw.time(TimeUnit.MILLISECONDS);
                final long seconds = tw.time(TimeUnit.SECONDS);
                System.out.println("\n#"+ ci.getName() +File.separator+ mi.getName() + ":");
                System.out.println(MessageFormat.format(
                        "Method {0} graphduas were calculated in {1} minutes and {2} seconds", methodname, seconds/60,seconds % 60));

                System.out.println("@@ " + methodname + "," + mi.getProgram().getGraph().size() + "," + mi.getDuas().size() + "," + milliseconds / 1000 + "," + milliseconds + "\n");

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
