package org.maltparser.core.syntaxgraph;

import java.util.Observer;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTableHandler;

public abstract class SyntaxGraph implements LabeledStructure, Observer {
   protected SymbolTableHandler symbolTables;
   protected final ObjectPoolList<LabelSet> labelSetPool;
   protected int numberOfComponents;

   public SyntaxGraph(SymbolTableHandler symbolTables) throws MaltChainedException {
      this.symbolTables = symbolTables;
      this.labelSetPool = new ObjectPoolList<LabelSet>() {
         protected LabelSet create() {
            return new LabelSet(6);
         }

         public void resetObject(LabelSet o) throws MaltChainedException {
            o.clear();
         }
      };
   }

   public SymbolTableHandler getSymbolTables() {
      return this.symbolTables;
   }

   public void setSymbolTables(SymbolTableHandler symbolTables) {
      this.symbolTables = symbolTables;
   }

   public void addLabel(Element element, String labelFunction, String label) throws MaltChainedException {
      element.addLabel(this.symbolTables.addSymbolTable(labelFunction), label);
   }

   public LabelSet checkOutNewLabelSet() throws MaltChainedException {
      return (LabelSet)this.labelSetPool.checkOut();
   }

   public void checkInLabelSet(LabelSet labelSet) throws MaltChainedException {
      this.labelSetPool.checkIn(labelSet);
   }

   public void clear() throws MaltChainedException {
      this.numberOfComponents = 0;
      this.labelSetPool.checkInAll();
   }
}
