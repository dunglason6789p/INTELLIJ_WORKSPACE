/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.edge;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.Weightable;
import org.maltparser.core.syntaxgraph.edge.GraphEdge;
import org.maltparser.core.syntaxgraph.node.Node;

public class WeightedEdge
extends GraphEdge
implements Weightable {
    private Double weight = Double.NaN;

    public WeightedEdge() {
    }

    public WeightedEdge(Node source, Node target, int type) throws MaltChainedException {
        super(source, target, type);
    }

    public WeightedEdge(Node source, Node target, int type, Double weight) throws MaltChainedException {
        super(source, target, type);
        this.setWeight(weight);
    }

    @Override
    public void clear() throws MaltChainedException {
        super.clear();
        this.weight = Double.NaN;
    }

    @Override
    public double getWeight() {
        return this.weight;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(WeightedEdge that) {
        if (this == that) {
            return 0;
        }
        int comparison = this.weight.compareTo(that.getWeight());
        if (comparison != 0) {
            return comparison;
        }
        return super.compareTo(that);
    }

    @Override
    public boolean equals(Object obj) {
        WeightedEdge e = (WeightedEdge)obj;
        return this.weight.equals(e.getWeight()) && super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == this.weight ? 0 : this.weight.hashCode());
        return 31 * hash + super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getWeight());
        sb.append(' ');
        sb.append(this.getSource().getIndex());
        sb.append("->");
        sb.append(this.getTarget().getIndex());
        sb.append(' ');
        sb.append(super.toString());
        return sb.toString();
    }
}

