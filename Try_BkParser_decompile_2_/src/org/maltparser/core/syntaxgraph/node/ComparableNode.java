/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.edge.Edge;

public interface ComparableNode
extends Element,
Comparable<ComparableNode> {
    public int getIndex();

    public int getCompareToIndex();

    public boolean isRoot();

    public ComparableNode getLeftmostProperDescendant() throws MaltChainedException;

    public ComparableNode getRightmostProperDescendant() throws MaltChainedException;

    public int getLeftmostProperDescendantIndex() throws MaltChainedException;

    public int getRightmostProperDescendantIndex() throws MaltChainedException;

    public ComparableNode getLeftmostDescendant() throws MaltChainedException;

    public ComparableNode getRightmostDescendant() throws MaltChainedException;

    public int getLeftmostDescendantIndex() throws MaltChainedException;

    public int getRightmostDescendantIndex() throws MaltChainedException;

    public int getInDegree();

    public int getOutDegree();

    public SortedSet<Edge> getIncomingSecondaryEdges() throws MaltChainedException;

    public SortedSet<Edge> getOutgoingSecondaryEdges() throws MaltChainedException;
}

