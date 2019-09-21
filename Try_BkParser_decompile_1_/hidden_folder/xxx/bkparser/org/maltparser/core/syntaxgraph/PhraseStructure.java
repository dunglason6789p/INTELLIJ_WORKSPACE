package org.maltparser.core.syntaxgraph;

import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

public interface PhraseStructure extends TokenStructure, SecEdgeStructure {
   PhraseStructureNode addTerminalNode() throws MaltChainedException;

   PhraseStructureNode addTerminalNode(int var1) throws MaltChainedException;

   PhraseStructureNode getTerminalNode(int var1);

   int nTerminalNode();

   Edge addPhraseStructureEdge(PhraseStructureNode var1, PhraseStructureNode var2) throws MaltChainedException;

   void removePhraseStructureEdge(PhraseStructureNode var1, PhraseStructureNode var2) throws MaltChainedException;

   int nEdges();

   PhraseStructureNode getPhraseStructureRoot();

   PhraseStructureNode getNonTerminalNode(int var1) throws MaltChainedException;

   PhraseStructureNode addNonTerminalNode() throws MaltChainedException;

   PhraseStructureNode addNonTerminalNode(int var1) throws MaltChainedException;

   int getHighestNonTerminalIndex();

   Set<Integer> getNonTerminalIndices();

   boolean hasNonTerminals();

   int nNonTerminals();

   boolean isContinuous();

   boolean isContinuousExcludeTerminalsAttachToRoot();
}
