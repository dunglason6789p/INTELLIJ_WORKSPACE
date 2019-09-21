package org.maltparser.core.syntaxgraph.node;

import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.headrules.HeadRules;

public interface NonTerminalNode extends PhraseStructureNode {
   TokenNode identifyHead(HeadRules var1) throws MaltChainedException;

   TokenNode getLexicalHead(HeadRules var1) throws MaltChainedException;

   TokenNode getLexicalHead() throws MaltChainedException;

   PhraseStructureNode getHeadChild(HeadRules var1) throws MaltChainedException;

   PhraseStructureNode getHeadChild() throws MaltChainedException;

   SortedSet<PhraseStructureNode> getChildren();

   PhraseStructureNode getChild(int var1);

   PhraseStructureNode getLeftChild();

   PhraseStructureNode getRightChild();

   int nChildren();

   boolean hasNonTerminalChildren();

   boolean hasTerminalChildren();

   int getHeight();

   boolean isContinuous();

   boolean isContinuousExcludeTerminalsAttachToRoot();
}
