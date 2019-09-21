package org.maltparser.core.syntaxgraph.node;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.edge.Edge;

public interface PhraseStructureNode extends ComparableNode {
   PhraseStructureNode getParent();

   Edge getParentEdge() throws MaltChainedException;

   String getParentEdgeLabelSymbol(SymbolTable var1) throws MaltChainedException;

   int getParentEdgeLabelCode(SymbolTable var1) throws MaltChainedException;

   boolean hasParentEdgeLabel(SymbolTable var1) throws MaltChainedException;
}
