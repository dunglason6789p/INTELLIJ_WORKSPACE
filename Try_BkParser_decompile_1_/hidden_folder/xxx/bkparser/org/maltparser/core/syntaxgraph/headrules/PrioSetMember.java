package org.maltparser.core.syntaxgraph.headrules;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.symbol.SymbolTable;

public class PrioSetMember {
   protected PrioSet prioSet;
   protected SymbolTable table;
   protected ColumnDescription column;
   protected int symbolCode;
   protected PrioSetMember.RelationToPrevMember relationToPrevMember;

   public PrioSetMember(PrioSet prioSet, SymbolTable table, ColumnDescription column, int symbolCode, PrioSetMember.RelationToPrevMember relationToPrevMember) {
      this.setPrioSet(prioSet);
      this.setTable(table);
      this.setColumn(column);
      this.setSymbolCode(symbolCode);
      this.setRelationToPrevMember(relationToPrevMember);
   }

   public PrioSetMember(PrioSet prioSet, SymbolTable table, ColumnDescription column, String symbolString, PrioSetMember.RelationToPrevMember relationToPrevMember) throws MaltChainedException {
      this.setPrioSet(prioSet);
      this.setTable(table);
      this.setColumn(column);
      if (table != null) {
         this.setSymbolCode(table.getSymbolStringToCode(symbolString));
      } else {
         this.setSymbolCode(-1);
      }

      this.setRelationToPrevMember(relationToPrevMember);
   }

   public PrioSet getPrioSet() {
      return this.prioSet;
   }

   public void setPrioSet(PrioSet prioSet) {
      this.prioSet = prioSet;
   }

   public ColumnDescription getColumn() {
      return this.column;
   }

   public void setColumn(ColumnDescription column) {
      this.column = column;
   }

   public SymbolTable getTable() {
      return this.table;
   }

   public void setTable(SymbolTable table) {
      this.table = table;
   }

   public int getSymbolCode() {
      return this.symbolCode;
   }

   public String getSymbolString() throws MaltChainedException {
      return this.table != null && this.symbolCode >= 0 ? this.table.getSymbolCodeToString(this.symbolCode) : null;
   }

   public void setSymbolCode(int symbolCode) {
      this.symbolCode = symbolCode;
   }

   public PrioSetMember.RelationToPrevMember getRelationToPrevMember() {
      return this.relationToPrevMember;
   }

   public void setRelationToPrevMember(PrioSetMember.RelationToPrevMember relationToPrevMember) {
      this.relationToPrevMember = relationToPrevMember;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + this.symbolCode;
      result = 31 * result + (this.relationToPrevMember == null ? 0 : this.relationToPrevMember.hashCode());
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         PrioSetMember other = (PrioSetMember)obj;
         if (this.symbolCode != other.symbolCode) {
            return false;
         } else {
            if (this.relationToPrevMember == null) {
               if (other.relationToPrevMember != null) {
                  return false;
               }
            } else if (!this.relationToPrevMember.equals(other.relationToPrevMember)) {
               return false;
            }

            return true;
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.table.getName());
      sb.append(':');

      try {
         sb.append(this.getSymbolString());
      } catch (MaltChainedException var3) {
         if (this.prioSet.getLogger().isDebugEnabled()) {
            this.prioSet.getLogger().debug("", var3);
         } else {
            this.prioSet.getLogger().error(var3.getMessageChain());
         }
      }

      return sb.toString();
   }

   protected static enum RelationToPrevMember {
      START,
      DISJUNCTION,
      CONJUNCTION;

      private RelationToPrevMember() {
      }
   }
}
