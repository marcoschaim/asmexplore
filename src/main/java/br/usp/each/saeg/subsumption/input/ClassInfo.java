package br.usp.each.saeg.subsumption.input;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ClassInfo {
    ClassReader cr;
    ClassNode cn;
    String dir;
    String clazzname;
    int access;

    final List<MethodInfo> methods = new LinkedList<>();
    final HashMap<String, MethodInfo> mapMethod = new HashMap<>();

    public ClassInfo(String dir, String clazzname) {
        this.dir = clazzname;
        this.clazzname = clazzname;

        String path = dir+clazzname;

        try {

            File f = new File(path);
            if (!f.exists()) {
                System.out.println("Class file " + path + "does not exists");
                throw new IOException();
            }

            byte[] bytesArray = new byte[(int) f.length()];

            FileInputStream fis = new FileInputStream(f);

            fis.read(bytesArray);
            fis.close();

            cr = new ClassReader(bytesArray);
            cn = new ClassNode();
            cr.accept(cn, 0);
            access = cr.getAccess();

        } catch (IOException e) {
            System.out.println("Failed to open class file: " + path);
        }
    }

    public ClassInfo(InputStream f) {
        try {
            cr = new ClassReader(f);
            cn = new ClassNode();
            cr.accept(cn, 0);
            access = cr.getAccess();
            genAllMethodInfo();
        } catch (IOException | AnalyzerException e) {
            System.out.println("Failed to open class file:"+cn.name);
        }
    }

    public void genAllMethodInfo() throws AnalyzerException {
        for (MethodNode m : cn.methods) {
            // Does not analyze:
            // 1. Interfaces
            if ((access & Opcodes.ACC_INTERFACE) != 0)
                return;
            // 2. Abstract methods
            else if ((access & Opcodes.ACC_ABSTRACT) != 0)
                return;
            // 3. Static class initialization
            else if (m.name.equals("<clinit>"))
                return;

            MethodInfo mi = new MethodInfo(cn.name,m);
            methods.add(mi);
            mapMethod.put(m.signature,mi);
        }

    }

    public List<MethodInfo> getMethodsInfo() { return methods;}

    public String getName() { return cn.name;}
}
