package org.maltparser.parser.history.container;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;

public class TableContainer {
   protected int cachedCode;
   protected final StringBuilder cachedSymbol;
   protected Table table;
   protected String name;
   private final TableContainer.RelationToNextDecision relationToNextDecision;

   public TableContainer(Table _table, String _name, char _decisionSeparator) {
      this.table = _table;
      this.name = _name;
      switch(_decisionSeparator) {
      case '#':
         this.relationToNextDecision = TableContainer.RelationToNextDecision.BRANCHED;
         break;
      case '+':
         this.relationToNextDecision = TableContainer.RelationToNextDecision.COMBINED;
         break;
      case ',':
         this.relationToNextDecision = TableContainer.RelationToNextDecision.SEQUANTIAL;
         break;
      case ';':
         this.relationToNextDecision = TableContainer.RelationToNextDecision.BRANCHED;
         break;
      case '?':
         this.relationToNextDecision = TableContainer.RelationToNextDecision.SWITCHED;
         break;
      default:
         this.relationToNextDecision = TableContainer.RelationToNextDecision.NONE;
      }

      this.cachedSymbol = new StringBuilder();
      this.cachedCode = -1;
   }

   public void clearCache() {
      this.cachedCode = -1;
      this.cachedSymbol.setLength(0);
   }

   public String getSymbol(int code) throws MaltChainedException {
      if (code < 0 && !this.containCode(code)) {
         this.clearCache();
         return null;
      } else {
         if (this.cachedCode != code) {
            this.clearCache();
            this.cachedCode = code;
            this.cachedSymbol.append(this.table.getSymbolCodeToString(this.cachedCode));
         }

         return this.cachedSymbol.toString();
      }
   }

   public int getCode(String symbol) throws MaltChainedException {
      if (this.cachedSymbol == null || !this.cachedSymbol.equals(symbol)) {
         this.clearCache();
         this.cachedSymbol.append(symbol);
         this.cachedCode = this.table.getSymbolStringToCode(symbol);
      }

      return this.cachedCode;
   }

   public boolean containCode(int code) throws MaltChainedException {
      if (this.cachedCode != code) {
         this.clearCache();
         this.cachedSymbol.append(this.table.getSymbolCodeToString(code));
         if (this.cachedSymbol == null) {
            return false;
         }

         this.cachedCode = code;
      }

      return true;
   }

   public boolean containSymbol(String symbol) throws MaltChainedException {
      if (this.cachedSymbol == null || !this.cachedSymbol.equals(symbol)) {
         this.clearCache();
         this.cachedCode = this.table.getSymbolStringToCode(symbol);
         if (this.cachedCode < 0) {
            return false;
         }

         this.cachedSymbol.append(symbol);
      }

      return true;
   }

   public boolean continueWithNextDecision(int code) throws MaltChainedException {
      return this.table instanceof DecisionPropertyTable ? ((DecisionPropertyTable)this.table).continueWithNextDecision(code) : true;
   }

   public boolean continueWithNextDecision(String symbol) throws MaltChainedException {
      return this.table instanceof DecisionPropertyTable ? ((DecisionPropertyTable)this.table).continueWithNextDecision(symbol) : true;
   }

   public Table getTable() {
      return this.table;
   }

   public String getTableName() {
      return this.table != null ? this.table.getName() : null;
   }

   public String getTableContainerName() {
      return this.name;
   }

   public TableContainer.RelationToNextDecision getRelationToNextDecision() {
      return this.relationToNextDecision;
   }

   protected void setTable(Table table) {
      this.table = table;
   }

   protected void setName(String name) {
      this.name = name;
   }

   public int size() {
      return this.table.size();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.name);
      sb.append(" -> ");
      sb.append(this.cachedSymbol);
      sb.append(" = ");
      sb.append(this.cachedCode);
      return sb.toString();
   }

   public static enum RelationToNextDecision {
      COMBINED,
      SEQUANTIAL,
      BRANCHED,
      SWITCHED,
      NONE;

      private RelationToNextDecision() {
      }
   }
}
