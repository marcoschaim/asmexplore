package br.usp.each.saeg.subsumption.input;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClassInfoTest extends TestCase {
ClassInfo cl;
    //@Test
    public void test1(){
        System.out.println("Teste");
        try {
        cl = new ClassInfo("/Users/marcoschaim/projetos/data/sort/src/main/java/br/usp/each/saeg/", "Sort.class");
        cl.genAllMethodInfo();

        for(MethodInfo mi: cl.getMethodsInfo()){
            mi.printMethodCFG();
            mi.printMethodDuas();
        }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //@Test
    public void test2() {
        System.out.println("MethodInfo");
        String dir = "/Users/marcoschaim/projetos/data/MethodInfo/";
        String clazzname = "MethodInfo.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for(MethodInfo mi: cl.getMethodsInfo()){
                mi.printMethodCFG();
                writeBufferToFile(dir, mi.getName()+".csv", mi.printMethodDuas());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void test3() {
        System.out.println("BOBYQAOptimizer");
        String dir = "/Users/marcoschaim/projetos/data/BOBYQAOptimizer/";
        String clazzname = "BOBYQAOptimizer.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for(MethodInfo mi: cl.getMethodsInfo()){
                mi.printMethodCFG();
                writeBufferToFile(dir, mi.getName()+".csv", mi.printMethodDuas());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void writeBufferToFile(String dir, String name, String s) {
        // Convert the string to a
        // byte array.

        byte data[] = s.getBytes();
        Path p = Paths.get(dir + name);

        try (OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p))) {
            out.write(data, 0, data.length);
        } catch (IOException x) {
            System.err.println(x);
        }
    }


}
