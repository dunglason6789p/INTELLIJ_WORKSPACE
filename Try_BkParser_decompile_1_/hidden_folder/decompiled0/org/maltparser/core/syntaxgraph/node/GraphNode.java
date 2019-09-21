/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.GraphElement;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.Node;

public abstract class GraphNode
extends GraphElement
implements Node {
    protected SortedSet<Edge> incomingEdges = new TreeSet<Edge>();
    protected SortedSet<Edge> outgoingEdges = new TreeSet<Edge>();

    @Override
    public void addIncomingEdge(Edge in) throws MaltChainedException {
        if (in.getTarget() != this) {
            throw new SyntaxGraphException("The incoming edge's 'to' reference is not correct.");
        }
        this.incomingEdges.add(in);
    }

    @Override
    public void addOutgoingEdge(Edge out) throws MaltChainedException {
        if (out.getSource() != this) {
            throw new SyntaxGraphException("The outgoing edge's 'from' reference is not correct");
        }
        this.outgoingEdges.add(out);
    }

    @Override
    public void removeIncomingEdge(Edge in) throws MaltChainedException {
        if (in.getTarget() != this) {
            throw new SyntaxGraphException("The incoming edge's 'to' reference is not correct");
        }
        this.incomingEdges.remove(in);
    }

    @Override
    public void removeOutgoingEdge(Edge out) throws MaltChainedException {
        if (out.getSource() != this) {
            throw new SyntaxGraphException("The outgoing edge's 'from' reference is not correct");
        }
        this.outgoingEdges.remove(out);
    }

    @Override
    public int getLeftmostProperDescendantIndex() throws MaltChainedException {
        ComparableNode node = this.getLeftmostProperDescendant();
        return node != null ? node.getIndex() : -1;
    }

    @Override
    public int getRightmostProperDescendantIndex() throws MaltChainedException {
        ComparableNode node = this.getRightmostProperDescendant();
        return node != null ? node.getIndex() : -1;
    }

    @Override
    public int getLeftmostDescendantIndex() throws MaltChainedException {
        ComparableNode node = this.getLeftmostProperDescendant();
        return node != null ? node.getIndex() : this.getIndex();
    }

    @Override
    public int getRightmostDescendantIndex() throws MaltChainedException {
        ComparableNode node = this.getRightmostProperDescendant();
        return node != null ? node.getIndex() : this.getIndex();
    }

    @Override
    public Iterator<Edge> getIncomingEdgeIterator() {
        return this.incomingEdges.iterator();
    }

    @Override
    public Iterator<Edge> getOutgoingEdgeIterator() {
        return this.outgoingEdges.iterator();
    }

    @Override
    public void clear() throws MaltChainedException {
        super.clear();
        this.incomingEdges.clear();
        this.outgoingEdges.clear();
    }

    @Override
    public int getInDegree() {
        return this.incomingEdges.size();
    }

    @Override
    public int getOutDegree() {
        return this.outgoingEdges.size();
    }

    @Override
    public SortedSet<Edge> getIncomingSecondaryEdges() {
        TreeSet<Edge> inSecEdges = new TreeSet<Edge>();
        for (Edge e : this.incomingEdges) {
            if (e.getType() != 3) continue;
            inSecEdges.add(e);
        }
        return inSecEdges;
    }

    @Override
    public SortedSet<Edge> getOutgoingSecondaryEdges() {
        TreeSet<Edge> outSecEdges = new TreeSet<Edge>();
        for (Edge e : this.outgoingEdges) {
            if (e.getType() != 3) continue;
            outSecEdges.add(e);
        }
        return outSecEdges;
    }

    @Override
    public int compareTo(ComparableNode o) {
        return super.compareTo((GraphElement)((Object)o));
    }

    @Override
    public abstract int getIndex();

    @Override
    public abstract void setIndex(int var1) throws MaltChainedException;

    @Override
    public abstract boolean isRoot();

    @Override
    public boolean equals(Object obj) {
        GraphNode v = (GraphNode)obj;
        return super.equals(obj) && this.incomingEdges.equals(v.incomingEdges) && this.outgoingEdges.equals(v.outgoingEdges);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + super.hashCode();
        hash = 31 * hash + (null == this.incomingEdges ? 0 : this.incomingEdges.hashCode());
        hash = 31 * hash + (null == this.outgoingEdges ? 0 : this.outgoingEdges.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getIndex());
        sb.append(" [I:");
        for (Edge e : this.incomingEdges) {
            sb.append(e.getSource().getIndex());
            sb.append("(");
            sb.append(e.toString());
            sb.append(")");
            if (this.incomingEdges.last() == e) continue;
            sb.append(",");
        }
        sb.append("][O:");
        for (Edge e : this.outgoingEdges) {
            sb.append(e.getTarget().getIndex());
            if (this.outgoingEdges.last() == e) continue;
            sb.append(",");
        }
        sb.append("]");
        sb.append(super.toString());
        return sb.toString();
    }
}

