package org.maltparser.core.syntaxgraph.node;

import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.edge.Edge;

public interface Node extends ComparableNode, Element {
   void addIncomingEdge(Edge var1) throws MaltChainedException;

   void addOutgoingEdge(Edge var1) throws MaltChainedException;

   void removeIncomingEdge(Edge var1) throws MaltChainedException;

   void removeOutgoingEdge(Edge var1) throws MaltChainedException;

   Iterator<Edge> getIncomingEdgeIterator();

   Iterator<Edge> getOutgoingEdgeIterator();

   void setIndex(int var1) throws MaltChainedException;
}
