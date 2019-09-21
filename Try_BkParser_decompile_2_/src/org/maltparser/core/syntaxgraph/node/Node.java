/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;

public interface Node
extends ComparableNode,
Element {
    public void addIncomingEdge(Edge var1) throws MaltChainedException;

    public void addOutgoingEdge(Edge var1) throws MaltChainedException;

    public void removeIncomingEdge(Edge var1) throws MaltChainedException;

    public void removeOutgoingEdge(Edge var1) throws MaltChainedException;

    public Iterator<Edge> getIncomingEdgeIterator();

    public Iterator<Edge> getOutgoingEdgeIterator();

    public void setIndex(int var1) throws MaltChainedException;
}

