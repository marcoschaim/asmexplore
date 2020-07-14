package br.usp.each.saeg.subsumption.input;

import br.usp.each.saeg.asm.defuse.*;

import br.usp.each.saeg.opal.Program;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import br.usp.each.saeg.opal.Block;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

public class DataControlFlowInfoGenerator {
    ClassReader cr;
    ClassNode cn;
    int[][] successors;
    int[][] predecessors;
    int[][] basicBlocks;
    int[] leaders;
    String path;
    String dir;
    String clazzname;
    MethodNode curMn;
    String curOwner;

    FlowAnalyzer<BasicValue> analyzer;
    Program program;

    public DataControlFlowInfoGenerator(String dir, String clazzname) {

        this.dir = dir;
        this.clazzname = clazzname;

        path = dir + clazzname;

        try {

            File f = new File(path);
            if (!f.exists()) {
                System.out.println("Class file " + path + "does not exists");
                return;
            }

            byte[] bytesArray = new byte[(int) f.length()];

            FileInputStream fis = new FileInputStream(f);

            fis.read(bytesArray);
            fis.close();

            cr = new ClassReader(bytesArray);
            cn = new ClassNode();
            cr.accept(cn, 0);

            analyzer = new FlowAnalyzer<BasicValue>(new BasicInterpreter());
            program = new Program();

        } catch (IOException e) {
            System.out.println("Failed to open class file: " + path);
        }
    }

    void genFlowGraph(String owner, MethodNode mn) throws AnalyzerException {
        this.curMn = mn;
        curOwner = owner;
        System.out.println("owner:" + owner);
        System.out.println(curMn.name);

        analyzer.analyze(owner, curMn);
        successors = analyzer.getSuccessors();
        predecessors = analyzer.getPredecessors();
        basicBlocks = analyzer.getBasicBlocks();
        leaders = analyzer.getLeaders();

        boolean visited[] = new boolean[curMn.instructions.size()];
        for (int i = 0; i < leaders.length; ++i)
            visited[i] = false;


        System.out.println("Criando o GFC:");

        for (int i = 0; i < basicBlocks.length; i++) {
            //System.out.println("Basic block " + i + " contains " + basicBlocks[i].length + " instructions");
            program.getGraph().add(new Block(i));
        }
        for (int i = 0; i < curMn.instructions.size(); i++) {
            //System.out.println("Instruction " + i + " belongs to basic block " + leaders[i]);
            program.getGraph().get(leaders[i]).line(i);
        }

        if (curMn.instructions.size() > 0)
            visitInstruction(program, 0, visited);

        System.out.println("blocks:");
        final Iterator<Block> it = program.getGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            System.out.println("Block(" + blk.id() + "):" + blk.lines());
            System.out.print("\tSuccessors:");
            for (Block suc : program.getGraph().neighbors(blk.id())) {
                System.out.print(" " + suc.id());
            }
            System.out.print("\n");
        }

        return;
    }

    void visitInstruction(Program p, int ins, boolean[] vis) {
        vis[ins] = true;
        for (int suc : successors[ins]) {
            if (leaders[ins] != leaders[suc]) {
                if (!p.getGraph().adjacent(leaders[ins], leaders[suc])) {
                    p.getGraph().addEdge(leaders[ins], leaders[suc]);
                }
            }
            if (!vis[suc])
                visitInstruction(p, suc, vis);
        }
    }

    public void genAllFlowGraphs() throws AnalyzerException {
        System.out.println("Class:" + cn.name);
        for (MethodNode m : cn.methods) {
            System.out.println("GFC (" + m.name + ")");
            genFlowGraph(cn.name, m);
            genDuas(cn.name, m);
        }

    }

    void genDuas(String owner, MethodNode mn) throws AnalyzerException {
        curMn = mn;
        DefUseInterpreter interpreter = new DefUseInterpreter();
        FlowAnalyzer<Value> flowAnalyzer = new FlowAnalyzer<Value>(interpreter);
        DefUseAnalyzer analyzer = new DefUseAnalyzer(flowAnalyzer, interpreter);
        analyzer.analyze(owner, mn);

        final Variable[] variables = analyzer.getVariables();
        // Instructions by line number
        final int[] lines = getLines();
        final DefUseFrame[] frames = analyzer.getDefUseFrames();

        for (int i = 0; i < frames.length; ++i) {
            System.out.println("DFframe[" + i + "]:" + frames[i]);
        }

        if (frames.length == curMn.instructions.size())
            System.out.println("#frames == #instrucoes");
        else
            System.out.println("#frames != #instrucoes");

        for (int i = 0; i < variables.length; ++i)
            System.out.println("Variables[" + i + "]: " + variables[i].getVariables());

        for (LocalVariableNode lvn : mn.localVariables)
            System.out.println("Variables[" + lvn.index + "]: " + lvn.name);

        DefUseChain[] chains = new DepthFirstDefUseChainSearch().search(
                analyzer.getDefUseFrames(),
                analyzer.getVariables(),
                flowAnalyzer.getSuccessors(),
                flowAnalyzer.getPredecessors());

        // Only global DU
        final DefUseChain[] globalChains = DefUseChain.globals(chains,
                flowAnalyzer.getLeaders(), flowAnalyzer.getBasicBlocks());

        // DU by basic block (the ones we monitor)
        final DefUseChain[] blockChains = DefUseChain.toBasicBlock(chains,
                flowAnalyzer.getLeaders(), flowAnalyzer.getBasicBlocks());

        for (final DefUseChain c : globalChains) {
            System.out.println(c);
            program.getGraph().get(leaders[c.def]).def(c.var);
            System.out.println("def of var " + c.var + " in block " + leaders[c.def]);
            if (c.isComputationalChain()) {
                program.getGraph().get(leaders[c.use]).cuse(c.var);
                System.out.println("cuse of var " + c.var + " in block " + leaders[c.use]);
//                System.out.println("("+lines[c.def] + "," + lines[c.use] + "," + getVar(c, variables)+")");
            } else {
                program.getGraph().get(leaders[c.use]).puse(c.var);
                System.out.println("puse of var " + c.var + " in block " + leaders[c.use]);
//                System.out.println("("+lines[c.def]+",(" + lines[c.use]+"," +  lines[c.target] + ")," + getVar(c, variables)+")");
            }
        }


//        System.out.println("This method contains " + chains.length + " Definition-Use Chains");
//        for (int i = 0; i < globalChains.length; i++) {
//            DefUseChain chain = chains[i];
//            System.out.println("Instruction " + chain.def + " define variable " + variables[chain.var]);
//            System.out.println("Instruction " + chain.use + " uses variable " + variables[chain.var]);
//            // There is a path between chain.def and chain.use that not redefine chain.var
//        }
    }

    public int[] getLines() {
        final int[] lines = new int[curMn.instructions.size()];
        Arrays.fill(lines, -1);
        for (int i = 0; i < curMn.instructions.size(); i++) {
            if (curMn.instructions.get(i).getType() == AbstractInsnNode.LINE) {
                final LineNumberNode line = (LineNumberNode) curMn.instructions.get(i);
                Arrays.fill(lines, curMn.instructions.indexOf(line.start), lines.length, line.line);
            }
        }
        return lines;
    }

    public String getVar(final DefUseChain c, final Variable[] vars) {
        System.out.println("Variables[" + c.var + "]: " + vars[c.var].getVariables());
        return getVar(vars[c.var], c.use);
    }

    public String getVar(final Value v, final int insn) {
        if (v instanceof StaticField) {
            return ((StaticField) v).name;
        } else if (v instanceof ObjectField) {
            System.out.println("Instance of Object");
            final ObjectField objField = (ObjectField) v;
            final String var = getVar(objField.getRoot(), insn);
            if (var != null) {
                return String.format("%s.%s", var, objField.name);
            }
        } else if (v instanceof Local) {
            System.out.println("Instance of local" + (Local) v);
            return getVar((Local) v, insn);
        }
        return null;
    }

    public String getVar(final Local local, final int insn) {
        System.out.println("LocalVariables:" + curMn.localVariables);
        for (final LocalVariableNode lvn : curMn.localVariables) {
            System.out.println(lvn.index);
            if (lvn.index == local.var
                    && insn >= curMn.instructions.indexOf(lvn.start)
                    && insn < curMn.instructions.indexOf(lvn.end)) {
                return lvn.name;
            }
        }
        return null;
    }

}
