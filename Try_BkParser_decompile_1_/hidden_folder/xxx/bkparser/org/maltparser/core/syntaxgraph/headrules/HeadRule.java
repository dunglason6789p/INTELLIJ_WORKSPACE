package org.maltparser.core.syntaxgraph.headrules;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

public class HeadRule extends ArrayList<PrioList> {
   public static final long serialVersionUID = 8045568022124826323L;
   protected HeadRules headRules;
   protected SymbolTable table;
   protected int symbolCode;
   protected Direction defaultDirection;

   public HeadRule(HeadRules headRules, String ruleSpec) throws MaltChainedException {
      this.setHeadRules(headRules);
      this.init(ruleSpec);
   }

   public void init(String ruleSpec) throws MaltChainedException {
      String spec = ruleSpec.trim();
      String[] items = spec.split("\t");
      if (items.length != 3) {
         throw new HeadRuleException("The specification of the head rule is not correct '" + ruleSpec + "'. ");
      } else {
         int index = items[0].indexOf(58);
         if (index == -1) {
            throw new HeadRuleException("The specification of the head rule is not correct '" + ruleSpec + "'. ");
         } else {
            SymbolTable t = this.headRules.getSymbolTableHandler().getSymbolTable(items[0].substring(0, index));
            if (t == null) {
               throw new HeadRuleException("The specification of the head rule is not correct '" + ruleSpec + "'. ");
            } else {
               this.setTable(t);
               this.setSymbolCode(this.table.addSymbol(items[0].substring(index + 1)));
               if (items[1].charAt(0) == 'r') {
                  this.defaultDirection = Direction.RIGHT;
               } else {
                  if (items[1].charAt(0) != 'l') {
                     throw new HeadRuleException("Could not determine the default direction of the head rule '" + ruleSpec + "'. ");
                  }

                  this.defaultDirection = Direction.LEFT;
               }

               if (items[2].length() > 1) {
                  if (items[2].indexOf(59) == -1) {
                     this.add(new PrioList(this, items[2]));
                  } else {
                     String[] lists = items[2].split(";");

                     for(int i = 0; i < lists.length; ++i) {
                        this.add(new PrioList(this, lists[i]));
                     }
                  }
               }

            }
         }
      }
   }

   public PhraseStructureNode getHeadChild(NonTerminalNode nt) throws MaltChainedException {
      PhraseStructureNode headChild = null;

      for(int i = 0; i < this.size(); ++i) {
         headChild = ((PrioList)this.get(i)).getHeadChild(nt);
         if (headChild != null) {
            break;
         }
      }

      return headChild;
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

   public void setSymbolCode(int symbolCode) {
      this.symbolCode = symbolCode;
   }

   public String getSymbolString() throws MaltChainedException {
      return this.table.getSymbolCodeToString(this.symbolCode);
   }

   public Direction getDefaultDirection() {
      return this.defaultDirection;
   }

   public void setDefaultDirection(Direction direction) {
      this.defaultDirection = direction;
   }

   public Logger getLogger() {
      return this.headRules.getLogger();
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.headRules.getDataFormatInstance();
   }

   public SymbolTableHandler getSymbolTableHandler() {
      return this.headRules.getSymbolTableHandler();
   }

   public void setHeadRules(HeadRules headRules) {
      this.headRules = headRules;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.table.getName());
      sb.append(':');

      try {
         sb.append(this.getSymbolString());
      } catch (MaltChainedException var3) {
         if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("", var3);
         } else {
            this.getLogger().error(var3.getMessageChain());
         }
      }

      sb.append('\t');
      if (this.defaultDirection == Direction.LEFT) {
         sb.append('l');
      } else if (this.defaultDirection == Direction.RIGHT) {
         sb.append('r');
      }

      sb.append('\t');
      if (this.size() == 0) {
         sb.append('*');
      } else {
         for(int i = 0; i < this.size(); ++i) {
            sb.append(this.get(i));
            if (i < this.size() - 1) {
               sb.append(';');
            }
         }
      }

      return sb.toString();
   }
}
