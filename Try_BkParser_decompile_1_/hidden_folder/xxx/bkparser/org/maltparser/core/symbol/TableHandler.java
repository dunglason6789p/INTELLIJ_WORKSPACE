package org.maltparser.core.symbol;

import org.maltparser.core.exception.MaltChainedException;

public interface TableHandler {
   Table getSymbolTable(String var1) throws MaltChainedException;

   Table addSymbolTable(String var1) throws MaltChainedException;
}
