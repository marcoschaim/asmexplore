package br.usp.each.saeg.subsumption.graphdua;

import java.util.Objects;

import br.usp.each.saeg.opal.Identifiable;

public class Dua<K extends Identifiable> {

    private final K def;
    private final K use;
    private final K from;
    private final K to;
    private final int var;
    private final String varName;
    private final boolean cuse;

    public Dua(K def, K use, int var, String varName) {
        this.def = def;
        this.use = use;
        this.from = null;
        this.to = null;
        this.var = var;
        this.varName = varName;
        this.cuse = true;
    }

    public Dua(K def, K from, K to, int var, String varName) {
        this.def = def;
        this.use = to;
        this.from = from;
        this.to = to;
        this.var = var;
        this.varName = varName;
        this.cuse = false;
    }

    public K def() {
        return this.def;
    }

    public K use() {
        return this.use;
    }

    public K from() {
        return this.from;
    }

    public K to() {
        return this.to;
    }

    public int var() {
        return this.var;
    }

    public String varName() {
        return this.varName;
    }

    public boolean isCUse() {
        return this.cuse;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Dua<?>)) return false;

        Dua<K> dua = (Dua<K>) obj;
        return dua.var == this.var &&
                dua.varName == this.varName &&
                Objects.equals(dua.def, this.def) &&
                Objects.equals(dua.use, this.use) &&
                Objects.equals(dua.from, this.from) &&
                Objects.equals(dua.to, this.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.def, this.use, this.from, this.to ,this.var, this.varName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.def.id());
        sb.append(",");

        if (this.isCUse()) {
            sb.append(this.use.id());
        }
        else {
            sb.append("(");
            sb.append(this.from.id());
            sb.append(",");
            sb.append(this.to.id());
            sb.append(")");
        }
        
        sb.append(", ");
        sb.append(this.varName);
        sb.append(")");

        return sb.toString();
    }

}