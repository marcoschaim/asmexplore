package br.usp.each.saeg.subsumption.input;

import junit.framework.TestCase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClassInfoTest extends TestCase {
    ClassInfo cl;

    //@Test
    public void test1() {
        System.out.println("Teste");
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/sort/", "Sort.class");
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
                mi.printMethodCFG();
                mi.printMethodDuas();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test12() {
        System.out.println("Teste");
        try {
            cl = new ClassInfo("/Users/marcoschaim/projetos/data/max/", "Max.class");
            cl.genAllMethodInfo();
            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(cl.toJsonSubsumption());
        System.out.println(cl.toJsonDuas());
    }

    //@Test
//    public void test2() {
//        System.out.println("MethodInfo");
//        String dir = "/Users/marcoschaim/projetos/data/MethodInfo/";
//        String clazzname = "MethodInfo.class";
//        try {
//            cl = new ClassInfo(dir, clazzname);
//            cl.genAllMethodInfo();
//
//            for (MethodInfo mi : cl.getMethodsInfo()) {
//                mi.createMethodCFG();
//                mi.createMethodDuas();
////                mi.printMethodCFG();
////                writeBufferToFile(dir, mi.getName() + ".csv", mi.printMethodDuas());
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(cl.toJsonSubsumption());
//        System.out.println(cl.toJsonDuas());
//    }

    //@Test
    public void test3() {
        System.out.println("BOBYQAOptimizer");
        String dir = "/Users/marcoschaim/projetos/data/bobyqb/";
        String clazzname = "BOBYQAOptimizer.class";
        try {
            cl = new ClassInfo(dir, clazzname);
            cl.genAllMethodInfo();

            for (MethodInfo mi : cl.getMethodsInfo()) {
                mi.createMethodCFG();
                mi.createMethodDuas();
//                mi.printMethodCFG();
                //writeBufferToFile(dir, mi.getName() + ".csv", mi.printMethodDuas());
            }
//            System.out.println(cl.toJsonSubsumption());
//            System.out.println(cl.toJsonDuas());
            writeBufferToFile(dir, cl.getName().replace(File.separator, ".") + ".sub.json", cl.toJsonSubsumption());
            writeBufferToFile(dir, cl.getName().replace(File.separator, ".") + ".dua.json", cl.toJsonDuas());
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
