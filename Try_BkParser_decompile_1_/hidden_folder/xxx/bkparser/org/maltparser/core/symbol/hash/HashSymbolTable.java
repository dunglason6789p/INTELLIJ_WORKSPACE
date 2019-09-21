package org.maltparser.core.symbol.hash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.nullvalue.InputNullValues;
import org.maltparser.core.symbol.nullvalue.NullValues;
import org.maltparser.core.symbol.nullvalue.OutputNullValues;

public final class HashSymbolTable implements SymbolTable {
   private final String name;
   private final Map<String, Integer> symbolCodeMap;
   private final Map<Integer, String> codeSymbolMap;
   private final Map<String, Double> symbolValueMap;
   private final NullValues nullValues;
   private final int category;
   private final int type;
   private int valueCounter;

   public HashSymbolTable(String _name, int _category, int _type, String nullValueStrategy) throws MaltChainedException {
      this.name = _name;
      this.category = _category;
      this.type = _type;
      this.symbolCodeMap = new HashMap();
      this.codeSymbolMap = new HashMap();
      this.symbolValueMap = new HashMap();
      if (this.category == 3) {
         this.nullValues = new OutputNullValues(nullValueStrategy, this);
      } else {
         this.nullValues = new InputNullValues(nullValueStrategy, this);
      }

      this.valueCounter = this.nullValues.getNextCode();
   }

   public HashSymbolTable(String _name) {
      this.name = _name;
      this.category = -1;
      this.type = 1;
      this.symbolCodeMap = new HashMap();
      this.codeSymbolMap = new HashMap();
      this.symbolValueMap = new HashMap();
      this.nullValues = new InputNullValues("one", this);
      this.valueCounter = 1;
   }

   public int addSymbol(String symbol) throws MaltChainedException {
      if (this.nullValues != null && this.nullValues.isNullValue(symbol)) {
         return this.nullValues.symbolToCode(symbol);
      } else if (symbol != null && symbol.length() != 0) {
         if (this.type == 4) {
            this.addSymbolValue(symbol);
         }

         if (!this.symbolCodeMap.containsKey(symbol)) {
            int code = this.valueCounter;
            this.symbolCodeMap.put(symbol, code);
            this.codeSymbolMap.put(code, symbol);
            ++this.valueCounter;
            return code;
         } else {
            return (Integer)this.symbolCodeMap.get(symbol);
         }
      } else {
         throw new SymbolException("Symbol table error: empty string cannot be added to the symbol table");
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
      if (code >= 0) {
         return this.nullValues != null && this.nullValues.isNullValue(code) ? this.nullValues.codeToSymbol(code) : (String)this.codeSymbolMap.get(code);
      } else {
         throw new SymbolException("The symbol code '" + code + "' cannot be found in the symbol table. ");
      }
   }

   public int getSymbolStringToCode(String symbol) throws MaltChainedException {
      if (symbol != null) {
         if (this.nullValues != null && this.nullValues.isNullValue(symbol)) {
            return this.nullValues.symbolToCode(symbol);
         } else {
            Integer value = (Integer)this.symbolCodeMap.get(symbol);
            return value != null ? value : -1;
         }
      } else {
         throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
      }
   }

   public double getSymbolStringToValue(String symbol) throws MaltChainedException {
      if (symbol == null) {
         throw new SymbolException("The symbol code '" + symbol + "' cannot be found in the symbol table. ");
      } else if ((this.type != 4 || this.nullValues != null) && this.nullValues.isNullValue(symbol)) {
         return 1.0D;
      } else {
         Double value = (Double)this.symbolValueMap.get(symbol);
         return value != null ? value : Double.parseDouble(symbol);
      }
   }

   public void saveHeader(BufferedWriter out) throws MaltChainedException {
      try {
         out.append('\t');
         out.append(this.getName());
         out.append('\t');
         out.append(Integer.toString(this.getCategory()));
         out.append('\t');
         out.append(Integer.toString(this.getType()));
         out.append('\t');
         out.append(this.getNullValueStrategy());
         out.append('\n');
      } catch (IOException var3) {
         throw new SymbolException("Could not save the symbol table. ", var3);
      }
   }

   public int getCategory() {
      return this.category;
   }

   public int getType() {
      return this.type;
   }

   public String getNullValueStrategy() {
      return this.nullValues == null ? null : this.nullValues.getNullValueStrategy();
   }

   public int size() {
      return this.symbolCodeMap.size();
   }

   public void save(BufferedWriter out) throws MaltChainedException {
      try {
         out.write(this.name);
         out.write(10);
         Iterator i$;
         if (this.type != 4) {
            i$ = this.codeSymbolMap.keySet().iterator();

            while(i$.hasNext()) {
               Integer code = (Integer)i$.next();
               out.write(Integer.toString(code));
               out.write(9);
               out.write((String)this.codeSymbolMap.get(code));
               out.write(10);
            }
         } else {
            i$ = this.symbolValueMap.keySet().iterator();

            while(i$.hasNext()) {
               String symbol = (String)i$.next();
               out.write(1);
               out.write(9);
               out.write(symbol);
               out.write(10);
            }
         }

         out.write(10);
      } catch (IOException var4) {
         throw new SymbolException("Could not save the symbol table. ", var4);
      }
   }

   public void load(BufferedReader in) throws MaltChainedException {
      int max = 0;

      try {
         String fileLine;
         while((fileLine = in.readLine()) != null) {
            int index;
            if (fileLine.length() == 0 || (index = fileLine.indexOf(9)) == -1) {
               this.valueCounter = max + 1;
               break;
            }

            if (this.type != 4) {
               int code;
               try {
                  code = Integer.parseInt(fileLine.substring(0, index));
               } catch (NumberFormatException var7) {
                  throw new SymbolException("The symbol table file (.sym) contains a non-integer value in the first column. ", var7);
               }

               String symbol = fileLine.substring(index + 1);
               this.symbolCodeMap.put(symbol, code);
               this.codeSymbolMap.put(code, symbol);
               if (max < code) {
                  max = code;
               }
            } else {
               String symbol = fileLine.substring(index + 1);
               this.symbolValueMap.put(symbol, Double.parseDouble(symbol));
               max = 1;
            }
         }

      } catch (IOException var8) {
         throw new SymbolException("Could not load the symbol table. ", var8);
      }
   }

   public String getName() {
      return this.name;
   }

   public int getValueCounter() {
      return this.valueCounter;
   }

   public int getNullValueCode(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
      if (this.nullValues == null) {
         throw new SymbolException("The symbol table does not have any null-values. ");
      } else {
         return this.nullValues.nullvalueToCode(nullValueIdentifier);
      }
   }

   public String getNullValueSymbol(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
      if (this.nullValues == null) {
         throw new SymbolException("The symbol table does not have any null-values. ");
      } else {
         return this.nullValues.nullvalueToSymbol(nullValueIdentifier);
      }
   }

   public boolean isNullValue(String symbol) throws MaltChainedException {
      return this.nullValues != null ? this.nullValues.isNullValue(symbol) : false;
   }

   public boolean isNullValue(int code) throws MaltChainedException {
      return this.nullValues != null ? this.nullValues.isNullValue(code) : false;
   }

   public Set<Integer> getCodes() {
      return this.codeSymbolMap.keySet();
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         HashSymbolTable other = (HashSymbolTable)obj;
         return this.name == null ? other.name == null : this.name.equals(other.name);
      }
   }

   public int hashCode() {
      return 217 + (null == this.name ? 0 : this.name.hashCode());
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.name);
      sb.append(' ');
      sb.append(this.valueCounter);
      return sb.toString();
   }
}
