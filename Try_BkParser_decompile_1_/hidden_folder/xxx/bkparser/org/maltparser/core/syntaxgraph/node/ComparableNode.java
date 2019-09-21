package org.maltparser.core.syntaxgraph.node;

import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.edge.Edge;

public interface ComparableNode extends Element, Comparable<ComparableNode> {
   int getIndex();

   int getCompareToIndex();

   boolean isRoot();

   ComparableNode getLeftmostProperDescendant() throws MaltChainedException;

   ComparableNode getRightmostProperDescendant() throws MaltChainedException;

   int getLeftmostProperDescendantIndex() throws MaltChainedException;

   int getRightmostProperDescendantIndex() throws MaltChainedException;

   ComparableNode getLeftmostDescendant() throws MaltChainedException;

   ComparableNode getRightmostDescendant() throws MaltChainedException;

   int getLeftmostDescendantIndex() throws MaltChainedException;

   int getRightmostDescendantIndex() throws MaltChainedException;

   int getInDegree();

   int getOutDegree();

   SortedSet<Edge> getIncomingSecondaryEdges() throws MaltChainedException;

   SortedSet<Edge> getOutgoingSecondaryEdges() throws MaltChainedException;
}
