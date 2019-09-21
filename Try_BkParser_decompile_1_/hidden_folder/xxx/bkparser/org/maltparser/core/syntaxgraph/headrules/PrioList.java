package org.maltparser.core.syntaxgraph.headrules;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

public class PrioList extends ArrayList<PrioSet> {
   public static final long serialVersionUID = 8045568022124816323L;
   protected HeadRule headRule;
   protected Direction direction;

   public PrioList(HeadRule headRule, String listSpec) throws MaltChainedException {
      this.setHeadRule(headRule);
      this.init(listSpec);
   }

   public void init(String listSpec) throws MaltChainedException {
      String spec = listSpec.trim();
      if (spec.length() < 8) {
         throw new HeadRuleException("The specification of the priority list is not correct '" + listSpec + "'. ");
      } else {
         if (spec.charAt(0) == 'r') {
            this.direction = Direction.RIGHT;
         } else {
            if (spec.charAt(0) != 'l') {
               throw new HeadRuleException("Could not determine the direction of the priority list '" + listSpec + "'. ");
            }

            this.direction = Direction.LEFT;
         }

         if (spec.charAt(1) == '[' && spec.charAt(spec.length() - 1) == ']') {
            String[] items = spec.substring(2, spec.length() - 1).split(" ");

            for(int i = 0; i < items.length; ++i) {
               this.add(new PrioSet(this, items[i]));
            }

         } else {
            throw new HeadRuleException("The specification of the priority list is not correct '" + listSpec + "'. ");
         }
      }
   }

   public PhraseStructureNode getHeadChild(NonTerminalNode nt) throws MaltChainedException {
      PhraseStructureNode headChild = null;
      int i = 0;

      for(int n = this.size(); i < n; ++i) {
         headChild = ((PrioSet)this.get(i)).getHeadChild(nt, this.direction);
         if (headChild != null) {
            break;
         }
      }

      return headChild;
   }

   public Logger getLogger() {
      return this.headRule.getLogger();
   }

   public DataFormatInstance getDataFormatInstance() {
      return this.headRule.getDataFormatInstance();
   }

   public SymbolTableHandler getSymbolTableHandler() {
      return this.headRule.getSymbolTableHandler();
   }

   public HeadRule getHeadRule() {
      return this.headRule;
   }

   public void setHeadRule(HeadRule headRule) {
      this.headRule = headRule;
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
      if (this.direction == Direction.LEFT) {
         sb.append("l[");
      } else if (this.direction == Direction.RIGHT) {
         sb.append("r[");
      }

      Iterator i$ = this.iterator();

      while(i$.hasNext()) {
         PrioSet set = (PrioSet)i$.next();
         sb.append(set);
         sb.append(' ');
      }

      if (sb.length() != 0) {
         sb.setLength(sb.length() - 1);
      }

      sb.append("]");
      return sb.toString();
   }
}
