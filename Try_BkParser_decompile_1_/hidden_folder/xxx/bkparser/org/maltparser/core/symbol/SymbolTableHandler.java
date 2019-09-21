package org.maltparser.core.symbol;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;

public interface SymbolTableHandler extends TableHandler {
   SymbolTable addSymbolTable(String var1) throws MaltChainedException;

   SymbolTable addSymbolTable(String var1, SymbolTable var2) throws MaltChainedException;

   SymbolTable addSymbolTable(String var1, int var2, int var3, String var4) throws MaltChainedException;

   SymbolTable getSymbolTable(String var1) throws MaltChainedException;

   Set<String> getSymbolTableNames();

   void cleanUp();

   void save(OutputStreamWriter var1) throws MaltChainedException;

   void save(String var1, String var2) throws MaltChainedException;

   void load(InputStreamReader var1) throws MaltChainedException;

   void load(String var1, String var2) throws MaltChainedException;

   SymbolTable loadTagset(String var1, String var2, String var3, int var4, int var5, String var6) throws MaltChainedException;
}
