package org.maltparser.core.symbol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.nullvalue.NullValues;

public interface SymbolTable extends Table {
   int NA = -1;
   int INPUT = 1;
   int OUTPUT = 3;
   String[] categories = new String[]{"", "INPUT", "", "OUTPUT"};
   int STRING = 1;
   int INTEGER = 2;
   int BOOLEAN = 3;
   int REAL = 4;
   String[] types = new String[]{"", "STRING", "INTEGER", "BOOLEAN", "REAL"};

   void save(BufferedWriter var1) throws MaltChainedException;

   void load(BufferedReader var1) throws MaltChainedException;

   int getValueCounter();

   int getNullValueCode(NullValues.NullValueId var1) throws MaltChainedException;

   String getNullValueSymbol(NullValues.NullValueId var1) throws MaltChainedException;

   boolean isNullValue(String var1) throws MaltChainedException;

   boolean isNullValue(int var1) throws MaltChainedException;
}
