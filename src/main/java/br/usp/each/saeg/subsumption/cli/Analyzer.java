package br.usp.each.saeg.subsumption.cli;

import br.usp.each.saeg.subsumption.input.ClassInfo;
import br.usp.each.saeg.subsumption.input.MethodInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Analyzer {

    public static int analyzeAll(InputStream input, String path) {
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

                methodname = ci.getName().replace(File.separator, ".") + mi.getName();

                System.out.println(methodname + " " + "#Nodes: " + mi.getProgram().getGraph().size());
                writeBufferToFile(path, methodname + ".csv", mi.toDuasCSV());
                writeBufferToFile(path, methodname + ".gz", mi.graphDefUseToDot());

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
