/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.edge;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.GraphElement;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.Node;

public class GraphEdge
extends GraphElement
implements Edge,
Comparable<GraphEdge> {
    private Node source = null;
    private Node target = null;
    private int type;

    public GraphEdge() {
    }

    public GraphEdge(Node source, Node target, int type) throws MaltChainedException {
        this.clear();
        this.setEdge(source, target, type);
    }

    @Override
    public void setEdge(Node source, Node target, int type) throws MaltChainedException {
        this.source = source;
        this.target = target;
        if (type >= 1 && type <= 3) {
            this.type = type;
        }
        this.source.addOutgoingEdge(this);
        this.target.addIncomingEdge(this);
        this.setChanged();
        this.notifyObservers(this);
    }

    @Override
    public void clear() throws MaltChainedException {
        super.clear();
        if (this.source != null) {
            this.source.removeOutgoingEdge(this);
        }
        if (this.target != null) {
            this.target.removeIncomingEdge(this);
        }
        this.source = null;
        this.target = null;
        this.type = -1;
    }

    @Override
    public Node getSource() {
        return this.source;
    }

    @Override
    public Node getTarget() {
        return this.target;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public int compareTo(GraphEdge that) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == that) {
            return 0;
        }
        if (this.target.getCompareToIndex() < that.target.getCompareToIndex()) {
            return -1;
        }
        if (this.target.getCompareToIndex() > that.target.getCompareToIndex()) {
            return 1;
        }
        if (this.source.getCompareToIndex() < that.source.getCompareToIndex()) {
            return -1;
        }
        if (this.source.getCompareToIndex() > that.source.getCompareToIndex()) {
            return 1;
        }
        if (this.type < that.type) {
            return -1;
        }
        if (this.type > that.type) {
            return 1;
        }
        return super.compareTo(that);
    }

    @Override
    public boolean equals(Object obj) {
        GraphEdge e = (GraphEdge)obj;
        return this.type == e.getType() && this.source.equals(e.getSource()) && this.target.equals(e.getTarget()) && super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.type;
        hash = 31 * hash + (null == this.source ? 0 : this.source.hashCode());
        hash = 31 * hash + (null == this.target ? 0 : this.target.hashCode());
        return 31 * hash + super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.source.getIndex());
        sb.append("->");
        sb.append(this.target.getIndex());
        sb.append(' ');
        sb.append(super.toString());
        return sb.toString();
    }
}

