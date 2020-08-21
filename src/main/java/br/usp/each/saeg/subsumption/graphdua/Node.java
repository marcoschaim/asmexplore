package br.usp.each.saeg.subsumption.graphdua;

import br.usp.each.saeg.opal.Block;
import br.usp.each.saeg.opal.Identifiable;

import java.util.BitSet;
import java.util.Objects;

public class Node implements Identifiable {

    private final int id;
    private final int idSubgraph;
    private BitSet in;
    private BitSet out;
    private BitSet gen;
    private BitSet kill;
    private BitSet born;
    private BitSet liveduas;
    private BitSet sleepy;
    private BitSet covered;
    private BitSet dom;
    private BitSet postDom;

    private final Block block;
    boolean outpred = false;
    boolean outsuc = false;

    public Node(Block block, int idSubgraph) {
        this.block = block.clone();
        this.idSubgraph = idSubgraph;
        this.id = hash(block.id(), idSubgraph);
    }

    @Override
    public int id() {
        return this.id;
    }

    public int idSubgraph() {
        return this.idSubgraph;
    }

    public Block block() {
        return this.block;
    }

    public void initNodeSets(int size){
        in = new BitSet(size);
        out = new BitSet(size);
        gen = new BitSet(size);
        kill = new BitSet(size);
        born = new BitSet(size);
        sleepy = new BitSet(size);
        liveduas = new BitSet(size);
        covered = new BitSet(size);
    }

    public void initDominanceNodes(int size) {
        dom = new BitSet(size);
        postDom = new BitSet(size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Node)) return false;

        Node node = (Node) obj;
        return node.id == this.id &&
                node.idSubgraph == this.idSubgraph &&
                Objects.equals(node.block, this.block);
    }

    @Override
    public int hashCode() {
        return hash(this.block.id(), this.idSubgraph);
    }

    @Override
    public Node clone() {
        final Node node = new Node(block, idSubgraph);
        return node;
    }

    public static int hash(int id, int idsg) {
        return 1031 * id + idsg;
    }

    public BitSet getLiveDuas() {
        return liveduas;
    }

    public BitSet getCovered() { return covered; }

    public BitSet getIn() {
        return in;
    }

    public BitSet getOut() {
        return out;
    }

    public BitSet getKill() {
        return kill;
    }

    public BitSet getBorn() {
        return born;
    }

    public BitSet getSleepy() {
        return sleepy;
    }

    public BitSet getGen() {
        return gen;
    }

    public boolean getOutPred() {
        return outpred;
    }

    public void setOutPred(boolean val) {
        outpred = val;
    }

    public void setGen(int id) {
        gen.set(id);
    }

    public void setBorn(int id) {
        born.set(id);
    }

    public void setKill(int id) {
        kill.set(id);
    }

    public void setSleepy(int id) {
        sleepy.set(id);
    }

    public boolean getOutSuc() {
        return outsuc;
    }

    public void setOutSuc(boolean val) {
        outsuc = val;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[" + this.id() + ":" + this.block.id() + "(" + this.idSubgraph() + ")");
        sb.append(":" + this.getOutSuc());
        sb.append(":" + this.getOutPred() + "]");
        return sb.toString();
    }
}