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
import java.util.Iterator;
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

        String path = dir + clazzname;

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
            System.out.println("Failed to open class file:" + cn.name);
        }
    }

    public void genAllMethodInfo() throws AnalyzerException {
        // Does not analyze:
        // 1. Interfaces
        if ((access & Opcodes.ACC_INTERFACE) != 0)
            return;

        for (MethodNode m : cn.methods) {
            // 2. Abstract methods
            if ((m.access & Opcodes.ACC_ABSTRACT) != 0)
                continue;
                // 3. Static class initialization
            else if (m.name.equals("<clinit>"))
                continue;

            MethodInfo mi = new MethodInfo(cn.name, m);
            methods.add(mi);
            mapMethod.put(m.signature, mi);
        }

    }

    public String toJsonSubsumption() {
        StringBuffer sb = new StringBuffer();

        String methodname = getName().replace(File.separator, ".");

        sb.append("{\n\"Class\" : " + "\"" + methodname + "\", \n\"Methods\" : [");

        boolean first = true;
        System.out.println(methodname + ":");

        Iterator<MethodInfo> it = getMethodsInfo().iterator();
        while (it.hasNext()) {
            MethodInfo mi = it.next();
            if (mi.getDuas().isEmpty())
                continue;

            if (first)
                first = false;
            else
                sb.append(",");

            mi.toJsonSubsumption(sb);
        }

        sb.append("]\n}");

        return sb.toString();
    }

    public String toJsonDuas() {
        StringBuffer sb = new StringBuffer();

        String methodname = getName().replace(File.separator, ".");

        sb.append("{\n\"Class\" : " + "\"" + methodname + "\", \n\"Methods\" : [");

        boolean first = true;
        for (MethodInfo mi : getMethodsInfo()) {
            if (mi.getDuas().isEmpty()) continue;
            if (first) {
                first = false;
            } else
                sb.append(",");
            mi.toJsonDuas(sb);
        }
        sb.append("]\n}");
        return sb.toString();
    }

    public String toJsonDuas2Nodes() {
        StringBuffer sb = new StringBuffer();

        String methodname = getName().replace(File.separator, ".");

        sb.append("{\n\"Class\" : " + "\"" + methodname + "\", \n\"Methods\" : [");

        boolean first = true;
        for (MethodInfo mi : getMethodsInfo()) {
            if (mi.getDuas().isEmpty()) continue;
            if (first) {
                first = false;
            } else
                sb.append(",");
            mi.toJsonDuas2Nodes(sb);
        }
        sb.append("]\n}");
        return sb.toString();
    }

    public String toJsonDuas2Edges() {
        StringBuffer sb = new StringBuffer();

        String methodname = getName().replace(File.separator, ".");

        sb.append("{\n\"Class\" : " + "\"" + methodname + "\", \n\"Methods\" : [");

        boolean first = true;
        for (MethodInfo mi : getMethodsInfo()) {
            if (mi.getDuas().isEmpty()) continue;
            if (first) {
                first = false;
            } else
                sb.append(",");
            mi.toJsonDuas2Edges(sb);
        }
        sb.append("]\n}");
        return sb.toString();
    }

    public String toJsonNodeSubsumption() {
        StringBuffer sb = new StringBuffer();

        String methodname = getName().replace(File.separator, ".");

        sb.append("{\n\"Class\" : " + "\"" + methodname + "\", \n\"Methods\" : [");

        boolean first = true;
        System.out.println(methodname + ":");

        Iterator<MethodInfo> it = getMethodsInfo().iterator();
        while (it.hasNext()) {
            MethodInfo mi = it.next();
            if (mi.getDuas().isEmpty())
                continue;

            if (first)
                first = false;
            else
                sb.append(",");

            mi.toJsonNodeSubsumption(sb);

        }

        sb.append("]\n}");

        return sb.toString();
    }

    public String toJsonNodes() {
        StringBuffer sb = new StringBuffer();

        String methodname = getName().replace(File.separator, ".");

        sb.append("{\n\"Class\" : " + "\"" + methodname + "\", \n\"Methods\" : [");

        boolean first = true;
        for (MethodInfo mi : getMethodsInfo()) {
            if (mi.getDuas().isEmpty()) continue;
            if (first) {
                first = false;
            } else
                sb.append(",");
            mi.toJsonNodes(sb);
        }
        sb.append("]\n}");
        return sb.toString();
    }

    public String toJsonEdgeSubsumption() {
        StringBuffer sb = new StringBuffer();

        String classname = getName().replace(File.separator, ".");

        sb.append("{\n\"Class\" : " + "\"" + classname + "\", \n\"Methods\" : [");

        boolean first = true;
//        System.out.println(classname + ":");
//        int noMethod = 0;
        Iterator<MethodInfo> it = getMethodsInfo().iterator();
        while (it.hasNext()) {
            MethodInfo mi = it.next();

            if (mi.getDuas().isEmpty()) {
//                System.out.println("Method("+noMethod+") WITHOUT duas: "+mi.getName());
                continue;
            }
//            else
//                System.out.println("Method("+noMethod+") with duas: "+mi.getName());
//            ++noMethod;

            if (first)
                first = false;
            else
                sb.append(",");

            mi.toJsonEdgeSubsumption(sb);

        }

        sb.append("]\n}");

        return sb.toString();
    }

    public String toJsonEdges() {
        StringBuffer sb = new StringBuffer();

        String methodname = getName().replace(File.separator, ".");

        sb.append("{\n\"Class\" : " + "\"" + methodname + "\", \n\"Methods\" : [");

        boolean first = true;
        for (MethodInfo mi : getMethodsInfo()) {
            if (mi.getDuas().isEmpty()) continue;
            if (first) {
                first = false;
            } else
                sb.append(",");
            mi.toJsonEdges(sb);
        }
        sb.append("]\n}");
        return sb.toString();
    }

    public List<MethodInfo> getMethodsInfo() {
        return methods;
    }

    public String getName() {
        return cn.name;
    }


}
