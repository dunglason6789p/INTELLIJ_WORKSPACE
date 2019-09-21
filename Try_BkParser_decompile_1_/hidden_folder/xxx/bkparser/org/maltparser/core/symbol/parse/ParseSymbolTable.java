package org.maltparser.core.symbol.parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Map;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;

public class ParseSymbolTable implements SymbolTable {
   private final String name;
   private final SymbolTable parentSymbolTable;
   private final int type;
   private final Map<String, Integer> symbolCodeMap;
   private final Map<Integer, String> codeSymbolMap;
   private final Map<String, Double> symbolValueMap;
   private int valueCounter;

   public ParseSymbolTable(String _name, int _category, int _type, String nullValueStrategy, SymbolTableHandler parentSymbolTableHandler) throws MaltChainedException {
      this.name = _name;
      this.type = _type;
      this.parentSymbolTable = parentSymbolTableHandler.addSymbolTable(this.name, _category, _type, nullValueStrategy);
      this.symbolCodeMap = new HashMap();
      this.codeSymbolMap = new HashMap();
      this.symbolValueMap = new HashMap();
      this.valueCounter = -1;
   }

   public ParseSymbolTable(String _name, SymbolTable parentTable, SymbolTableHandler parentSymbolTableHandler) throws MaltChainedException {
      this.name = _name;
      this.type = 1;
      this.parentSymbolTable = parentSymbolTableHandler.addSymbolTable(this.name, parentTable);
      this.symbolCodeMap = new HashMap();
      this.codeSymbolMap = new HashMap();
      this.symbolValueMap = new HashMap();
      this.valueCounter = -1;
   }

   public ParseSymbolTable(String name, SymbolTableHandler parentSymbolTableHandler) throws MaltChainedException {
      this.name = name;
      this.type = 1;
      this.parentSymbolTable = parentSymbolTableHandler.addSymbolTable(name);
      this.symbolCodeMap = new HashMap();
      this.codeSymbolMap = new HashMap();
      this.symbolValueMap = new HashMap();
      this.valueCounter = -1;
   }

   public int addSymbol(String symbol) throws MaltChainedException {
      if (!this.parentSymbolTable.isNullValue(symbol)) {
         if (symbol != null && symbol.length() != 0) {
            int code = this.parentSymbolTable.getSymbolStringToCode(symbol);
            if (code > -1) {
               return code;
            } else {
               if (this.type == 4) {
                  this.addSymbolValue(symbol);
               }

               if (!this.symbolCodeMap.containsKey(symbol)) {
                  if (this.valueCounter == -1) {
                     this.valueCounter = this.parentSymbolTable.getValueCounter() + 1;
                  } else {
                     ++this.valueCounter;
                  }

                  this.symbolCodeMap.put(symbol, this.valueCounter);
                  this.codeSymbolMap.put(this.valueCounter, symbol);
                  return this.valueCounter;
               } else {
                  return (Integer)this.symbolCodeMap.get(symbol);
               }
            }
         } else {
            throw new SymbolException("Symbol table error: empty string cannot be added to the symbol table");
         }
      } else {
         return this.parentSymbolTable.getSymbolStringToCode(symbol);
      }
   }

   public double addSymbolValue(String symbol) throws MaltChainedException {
      if (!this.symbolValueMap.containsKey(symbol)) {
         Double value = Double.valueOf(symbol);
         this.symbolValueMap.put(symbol, value);
         return value;
      } else {
         return (Double)this.symbolValueMap.get(symbol);
      }
   }

   public String getSymbolCodeToString(int code) throws MaltChainedException {
      if (code < 0) {
         throw new SymbolException("The symbol code '" + code + "' cannot be found in the symbol table. ");
      } else {
         String symbol = this.parentSymbolTable.getSymbolCodeToString(code);
         return symbol != null ? symbol : (String)this.codeSymbolMap.get(code);
      }
   }

   public int getSymbolStringToCode(String symbol) throws MaltChainedException {
      if (symbol == null) {
         throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
      } else {
         int code = this.parentSymbolTable.getSymbolStringToCode(symbol);
         if (code > -1) {
            return code;
         } else {
            Integer item = (Integer)this.symbolCodeMap.get(symbol);
            if (item == null) {
               throw new SymbolException("Could not find the symbol '" + symbol + "' in the symbol table. ");
            } else {
               return item;
            }
         }
      }
   }

   public double getSymbolStringToValue(String symbol) throws MaltChainedException {
      if (symbol == null) {
         throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
      } else {
         double value = this.parentSymbolTable.getSymbolStringToValue(symbol);
         if (value != 0.0D / 0.0) {
            return value;
         } else {
            Double item = (Double)this.symbolValueMap.get(symbol);
            if (item == null) {
               throw new SymbolException("Could not find the symbol '" + symbol + "' in the symbol table. ");
            } else {
               return item;
            }
         }
      }
   }

   public void clearTmpStorage() {
      this.symbolCodeMap.clear();
      this.codeSymbolMap.clear();
      this.symbolValueMap.clear();
      this.valueCounter = -1;
   }

   public int size() {
      return this.parentSymbolTable.size();
   }

   public void save(BufferedWriter out) throws MaltChainedException {
      this.parentSymbolTable.save(out);
   }

   public void load(BufferedReader in) throws MaltChainedException {
      this.parentSymbolTable.load(in);
   }

   public String getName() {
      return this.name;
   }

   public int getValueCounter() {
      return this.parentSymbolTable.getValueCounter();
   }

   public int getNullValueCode(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
      return this.parentSymbolTable.getNullValueCode(nullValueIdentifier);
   }

   public String getNullValueSymbol(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
      return this.parentSymbolTable.getNullValueSymbol(nullValueIdentifier);
   }

   public boolean isNullValue(String symbol) throws MaltChainedException {
      return this.parentSymbolTable.isNullValue(symbol);
   }

   public boolean isNullValue(int code) throws MaltChainedException {
      return this.parentSymbolTable.isNullValue(code);
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         ParseSymbolTable other = (ParseSymbolTable)obj;
         return this.name == null ? other.name == null : this.name.equals(other.name);
      }
   }

   public int hashCode() {
      return 217 + (null == this.name ? 0 : this.name.hashCode());
   }

   public String toString() {
      return this.parentSymbolTable.toString();
   }
}
