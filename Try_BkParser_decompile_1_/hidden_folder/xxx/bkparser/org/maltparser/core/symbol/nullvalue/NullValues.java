package org.maltparser.core.symbol.nullvalue;

import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;

public abstract class NullValues {
   protected HashMap<NullValues.NullValueId, String> nullValue2SymbolMap;
   protected HashMap<NullValues.NullValueId, Integer> nullValue2CodeMap;
   protected HashMap<String, Integer> symbol2CodeMap;
   protected SortedMap<Integer, String> code2SymbolMap;
   protected SymbolTable table;
   protected NullValues.NullValueDegree nullValueEncoding;
   protected String nullValueStrategy;
   protected int nextCode;

   public NullValues(SymbolTable table) {
      this.setSymbolTable(table);
      this.nullValue2SymbolMap = new HashMap();
      this.nullValue2CodeMap = new HashMap();
      this.symbol2CodeMap = new HashMap();
      this.code2SymbolMap = new TreeMap();
   }

   private void setSymbolTable(SymbolTable table) {
      this.table = table;
   }

   public SymbolTable getSymbolTable() {
      return this.table;
   }

   public String getNullValueStrategy() {
      return this.nullValueStrategy;
   }

   protected void setNullValueStrategy(String nullValueStrategy) {
      this.nullValueStrategy = nullValueStrategy;
   }

   public NullValues.NullValueDegree getNullValueEncoding() {
      return this.nullValueEncoding;
   }

   public int getNextCode() {
      return this.nextCode;
   }

   protected void setNextCode(int nextCode) {
      this.nextCode = nextCode;
   }

   public boolean isNullValue(int code) {
      return code >= 0 && code < this.nextCode;
   }

   public boolean isNullValue(String symbol) {
      if (symbol != null && symbol.length() != 0 && symbol.charAt(0) == '#') {
         if (symbol.equals("#null#")) {
            return true;
         } else if ((this.nullValueEncoding == NullValues.NullValueDegree.ROOTNODE || this.nullValueEncoding == NullValues.NullValueDegree.NOVALUE) && symbol.equals("#rootnode#")) {
            return true;
         } else {
            return this.nullValueEncoding == NullValues.NullValueDegree.NOVALUE && symbol.equals("#novalue#");
         }
      } else {
         return false;
      }
   }

   public int nullvalueToCode(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
      if (!this.nullValue2CodeMap.containsKey(nullValueIdentifier)) {
         throw new SymbolException("Illegal null-value identifier. ");
      } else {
         return (Integer)this.nullValue2CodeMap.get(nullValueIdentifier);
      }
   }

   public String nullvalueToSymbol(NullValues.NullValueId nullValueIdentifier) throws MaltChainedException {
      if (!this.nullValue2SymbolMap.containsKey(nullValueIdentifier)) {
         throw new SymbolException("Illegal null-value identifier. ");
      } else {
         return (String)this.nullValue2SymbolMap.get(nullValueIdentifier);
      }
   }

   public int symbolToCode(String symbol) {
      return !this.symbol2CodeMap.containsKey(symbol) ? -1 : (Integer)this.symbol2CodeMap.get(symbol);
   }

   public String codeToSymbol(int code) {
      return !this.code2SymbolMap.containsKey(code) ? null : (String)this.code2SymbolMap.get(code);
   }

   protected abstract void setNullValueEncoding(String var1);

   protected abstract void makeNullValues();

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         NullValues nl = (NullValues)obj;
         if (!this.nullValueStrategy.equalsIgnoreCase(nl.getNullValueStrategy())) {
            return false;
         } else if (this.nextCode != nl.getNextCode()) {
            return false;
         } else if (!this.nullValue2SymbolMap.equals(nl.nullValue2SymbolMap)) {
            return false;
         } else if (!this.nullValue2CodeMap.equals(nl.nullValue2CodeMap)) {
            return false;
         } else if (!this.code2SymbolMap.equals(nl.code2SymbolMap)) {
            return false;
         } else {
            return this.symbol2CodeMap.equals(nl.symbol2CodeMap);
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Null-values:\n");
      sb.append("  Strategy: " + this.nullValueStrategy);
      sb.append("  NO_NODE -> " + this.nullValue2CodeMap.get(NullValues.NullValueId.NO_NODE) + " " + (String)this.nullValue2SymbolMap.get(NullValues.NullValueId.NO_NODE) + "\n");
      sb.append("  ROOT_NODE -> " + this.nullValue2CodeMap.get(NullValues.NullValueId.ROOT_NODE) + " " + (String)this.nullValue2SymbolMap.get(NullValues.NullValueId.ROOT_NODE) + "\n");
      sb.append("  NO_VALUE -> " + this.nullValue2CodeMap.get(NullValues.NullValueId.NO_VALUE) + " " + (String)this.nullValue2SymbolMap.get(NullValues.NullValueId.NO_VALUE) + "\n");
      return sb.toString();
   }

   public static enum NullValueId {
      NO_NODE,
      ROOT_NODE,
      NO_VALUE;

      private NullValueId() {
      }
   }

   protected static enum NullValueDegree {
      NONE,
      ONE,
      ROOTNODE,
      NOVALUE;

      private NullValueDegree() {
      }
   }
}
