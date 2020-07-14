package br.usp.each.saeg.subsumption.graphdua;

import java.util.BitSet;
import java.util.Objects;

import br.usp.each.saeg.opal.Identifiable;
import br.usp.each.saeg.opal.Block;

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

    private final Block block;

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

    @Override
    public String toString() {
        return String.valueOf(this.block.id());
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

    public BitSet getBorn() { return born; }

    public BitSet getSleepy() {
        return sleepy;
    }

    public BitSet getGen() { return gen; }

    public void setGen(int id){
        gen.set(id);
    }

    public void setBorn(int id){
        born.set(id);
    }

    public void setKill(int id){
        kill.set(id);
    }

    public void setSleepy(int id) {
        sleepy.set(id);
    }
}