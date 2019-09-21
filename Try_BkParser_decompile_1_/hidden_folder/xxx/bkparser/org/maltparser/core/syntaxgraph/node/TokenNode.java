package org.maltparser.core.syntaxgraph.node;

public interface TokenNode extends DependencyNode, PhraseStructureNode {
   void setPredecessor(TokenNode var1);

   void setSuccessor(TokenNode var1);

   TokenNode getTokenNodePredecessor();

   TokenNode getTokenNodeSuccessor();
}
