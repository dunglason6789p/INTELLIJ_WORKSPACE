package org.maltparser.core.syntaxgraph.edge;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.node.Node;

public interface Edge extends Element {
   int DEPENDENCY_EDGE = 1;
   int PHRASE_STRUCTURE_EDGE = 2;
   int SECONDARY_EDGE = 3;

   void setEdge(Node var1, Node var2, int var3) throws MaltChainedException;

   Node getSource();

   Node getTarget();

   int getType();
}
