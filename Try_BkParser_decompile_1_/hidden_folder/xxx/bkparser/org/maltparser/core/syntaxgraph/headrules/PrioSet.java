package org.maltparser.core.syntaxgraph.headrules;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class PrioSet extends ArrayList<PrioSetMember> {
   public static final long serialVersionUID = 8045568022124816313L;
   protected PrioList prioList;
   protected PrioSetMember cache;

   public PrioSet(PrioList prioList) {
      this.setPrioList(prioList);
      this.cache = new PrioSetMember(this, (SymbolTable)null, (ColumnDescription)null, -1, PrioSetMember.RelationToPrevMember.START);
   }

   public PrioSet(PrioList prioList, String setSpec) throws MaltChainedException {
      this.setPrioList(prioList);
      this.cache = new PrioSetMember(this, (SymbolTable)null, (ColumnDescription)null, -1, PrioSetMember.RelationToPrevMember.START);
      this.init(setSpec);
   }

   public void init(String setSpec) throws MaltChainedException {
      String spec = setSpec.trim();
      String[] disItems = spec.split("\\|");

      for(int i = 0; i < disItems.length; ++i) {
         String[] conItems = spec.split("\\&");

         for(int j = 0; j < conItems.length; ++j) {
            int index = conItems[j].indexOf(58);
            if (index == -1) {
               throw new HeadRuleException("The specification of the priority list is not correct '" + setSpec + "'. ");
            }

            SymbolTable table = this.prioList.getSymbolTableHandler().getSymbolTable(conItems[j].substring(0, index));
            ColumnDescription column = this.prioList.getDataFormatInstance().getColumnDescriptionByName(conItems[j].substring(0, index));
            if (i == 0 && j == 0) {
               this.addPrioSetMember(table, column, conItems[j].substring(index + 1), PrioSetMember.RelationToPrevMember.START);
            } else if (j == 0) {
               this.addPrioSetMember(table, column, conItems[j].substring(index + 1), PrioSetMember.RelationToPrevMember.DISJUNCTION);
            } else {
               this.addPrioSetMember(table, column, conItems[j].substring(index + 1), PrioSetMember.RelationToPrevMember.CONJUNCTION);
            }
         }
      }

   }

   public PrioSetMember addPrioSetMember(SymbolTable table, ColumnDescription column, String symbolString, PrioSetMember.RelationToPrevMember relationToPrevMember) throws MaltChainedException {
      if (table == null) {
         throw new HeadRuleException("Could add a member to priority set because the symbol table could be found. ");
      } else {
         return this.addPrioSetMember(table, column, table.addSymbol(symbolString), relationToPrevMember);
      }
   }

   public PrioSetMember addPrioSetMember(SymbolTable table, ColumnDescription column, int symbolCode, PrioSetMember.RelationToPrevMember relationToPrevMember) throws MaltChainedException {
      this.cache.setTable(table);
      this.cache.setSymbolCode(symbolCode);
      if (!this.contains(this.cache)) {
         PrioSetMember newItem = new PrioSetMember(this, table, column, symbolCode, relationToPrevMember);
         this.add(newItem);
         return newItem;
      } else {
         return this.cache;
      }
   }

   public PhraseStructureNode getHeadChild(NonTerminalNode nt, Direction direction) throws MaltChainedException {
      boolean match = false;
      PhraseStructureNode child;
      int j;
      if (direction == Direction.LEFT) {
         Iterator i$ = nt.getChildren().iterator();

         while(i$.hasNext()) {
            child = (PhraseStructureNode)i$.next();

            for(j = 0; j < this.size(); ++j) {
               match = this.matchHeadChild(child, (PrioSetMember)this.get(j));
               if (match) {
                  if (j + 1 >= this.size()) {
                     return child;
                  }

                  if (((PrioSetMember)this.get(j)).getRelationToPrevMember() != PrioSetMember.RelationToPrevMember.CONJUNCTION) {
                     return child;
                  }
               }
            }
         }
      } else if (direction == Direction.RIGHT) {
         for(int i = nt.nChildren() - 1; i >= 0; --i) {
            child = nt.getChild(i);

            for(j = 0; j < this.size(); ++j) {
               match = this.matchHeadChild(child, (PrioSetMember)this.get(j));
               if (match) {
                  if (j + 1 >= this.size()) {
                     return child;
                  }

                  if (((PrioSetMember)this.get(j)).getRelationToPrevMember() != PrioSetMember.RelationToPrevMember.CONJUNCTION) {
                     return child;
                  }
               }
            }
         }
      }

      return null;
   }

   private boolean matchHeadChild(PhraseStructureNode child, PrioSetMember member) throws MaltChainedException {
      if (child instanceof NonTerminalNode && member.getTable().getName().equals("CAT") && member.getSymbolCode() == child.getLabelCode(member.getTable())) {
         return true;
      } else if (member.getTable().getName().equals("LABEL") && member.getSymbolCode() == child.getParentEdgeLabelCode(member.getTable())) {
         return true;
      } else {
         return child instanceof TokenNode && member.getColumn().getCategory() == 1 && member.getSymbolCode() == child.getLabelCode(member.getTable());
      }
   }

   public Logger getLogger() {
      return this.prioList.getLogger();
   }

   public PrioList getPrioList() {
      return this.prioList;
   }

   protected void setPrioList(PrioList prioList) {
      this.prioList = prioList;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : super.equals(obj);
      }
   }

   public int hashCode() {
      return super.hashCode();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < this.size(); ++i) {
         if (i != 0) {
            if (((PrioSetMember)this.get(i)).getRelationToPrevMember() == PrioSetMember.RelationToPrevMember.CONJUNCTION) {
               sb.append('&');
            } else if (((PrioSetMember)this.get(i)).getRelationToPrevMember() == PrioSetMember.RelationToPrevMember.DISJUNCTION) {
               sb.append('|');
            }
         }

         sb.append(this.get(i));
      }

      return sb.toString();
   }
}
