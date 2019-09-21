package org.maltparser.core.syntaxgraph;

import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;

public interface Element {
   void addLabel(SymbolTable var1, String var2) throws MaltChainedException;

   void addLabel(SymbolTable var1, int var2) throws MaltChainedException;

   void addLabel(LabelSet var1) throws MaltChainedException;

   boolean hasLabel(SymbolTable var1) throws MaltChainedException;

   String getLabelSymbol(SymbolTable var1) throws MaltChainedException;

   int getLabelCode(SymbolTable var1) throws MaltChainedException;

   boolean isLabeled();

   int nLabels();

   Set<SymbolTable> getLabelTypes();

   LabelSet getLabelSet();

   void removeLabel(SymbolTable var1) throws MaltChainedException;

   void removeLabels() throws MaltChainedException;

   LabeledStructure getBelongsToGraph();

   void setBelongsToGraph(LabeledStructure var1);

   void clear() throws MaltChainedException;
}
