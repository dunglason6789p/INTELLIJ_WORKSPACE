package org.maltparser.core.symbol;

import org.maltparser.core.exception.MaltChainedException;

public interface Table {
   int addSymbol(String var1) throws MaltChainedException;

   String getSymbolCodeToString(int var1) throws MaltChainedException;

   int getSymbolStringToCode(String var1) throws MaltChainedException;

   double getSymbolStringToValue(String var1) throws MaltChainedException;

   String getName();

   int size();
}
