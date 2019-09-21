package org.maltparser.core.feature.function;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.symbol.SymbolTable;

public interface FeatureFunction extends Function {
   String getSymbol(int var1) throws MaltChainedException;

   int getCode(String var1) throws MaltChainedException;

   SymbolTable getSymbolTable();

   FeatureValue getFeatureValue();

   int getType();

   String getMapIdentifier();
}
