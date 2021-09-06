package br.usp.each.saeg.subsumption.input;

import br.usp.each.saeg.asm.defuse.*;
import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Program;
import br.usp.each.saeg.subsumption.analysis.ReductionGraph;
import br.usp.each.saeg.subsumption.analysis.ReductionNode;
import br.usp.each.saeg.subsumption.analysis.SubsumptionAnalyzer;
import br.usp.each.saeg.subsumption.analysis.SubsumptionGraph;
import br.usp.each.saeg.subsumption.graphdua.Dua;
import br.usp.each.saeg.subsumption.graphdua.Edge;
import br.usp.each.saeg.subsumption.graphdua.Graphdua;
import br.usp.each.saeg.subsumption.graphdua.Node;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.*;

public class MethodInfo {
    Program p;
    MethodNode mn;
    String name;
    String signature;
    int[][] successors;
    int[][] predecessors;
    int[][] basicBlocks;
    int[] leaders;
    String path;
    String dir;
    Variable[] vars;
    int[] lines;
    DefUseChain[] globalChains;
    DefUseChain[] blockChains;
    List<Dua> duas;
    HashMap<Integer, List<DefUseChain>> dua2DefUseChains = new HashMap<>();
    HashMap<DefUseChain, Integer> idDefUseChain = new HashMap<>();
    HashMap<Integer, List<Integer>> dua2idDefUseChains = new HashMap<>();
    Map<DefUseChain, Dua> defUseChain2Dua = new HashMap<>();

    Map<Integer, Edge> idEdgesMap = new HashMap<>();
    Map<Edge, Integer> edgesIdMap = new HashMap<>();

    ReductionGraph rg = null;
    SubsumptionGraph sg = null;
    Graphdua sne = null;
    boolean hasIncomingEdges = false;
    boolean hasAutoEdge = false;
    boolean hasDanglingNodes = false;
    String owner;
    private SubsumptionAnalyzer sa;


    public MethodInfo(String owner, MethodNode mn) {
        this.owner = owner;
        this.mn = mn;
        this.name = mn.name;
        this.signature = mn.signature;
        p = new Program();
        duas = new LinkedList<>();
        //dua2DefUseChains = new HashMap<>();
    }

    public MethodInfo(String name, Program prg, List<Dua> lstduas) {
        this.name = name;
        p = prg;
        vars = new Variable[prg.numberOfVars()];
        duas = lstduas;
        //dua2DefUseChains = new HashMap<>();
    }

    public void createMethodCFG() throws AnalyzerException {
        FlowAnalyzer<BasicValue> analyzer = new FlowAnalyzer<BasicValue>(new BasicInterpreter());
        analyzer.analyze(owner, mn);

        successors = analyzer.getSuccessors();
        predecessors = analyzer.getPredecessors();
        basicBlocks = analyzer.getBasicBlocks();
        leaders = analyzer.getLeaders();

        boolean[] visited = new boolean[mn.instructions.size()];
        for (int i = 0; i < leaders.length; ++i)
            visited[i] = false;

        for (int i = 0; i < basicBlocks.length; i++) {
            p.getGraph().add(new Block(i));
            p.getInvGraph().add(new Block(i));
        }

        for (int i = 0; i < mn.instructions.size(); i++) {
            if (leaders[i] >= 0) {
                p.getGraph().get(leaders[i]).line(i);
                p.getInvGraph().get(leaders[i]).line(i);
            }
        }

        // Connect nodes through edges
        if (mn.instructions.size() > 0)
            visitInstruction(p, 0, visited);

        p.getGraph().setEntry(0);
        p.getGraph().setExit(new Block(basicBlocks.length)); // added single new exit node

        p.getInvGraph().setExit(0);
        p.getInvGraph().setEntry(new Block(basicBlocks.length)); // added single new entry node

        // Connect all exit (entry) nodes to the new exit (entry) node

        boolean[] visitedBlks = new boolean[basicBlocks.length];

        for (int i = 0; i < basicBlocks.length; ++i)
            visitedBlks[i] = false;

        connectNewExitNode(p.getGraph().entry(), visitedBlks);

        if (!p.getGraph().revNeighbors(0).isEmpty()) {
            hasIncomingEdges = true;
        }

        if (!checkNodeReachability(p))
            hasDanglingNodes = true;

        // Create map of edges

        Iterator<Block> itblk = p.getGraph().iterator();
        idEdgesMap.clear();
        edgesIdMap.clear();

        int idedge = 0;

        while (itblk.hasNext()) {
            Block pred = itblk.next();
            for (Block suc : p.getGraph().neighbors(pred.id)) {
                Edge e = new Edge(pred, suc);
                if (!idEdgesMap.containsValue(e)) {
                    idEdgesMap.put(idedge, e);
                    edgesIdMap.put(e, idedge);
                    idedge++;
                }
            }
        }
    }

    void visitInstruction(Program p, int ins, boolean[] vis) {
        vis[ins] = true;

        for (int suc : successors[ins]) {
            if (leaders[ins] != leaders[suc]) {
                if (!p.getGraph().adjacent(leaders[ins], leaders[suc])) {
                    p.getGraph().addEdge(leaders[ins], leaders[suc]);
                    p.getInvGraph().addEdge(leaders[suc], leaders[ins]);
                }
            }
            if (!vis[suc])
                visitInstruction(p, suc, vis);
        }
    }

    boolean checkNodeReachability(Program p) {
        boolean vis = true;
        Iterator<Block> it = p.getGraph().iterator();
        while (it.hasNext()) {
            Block b = it.next();
            if (p.getGraph().entry().id() != b.id()) {
                if (p.getGraph().revNeighbors(b.id()).isEmpty()) {
                    vis = false;
                    break;
                }
            }
            if (p.getGraph().exit().id() != b.id()) {
                if (p.getGraph().neighbors(b.id()).isEmpty()) {
                    vis = false;
                    break;
                }
            }
        }
        return vis;
    }

    void connectNewExitNode(Block node, boolean[] vis) {
        vis[node.id()] = true;

        for (Block n : p.getGraph().neighbors(node.id())) {
            if (!vis[n.id()]) {
                connectNewExitNode(n, vis);
            }
        }

        if (p.getGraph().neighbors(node.id()).isEmpty()) {
            p.getGraph().addEdge(node.id(), p.getGraph().exit().id());
            p.getInvGraph().addEdge(p.getGraph().exit().id(), node.id());
        }
    }

    public void printMethodCFG() {

        System.out.println("CFG(" + mn.name + "):");
        System.out.println("blocks:");
        Iterator<Block> it = p.getGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            System.out.println("Block(" + blk.id() + "):" + blk.lines());
            System.out.print("\tSuccessors:");
            for (Block suc : p.getGraph().neighbors(blk.id())) {
                System.out.print(" " + suc.id());
            }
            System.out.print("\n");
        }

        System.out.println("invCFG(" + mn.name + "):");
        System.out.println("blocks:");
        it = p.getInvGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            System.out.println("Block(" + blk.id() + "):" + blk.lines());
            System.out.print("\tInv Successors:");
            for (Block pred : p.getInvGraph().neighbors(blk.id())) {
                System.out.print(" " + pred.id());
            }
            System.out.print("\n");
        }

        p.getGraph().printReversePostOrder();
        p.getInvGraph().printReversePostOrder();

    }

    public void createMethodDuas() throws AnalyzerException {

        DefUseInterpreter interpreter = new DefUseInterpreter();
        FlowAnalyzer<Value> flowAnalyzer = new FlowAnalyzer<Value>(interpreter);
        DefUseAnalyzer analyzer = new DefUseAnalyzer(flowAnalyzer, interpreter);
        analyzer.analyze(owner, mn);

        vars = analyzer.getVariables();
        // Instructions by line number
        lines = getLines();

        DefUseFrame[] frames = analyzer.getDefUseFrames();

        DefUseChain[] chains = new DepthFirstDefUseChainSearch().search(
                analyzer.getDefUseFrames(),
                analyzer.getVariables(),
                flowAnalyzer.getSuccessors(),
                flowAnalyzer.getPredecessors());

        // Only global DU
        globalChains = DefUseChain.globals(chains,
                flowAnalyzer.getLeaders(), flowAnalyzer.getBasicBlocks());

        // DU by basic block (the ones we monitor)
        blockChains = DefUseChain.toBasicBlock(chains,
                flowAnalyzer.getLeaders(), flowAnalyzer.getBasicBlocks());

        Dua d;
        int idDfc = 0;
        dua2DefUseChains.clear();

        for (final DefUseChain c : globalChains) {

            // Check if the DefUseChain variable is null. If so, skip it.

            if (getVar(c, vars) == null)
                continue;

            idDefUseChain.put(c, idDfc);

            Block defblk, cuseblk, targetblk;

            defblk = p.getGraph().get(leaders[c.def]);

//            System.out.println("Name:"+getVar(c,vars)+"; Unique name: "+getVarUnique(c,vars));

            int varId = p.getVariableId(getVarUnique(c, vars), getVar(c, vars));

//            p.addVariable(getVar(c, vars), c.var);

            defblk.def(varId);
            cuseblk = p.getGraph().get(leaders[c.use]);

            if (c.isComputationalChain()) {
                cuseblk.cuse(c.var);
                d = new Dua(defblk, cuseblk, varId, getVar(c, vars));
            } else {
                cuseblk.puse(c.var);
                targetblk = p.getGraph().get(leaders[c.target]);
                d = new Dua(defblk, cuseblk, targetblk, varId, getVar(c, vars));
//                System.out.println(d.toString());
                if (cuseblk.id() == targetblk.id()) {
                    hasAutoEdge = true;
//                    System.out.println("Def: "+defblk.id()+";");
//                    System.out.println("Cuse: "+cuseblk.id()+";");
//                    System.out.println("Target: "+targetblk.id());
//                    System.out.println(c.toString());
//                    System.out.println(d.toString());
                }


            }
            if (!dua2DefUseChains.containsKey(d.hashCode())) {
                List<DefUseChain> l = new LinkedList<>();
                l.add(c);
                List<Integer> lid = new LinkedList<>();
                lid.add(idDfc);
                dua2DefUseChains.put(d.hashCode(), l);
                dua2idDefUseChains.put(d.hashCode(), lid);
                duas.add(d);
            } else {
                dua2DefUseChains.get(d.hashCode()).add(c);
                dua2idDefUseChains.get(d.hashCode()).add(idDfc);
            }
            defUseChain2Dua.put(c, d);
            idDfc++;
        }

        // Find the sets Gen, Born, Kill & Sleepy associated with each block
        p.computeDataFlowSets(duas);
    }

    public String toDuasCSV() {
        final StringBuilder sb = new StringBuilder();

        sb.append("Number of Variables;" + p.numberOfVars() + ";\n");
        sb.append("Variables:;\n");

        for (int id = 0; id < p.numberOfVars(); ++id) {
            sb.append(id + ";;" + p.variable(id) + ";\n");
        }

        sb.append("\nNumber of Block duas;" + duas.size() + ";\n");
        if (mn != null)
            sb.append("Line Duas for method; " + mn.name + ";\n");
        sb.append("Number;Def Line; Origin Line; Use Line; Var Id; Var Name;\n");

        int counter = 0;

        for (final DefUseChain c : globalChains) {
            // TODO: delete duas with var == null

            sb.append(++counter + ";");
            if (c.isComputationalChain()) {
                //System.out.println("("+lines[c.def] + "," + lines[c.use] + "," +getVar(c, vars)+")");
                sb.append(lines[c.def] + ";;" + lines[c.use] + ";" + c.var + ";" + getVar(c, vars) + ";\n");
            } else {
                //p.getGraph().get(leaders[c.use]).puse(c.var);
                //System.out.println("("+lines[c.def]+",(" + lines[c.use]+"," +  lines[c.target] + ")," + getVar(c, vars)+")");
                sb.append(lines[c.def] + ";" + lines[c.use] + ";" + lines[c.target] + ";" + c.var + ";" + getVar(c, vars) + ";\n");
            }
        }

        sb.append("\nBlock Duas for method; " + mn.name + ";\n");
        sb.append("Number;Def Block; Origin Block; Use Block; Var Id; Var Name;\n");

        final Iterator<Dua> it = duas.iterator();
        counter = 0;

        while (it.hasNext()) {
            sb.append(++counter + ";");
            Dua d = it.next();
            if (d.isCUse())
                sb.append(d.def().id() + ";;" + d.use().id() + ";" + d.var() + ";" + d.varName() + ";\n");
            else
                sb.append(d.def().id() + ";" + d.from().id() + ";" + d.to().id() + ";" + d.var() + ";" + d.varName() + ";\n");

            final Iterator<DefUseChain> it2 = dua2DefUseChains.get(d.hashCode()).iterator();
            while (it2.hasNext()) {
                DefUseChain c = it2.next();
                if (c.isComputationalChain()) {
                    sb.append(";" + lines[c.def] + ";;" + lines[c.use] + ";" + c.var + ";" + getVar(c, vars) + ";\n");
                } else {
                    sb.append(";" + lines[c.def] + ";" + lines[c.use] + ";" + lines[c.target] + ";" + c.var + ";" + getVar(c, vars) + ";\n");
                }
            }
        }

        return sb.toString();
    }

    public int[] getLines() {
        final int[] lines = new int[mn.instructions.size()];
        Arrays.fill(lines, -1);
        for (int i = 0; i < mn.instructions.size(); i++) {
            if (mn.instructions.get(i).getType() == AbstractInsnNode.LINE) {
                final LineNumberNode line = (LineNumberNode) mn.instructions.get(i);
                Arrays.fill(lines, mn.instructions.indexOf(line.start), lines.length, line.line);
            }
        }
        return lines;
    }

    public String getVar(final DefUseChain c, final Variable[] vars) {
        return getVar(vars[c.var], c.use);
    }

    public String getVarUnique(final DefUseChain c, final Variable[] vars) {
        return getVarUnique(vars[c.var], c.use);
    }

    public String getVar(final Value v, final int insn) {
        if (v instanceof StaticField) {
            return ((StaticField) v).name;
        } else if (v instanceof ObjectField) {
            final ObjectField objField = (ObjectField) v;
            final String var = getVar(objField.getRoot(), insn);
            if (var != null) {
                return String.format("%s.%s", var, objField.name);
            }
        } else if (v instanceof Local) {
            return getVar((Local) v, insn);
        }
        return null;
    }

    public String getVarUnique(final Value v, final int insn) {
        if (v instanceof StaticField) {
            return ((StaticField) v).name;
        } else if (v instanceof ObjectField) {
            final ObjectField objField = (ObjectField) v;
            final String var = getVar(objField.getRoot(), insn);
            if (var != null) {
                return String.format("%s.%s", var, objField.name);
            }
        } else if (v instanceof Local) {
            return getVarUnique((Local) v, insn);
        }
        return null;
    }

    public String getVar(final Local local, final int insn) {
        for (final LocalVariableNode lvn : mn.localVariables) {
            if (lvn.index == local.var
                    && insn >= mn.instructions.indexOf(lvn.start)
                    && insn < mn.instructions.indexOf(lvn.end)) {
                return lvn.name;
            }
        }
        return null;
    }

    public String getVarUnique(final Local local, final int insn) {
        for (final LocalVariableNode lvn : mn.localVariables) {
            if (lvn.index == local.var
                    && insn >= mn.instructions.indexOf(lvn.start)
                    && insn < mn.instructions.indexOf(lvn.end)) {
                return lvn.name + "-" + lvn.start + "-" + lvn.end;
            }
        }
        return null;
    }

    public List<Dua> getDuas() {
        return this.duas;
    }

    public Program getProgram() {
        return this.p;
    }

    public void setProgram(Program prg) {
        this.p = prg;
    }

    public HashMap<Integer, List<DefUseChain>> getDefChainsMap() {
        return dua2DefUseChains;
    }

    public String getName() {
        if (mn == null)
            return null;
        return this.mn.name;
    }

    public boolean getHasIncomingEdges() {
        return this.hasIncomingEdges;
    }

    public boolean getHasAutoEdge() {
        return this.hasAutoEdge;
    }

    public boolean getHasDanglingNodes() {
        return this.hasDanglingNodes;
    }

    public void setReductionGraph(ReductionGraph rg) {
        this.rg = rg;
    }

    public ReductionGraph getReductionGraph() {
        return this.rg;
    }

    public Map<Edge, Integer> getEdgesId() {
        return edgesIdMap;
    }

    public void setSubsumptionGraph(SubsumptionGraph sg) {
        this.sg = sg;
    }

    public void setGraphDua(Graphdua gd) {
        this.sne = gd;
    }

    public void setSubsumptionAnalyzer(SubsumptionAnalyzer duaSubAnalyzer) {
        this.sa = duaSubAnalyzer;
    }

    public Graphdua getGraphdua() {
        return this.sne;
    }

    public String toJsonSubsumption(StringBuffer sb) {
        Dua subsumer = null;

        sb.append("{ \"Name\" : \"" + getName() + "\" ,\n");

        sb.append("\"Duas\" : \"" + idDefUseChain.size() + "\" ,\n");

        if (getReductionGraph() != null) {
            sb.append("\"Subsumers\" : " + rg.unconstrainedNodes().size() + ",\n");

            Iterator<ReductionNode> it = rg.unconstrainedNodes().iterator();

            int noSubsumers = 0;
            while (it.hasNext()) {
                ReductionNode r = it.next();
                sb.append("\"" + noSubsumers + "\" : [ ");

                Iterator<Dua> itDua = r.getListDuas().iterator();
                boolean first = true;

                while (itDua.hasNext()) {
                    subsumer = itDua.next();
                    Iterator<Integer> itDfc = dua2idDefUseChains.get(subsumer.hashCode()).iterator();
                    while (itDfc.hasNext()) {
                        int dfc = itDfc.next();
                        if (first)
                            first = false;
                        else
                            sb.append(", ");
                        sb.append(dfc);
                    }
                }

                sb.append("],");

                sb.append(" \"S" + noSubsumers + "\" : [");

                //SubsumptionGraph sg = rg.getSubsumptionGraph();
                sa = sg.getSubsumptionAnalyzer();

                int idDua = sg.getDuaId(subsumer);

                BitSet subsumptionVector = sg.getSubsumptionVector()[idDua];
                if (subsumptionVector != null) {
                    if (!subsumptionVector.isEmpty()) {
                        int idSubDua = -1;
                        first = true;

                        while ((idSubDua = subsumptionVector.nextSetBit(idSubDua + 1)) != -1) {
                            Dua subsuming = sa.getDuaFromId(idSubDua);
                            Iterator<Integer> itDfc = dua2idDefUseChains.get(subsuming.hashCode()).iterator();
                            while (itDfc.hasNext()) {
                                int dfc = itDfc.next();
                                if (first) {
                                    first = false;
                                } else
                                    sb.append(", ");
                                sb.append(dfc);
                            }
                        }
                        if (noSubsumers != (rg.unconstrainedNodes().size() - 1))
                            sb.append(" ],");
                        else
                            sb.append(" ]");
                    }
                    sb.append("\n");
                } else
                    System.out.println("Warning: Subsumption vector is null for dua:" + subsumer.toString());
                noSubsumers++;
            }
        } else {
            sb.append("\"Subsumers\" : 0 \n");
        }
        sb.append("}");
        return sb.toString();
    }


    public String toJsonDuas(StringBuffer sb) {
        sb.append("{ \"Name\" : \"" + getName() + "\" ,\n");

        sb.append("\"Duas\" : " + idDefUseChain.size() + ",\n");
        int idDfc = 0;
        for (DefUseChain dfc : globalChains) {

            if (getVar(dfc, vars) == null)
                continue;

            if (idDfc != 0)
                sb.append(",\n");

            sb.append("\"" + idDfc + "\" : ");

            if (dfc.isComputationalChain())
                sb.append(" \"(" + lines[dfc.def] + "," + lines[dfc.use] + ", " + getVar(dfc, vars) + ")\"");
            else
                sb.append(" \"(" + lines[dfc.def] + ",(" + lines[dfc.use] + "," + lines[dfc.target] + "), " + getVar(dfc, vars) + ")\"");

            idDfc++;
        }

        sb.append("}");
        return sb.toString();
    }

    public String toJsonDuas2Nodes(StringBuffer sb) {
        sb.append("{ \"Name\" : \"" + getName() + "\" ,\n");
        sb.append("\"Nodes\" : " + p.getGraph().size() + ",\n");
        sb.append("\"Duas\" : " + idDefUseChain.size() + ",\n");

        int idDfc = 0;
        for (DefUseChain dfc : globalChains) {

            if (getVar(dfc, vars) == null)
                continue;

            if (idDfc != 0)
                sb.append(",\n");

            sb.append("\"" + idDfc + "\" : ");

            Dua d = defUseChain2Dua.get(dfc);

            if (dfc.isComputationalChain()) {
                if (d.def().id() != d.use().id())
                    sb.append(" [ " + d.def().id() + "," + d.use().id() + " ]");
                else
                    sb.append(" [ " + d.def().id() + " ]");
            } else {
                if (d.def().id() == d.from().id() && d.def().id() == d.use().id())
                    sb.append(" [ " + d.def().id() + " ]");
                else if (d.def().id() == d.from().id())
                    sb.append(" [ " + d.def().id() + "," + d.use().id() + " ]");
                else if (d.def().id() == d.use().id())
                    sb.append(" [ " + d.def().id() + "," + d.from().id() + " ]");
                else
                    sb.append(" [ " + d.def().id() + "," + d.from().id() + "," + d.use().id() + " ]");
            }

            idDfc++;
        }

        sb.append("}");
        return sb.toString();
    }

    public String toJsonDuas2Edges(StringBuffer sb) {

        Map<Integer, Integer> mapDuas2Edges = new TreeMap<Integer, Integer>();

        sb.append("{ \"Name\" : \"" + getName() + "\" ,\n");
        sb.append("\"Edges\" : " + this.edgesIdMap.size() + ",\n");
        sb.append("\"Duas\" : " + idDefUseChain.size() + ",\n");

        int idDfc = 0;

        for (DefUseChain dfc : globalChains) {
            Dua d = defUseChain2Dua.get(dfc);
            if (getVar(dfc, vars) == null)
                continue;

            if (!dfc.isComputationalChain()) {
                Edge e = new Edge(new Block(d.from().id()), new Block(d.use().id()));
                mapDuas2Edges.put(idDfc, edgesIdMap.get(e));
            }

            idDfc++;
        }

        boolean first = true;

        for (Map.Entry<Integer, Integer> e : mapDuas2Edges.entrySet()) {
            if (first) {
                first = false;
                sb.append("\"" + e.getKey() + "\" : " + e.getValue());
            } else
                sb.append(", \n \"" + e.getKey() + "\" : " + e.getValue());
        }

        sb.append("}");
        return sb.toString();
    }


    public String toJsonNodeSubsumption(StringBuffer sb) {
        if (sne == null) {
            sb.append("{ \"Name\" : \"" + this.getName() + "\" ,\n");
            sb.append("\"Nodes\" : " + this.getProgram().getGraph().size() + ",\n");
            sb.append("\"CoveredDUAsByNodes\" : -1,\n");
            sb.append("\"Duas\" : " + idDefUseChain.size());
            sb.append("\n}");

            return sb.toString();
        }

        BitSet allSubsumed = new BitSet(sne.entry().getCovered().size());
        allSubsumed.clear();
        Set<Integer> subDuas = new HashSet<>();
        Set<Integer> allsubDuas = new HashSet<>();

        Iterator<Node> i = sne.iterator();

        sb.append("{ \"Name\" : \"" + this.getName() + "\" ,\n");
        sb.append("\"Nodes\" : " + this.getProgram().getGraph().size() + ",\n");


        while (i.hasNext()) {
            Node k = i.next();
            BitSet coveredInNode = k.getCovered();
            sb.append("\"" + k.block().id() + "\" : [ ");

            if (!coveredInNode.isEmpty()) {
                subDuas.clear();
                int idDua = -1;
                while ((idDua = coveredInNode.nextSetBit(idDua + 1)) != -1) {
                    Dua subDua = sa.getDuaFromId(idDua);
                    Iterator<Integer> itDfc = dua2idDefUseChains.get(subDua.hashCode()).iterator();
                    while (itDfc.hasNext()) {
                        subDuas.add(itDfc.next());
                    }
                }
                allSubsumed.or(coveredInNode);
            }

            if (!coveredInNode.isEmpty()) {
                Iterator<Integer> itsub = subDuas.iterator();

                while (itsub.hasNext()) {
                    int dfc = itsub.next();
                    if (itsub.hasNext())
                        sb.append(dfc + ", ");
                    else
                        sb.append(dfc);
                }
            }
            allsubDuas.addAll(subDuas);
            sb.append("],\n");
        }

        sb.append("\"CoveredDUAsByNodes\" : ");
        sb.append(allsubDuas.size() + ",\n");
        sb.append("\"Duas\" : " + idDefUseChain.size());
        sb.append("\n}");

        return sb.toString();
    }

    public String toJsonNodes(StringBuffer sb) {
        Set<Integer> nodeLines = new HashSet<>();
        Set<Integer> idNodes = new TreeSet<>();

        sb.append("{ \"Name\" : \"" + getName() + "\" ,\n");
        sb.append("\"Nodes\" : " + p.getGraph().size() + ",\n");

        Iterator<Block> itblk = p.getGraph().iterator();

        while (itblk.hasNext())
            idNodes.add(itblk.next().id());

        Iterator<Integer> itid = idNodes.iterator();

        while (itid.hasNext()) {
            Block suc = p.getGraph().get(itid.next());

            sb.append("\"" + suc.id() + "\" : [ ");
            nodeLines.clear();

            for (int l : suc.lines()) {
                if (!nodeLines.contains((lines[l]))) {
                    nodeLines.add(lines[l]);
                }
            }

            Iterator<Integer> it = nodeLines.iterator();

            while (it.hasNext()) {
                int line = it.next();
                if (it.hasNext())
                    sb.append(line + ",");
                else
                    sb.append(line);
            }
            if (itid.hasNext())
                sb.append(" ],\n");
            else
                sb.append(" ]\n");
        }

        sb.append("}");
        return sb.toString();
    }

    public String toJsonEdgeSubsumption(StringBuffer sb) {

        if (sne == null) {
            sb.append("{ \"Name\" : \"" + this.getName() + "\" ,\n");
            sb.append("\"Edges\" : " + this.edgesIdMap.size() + ",\n");
            sb.append("\"CoveredDUAsByEdges\" : -1,\n");
            sb.append("\"Duas\" : " + idDefUseChain.size());
            sb.append("\n}");

            return sb.toString();
        }

        BitSet allSubsumed = new BitSet(sne.entry().getCovered().size());
        allSubsumed.clear();
        Set<Integer> subDuas = new HashSet<>();
        Set<Integer> allsubDuas = new HashSet<>();

        sb.append("{ \"Name\" : \"" + this.getName() + "\" ,\n");
        sb.append("\"Edges\" : " + this.edgesIdMap.size() + ",\n");

        Iterator<Node> i = sne.iterator();

        while (i.hasNext()) {
            Node k = i.next();

            Set<Node> neighbors = sne.neighbors(k.id());
            for (Node kn : neighbors) {
                BitSet coveredInEdge = sne.getDuasSubsumedEdge(k, kn);

                Edge e = new Edge(k.block(), kn.block());

                int ide = 0;

                if (this.edgesIdMap.containsKey(e))
                    ide = this.edgesIdMap.get(e);
                else {
                    System.out.println("Warning: edge does not belong to map of edges.");
                    continue;
                }

                sb.append("\"" + ide + "\" : [ ");
                if (!coveredInEdge.isEmpty()) {
                    subDuas.clear();
                    int idDua = -1;
                    while ((idDua = coveredInEdge.nextSetBit(idDua + 1)) != -1) {
                        Dua subDua = sa.getDuaFromId(idDua);
                        Iterator<Integer> itDfc = dua2idDefUseChains.get(subDua.hashCode()).iterator();
                        while (itDfc.hasNext()) {
                            subDuas.add(itDfc.next());
                        }
                    }
                    allSubsumed.or(coveredInEdge);
                }

                if (!coveredInEdge.isEmpty()) {
                    Iterator<Integer> itsub = subDuas.iterator();

                    while (itsub.hasNext()) {
                        int dfc = itsub.next();
                        if (itsub.hasNext())
                            sb.append(dfc + ", ");
                        else
                            sb.append(dfc);
                    }
                }
                allsubDuas.addAll(subDuas);
                sb.append("],\n");
            }
        }

        sb.append("\"CoveredDUAsByEdges\" : ");
        sb.append(allsubDuas.size() + ",\n");
        sb.append("\"Duas\" : " + idDefUseChain.size());
        sb.append("\n}");

        return sb.toString();
    }

    public String toJsonEdges(StringBuffer sb) {
        sb.append("{ \"Name\" : \"" + getName() + "\" ,\n");
        sb.append("\"Edges\" : " + p.getGraph().sizeEdges() + ",\n");

        for (int i = 0; i < p.getGraph().sizeEdges(); ++i) {
            if (idEdgesMap.containsKey(i)) {
                Edge e = idEdgesMap.get(i);

                sb.append("\"" + i + "\" : [ " + e.getOrg().id() + "," + e.getTrg().id());
                if (i + 1 < p.getGraph().sizeEdges())
                    sb.append(" ],\n");
                else
                    sb.append(" ]\n");

            }
        }

        sb.append("}");
        return sb.toString();
    }


    public String graphDefUseToDot() {
        final StringBuilder sb = new StringBuilder();
        final boolean printLines = true;
        sb.append("digraph " + this.name + " {\n");

        Iterator<Block> it = p.getGraph().iterator();

        while (it.hasNext()) {
            Block blk = it.next();

            if (!p.getGraph().get(blk.id()).lines().isEmpty()) {
                Object[] instr = p.getGraph().get(blk.id()).lines().toArray();

                int firstLine = lines[(int) instr[0]];
                int lastLine = lines[(int) instr[instr.length - 1]];

                if (printLines)
                    sb.append(blk.id() + " [label=\"" + blk.id() + "\\n" + firstLine + "-" + lastLine + "\"]");
                else
                    sb.append(blk.id() + " [label=\"" + blk.id() + "\"]");
            } else
                sb.append(blk.id() + " [label=\"" + blk.id() + "\"]");
            sb.append("\n");
        }

        sb.append("{\n" +
                "node [shape=plaintext, fontsize=14];\n");
        it = p.getGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            if (blk.defs().size() == 0 && blk.cuses().size() == 0)
                continue;
            sb.append("setsNode_" + blk.id());
            sb.append(" [label=\"");
            if (blk.defs().size() != 0)
                sb.append(printDefSet(blk));
            if (blk.cuses().size() != 0) {
                if (blk.defs().size() != 0)
                    sb.append("\\n");
                sb.append(printCuseSet(blk));
            }
            sb.append("\"];\n");
        }
        sb.append("}\n");

        it = p.getGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            if (blk.defs().size() == 0 && blk.cuses().size() == 0)
                continue;
            sb.append("{rank = same; ");
            sb.append(blk.id() + " ; " + " setsNode_" + blk.id());
            sb.append("}\n");
        }

        it = p.getGraph().iterator();
        while (it.hasNext()) {
            Block blk = it.next();
            if (!p.getGraph().neighbors(blk.id()).isEmpty()) {
                for (Block suc : p.getGraph().neighbors(blk.id())) {
                    sb.append(blk.id() + " -> ");
                    sb.append(suc.id());
                    if (blk.puses().size() != 0) {
                        sb.append("[label=\"");
                        sb.append(printPuseSet(blk, suc));
                        sb.append("\",fontsize=14]");
                    }

                    sb.append(";");
                }
                sb.append("\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String printDefSet(Block blk) {
        final StringBuilder sb = new StringBuilder();

        if (blk.defs().size() == 0)
            return null;

        sb.append("def(" + blk.id() + ")={");

        boolean first = true;

        for (int id = 0; id < p.numberOfVars(); ++id) {
            if (blk.isDef(id)) {
                if (first) {
                    first = false;
                } else
                    sb.append(",");
                sb.append(p.variable(id));
            }
        }

        sb.append("}");

        return sb.toString();
    }

    private String printCuseSet(Block blk) {
        final StringBuilder sb = new StringBuilder();

        if (blk.cuses().size() == 0)
            return null;

        sb.append("use(" + blk.id() + ")={");

        boolean first = true;

        for (int id = 0; id < p.numberOfVars(); ++id) {
            if (blk.isCUse(id)) {
                if (first) {
                    first = false;
                } else
                    sb.append(",");
                sb.append(p.variable(id));
            }
        }

        sb.append("}");

        return sb.toString();
    }

    private String printPuseSet(Block blk, Block suc) {
        final StringBuilder sb = new StringBuilder();

        if (blk.puses().size() == 0)
            return null;

        sb.append("use(" + blk.id() + "," + suc.id() + ")={");

        boolean first = true;

        for (int id = 0; id < p.numberOfVars(); ++id) {
            if (blk.isPUse(id)) {
                if (first) {
                    first = false;
                } else
                    sb.append(",");
                sb.append(p.variable(id));
            }
        }

        sb.append("}");

        return sb.toString();
    }

}
