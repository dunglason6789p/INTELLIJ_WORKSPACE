package org.maltparser.core.syntaxgraph;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;

public interface SecEdgeStructure {
   Edge addSecondaryEdge(ComparableNode var1, ComparableNode var2) throws MaltChainedException;

   void removeSecondaryEdge(ComparableNode var1, ComparableNode var2) throws MaltChainedException;
}
