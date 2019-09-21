package org.maltparser.core.syntaxgraph;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Observable;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;

public abstract class GraphElement extends Observable implements Element {
   private LabeledStructure belongsToGraph = null;
   private LabelSet labelSet = null;

   public GraphElement() {
   }

   public void addLabel(SymbolTable table, String symbol) throws MaltChainedException {
      table.addSymbol(symbol);
      this.addLabel(table, table.getSymbolStringToCode(symbol));
   }

   public void addLabel(SymbolTable table, int code) throws MaltChainedException {
      if (table.getSymbolCodeToString(code) != null) {
         if (this.labelSet == null) {
            if (this.belongsToGraph == null) {
               throw new SyntaxGraphException("The graph element doesn't belong to any graph. ");
            }

            this.labelSet = this.belongsToGraph.checkOutNewLabelSet();
         }

         this.labelSet.put(table, code);
         this.setChanged();
         this.notifyObservers(table);
      }

   }

   public void addLabel(LabelSet labels) throws MaltChainedException {
      if (labels != null) {
         Iterator i$ = labels.keySet().iterator();

         while(i$.hasNext()) {
            SymbolTable table = (SymbolTable)i$.next();
            this.addLabel(table, (Integer)labels.get(table));
         }
      }

   }

   public boolean hasLabel(SymbolTable table) throws MaltChainedException {
      return this.labelSet != null ? this.labelSet.containsKey(table) : false;
   }

   public String getLabelSymbol(SymbolTable table) throws MaltChainedException {
      Integer code = (Integer)this.labelSet.get(table);
      if (code == null) {
         throw new SyntaxGraphException("No label symbol available for label '" + table.getName() + "'.");
      } else {
         return table.getSymbolCodeToString(code);
      }
   }

   public int getLabelCode(SymbolTable table) throws MaltChainedException {
      Integer code = (Integer)this.labelSet.get(table);
      if (code == null) {
         throw new SyntaxGraphException("No label symbol available for label '" + table.getName() + "'.");
      } else {
         return code;
      }
   }

   public boolean isLabeled() {
      if (this.labelSet == null) {
         return false;
      } else {
         return this.labelSet.size() > 0;
      }
   }

   public int nLabels() {
      return this.labelSet == null ? 0 : this.labelSet.size();
   }

   public Set<SymbolTable> getLabelTypes() {
      return (Set)(this.labelSet == null ? new LinkedHashSet() : this.labelSet.keySet());
   }

   public LabelSet getLabelSet() {
      return this.labelSet;
   }

   public void removeLabel(SymbolTable table) throws MaltChainedException {
      if (this.labelSet != null) {
         this.labelSet.remove(table);
      }

   }

   public void removeLabels() throws MaltChainedException {
      if (this.labelSet != null && this.belongsToGraph != null) {
         this.belongsToGraph.checkInLabelSet(this.labelSet);
      }

      this.labelSet = null;
   }

   public LabeledStructure getBelongsToGraph() {
      return this.belongsToGraph;
   }

   public void setBelongsToGraph(LabeledStructure belongsToGraph) {
      this.belongsToGraph = belongsToGraph;
      this.addObserver((SyntaxGraph)belongsToGraph);
   }

   public void clear() throws MaltChainedException {
      if (this.labelSet != null && this.belongsToGraph != null) {
         this.belongsToGraph.checkInLabelSet(this.labelSet);
      }

      this.labelSet = null;
      this.deleteObserver((SyntaxGraph)this.belongsToGraph);
      this.belongsToGraph = null;
   }

   public boolean equals(Object obj) {
      GraphElement ge = (GraphElement)obj;
      return this.belongsToGraph == ge.getBelongsToGraph() && this.labelSet == null ? ge.getLabelSet() == null : this.labelSet.equals(ge.getLabelSet());
   }

   public int hashCode() {
      int hash = 7;
      int hash = 31 * hash + (null == this.belongsToGraph ? 0 : this.belongsToGraph.hashCode());
      return 31 * hash + (null == this.labelSet ? 0 : this.labelSet.hashCode());
   }

   public int compareTo(GraphElement o) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == o) {
         return 0;
      } else if (this.labelSet == null && o.labelSet != null) {
         return -1;
      } else if (this.labelSet != null && o.labelSet == null) {
         return 1;
      } else if (this.labelSet == null && o.labelSet == null) {
         return 0;
      } else {
         int comparison = false;
         Iterator i$ = this.labelSet.keySet().iterator();

         while(i$.hasNext()) {
            SymbolTable table = (SymbolTable)i$.next();
            Integer ocode = (Integer)o.labelSet.get(table);
            Integer tcode = (Integer)this.labelSet.get(table);
            if (ocode != null && tcode != null && !ocode.equals(tcode)) {
               try {
                  int comparison = table.getSymbolCodeToString(tcode).compareTo(table.getSymbolCodeToString(ocode));
                  if (comparison != 0) {
                     return comparison;
                  }
               } catch (MaltChainedException var11) {
               }
            }
         }

         return 0;
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      if (this.labelSet != null) {
         for(Iterator i$ = this.labelSet.keySet().iterator(); i$.hasNext(); sb.append(' ')) {
            SymbolTable table = (SymbolTable)i$.next();

            try {
               sb.append(table.getName());
               sb.append(':');
               sb.append(this.getLabelSymbol(table));
            } catch (MaltChainedException var5) {
               System.err.println("Print error : " + var5.getMessageChain());
            }
         }
      }

      return sb.toString();
   }
}
