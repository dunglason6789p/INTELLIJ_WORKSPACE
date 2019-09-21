package org.maltparser.core.syntaxgraph;

import java.util.ArrayList;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public interface TokenStructure extends LabeledStructure {
   TokenNode addTokenNode() throws MaltChainedException;

   TokenNode addTokenNode(int var1) throws MaltChainedException;

   void addComment(String var1, int var2);

   ArrayList<String> getComment(int var1);

   boolean hasComments();

   TokenNode getTokenNode(int var1);

   int nTokenNode();

   SortedSet<Integer> getTokenIndices();

   int getHighestTokenIndex();

   boolean hasTokens();

   int getSentenceID();

   void setSentenceID(int var1);
}
