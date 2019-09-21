package org.maltparser.core.syntaxgraph;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;

public interface LabeledStructure {
   SymbolTableHandler getSymbolTables();

   void setSymbolTables(SymbolTableHandler var1);

   void addLabel(Element var1, String var2, String var3) throws MaltChainedException;

   LabelSet checkOutNewLabelSet() throws MaltChainedException;

   void checkInLabelSet(LabelSet var1) throws MaltChainedException;

   void clear() throws MaltChainedException;
}
