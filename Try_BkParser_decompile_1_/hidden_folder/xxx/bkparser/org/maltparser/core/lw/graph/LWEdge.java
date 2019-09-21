package org.maltparser.core.lw.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.Node;

public final class LWEdge implements Edge, Comparable<LWEdge> {
   private final Node source;
   private final Node target;
   private final SortedMap<ColumnDescription, String> labels;

   protected LWEdge(LWEdge edge) throws LWGraphException {
      this.source = edge.source;
      this.target = edge.target;
      this.labels = new TreeMap(edge.labels);
   }

   protected LWEdge(Node _source, Node _target, SortedMap<ColumnDescription, String> _labels) throws MaltChainedException {
      if (_source.getBelongsToGraph() != _target.getBelongsToGraph()) {
         throw new LWGraphException("The source node and target node must belong to the same dependency graph.");
      } else {
         this.source = _source;
         this.target = _target;
         this.labels = _labels;
         SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
         Iterator i$ = this.labels.keySet().iterator();

         while(i$.hasNext()) {
            ColumnDescription column = (ColumnDescription)i$.next();
            SymbolTable table = symbolTableHandler.addSymbolTable(column.getName());
            table.addSymbol((String)this.labels.get(column));
         }

      }
   }

   protected LWEdge(Node _source, Node _target) throws MaltChainedException {
      if (_source.getBelongsToGraph() != _target.getBelongsToGraph()) {
         throw new LWGraphException("The source node and target node must belong to the same dependency graph.");
      } else {
         this.source = _source;
         this.target = _target;
         this.labels = new TreeMap();
      }
   }

   public Node getSource() {
      return this.source;
   }

   public Node getTarget() {
      return this.target;
   }

   public String getLabel(ColumnDescription column) {
      if (this.labels.containsKey(column)) {
         return (String)this.labels.get(column);
      } else {
         return column.getCategory() == 7 ? column.getDefaultOutput() : "";
      }
   }

   public int nLabels() {
      return this.labels.size();
   }

   public boolean isLabeled() {
      return this.labels.size() > 0;
   }

   public void setEdge(Node source, Node target, int type) throws MaltChainedException {
      throw new LWGraphException("Not implemented in light-weight dependency graph");
   }

   public int getType() {
      return 1;
   }

   public void addLabel(SymbolTable table, String symbol) throws MaltChainedException {
      LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
      ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
      table.addSymbol(symbol);
      this.labels.put(column, symbol);
   }

   public void addLabel(SymbolTable table, int code) throws MaltChainedException {
      this.addLabel(table, table.getSymbolCodeToString(code));
   }

   public void addLabel(LabelSet labelSet) throws MaltChainedException {
      Iterator i$ = labelSet.keySet().iterator();

      while(i$.hasNext()) {
         SymbolTable table = (SymbolTable)i$.next();
         this.addLabel(table, (Integer)labelSet.get(table));
      }

   }

   public boolean hasLabel(SymbolTable table) throws MaltChainedException {
      if (table == null) {
         return false;
      } else {
         LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
         ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
         return this.labels.containsKey(column);
      }
   }

   public String getLabelSymbol(SymbolTable table) throws MaltChainedException {
      LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
      ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
      return (String)this.labels.get(column);
   }

   public int getLabelCode(SymbolTable table) throws MaltChainedException {
      LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
      ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
      return table.getSymbolStringToCode((String)this.labels.get(column));
   }

   public Set<SymbolTable> getLabelTypes() {
      Set<SymbolTable> labelTypes = new HashSet();
      SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
      Iterator i$ = this.labels.keySet().iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();

         try {
            labelTypes.add(symbolTableHandler.getSymbolTable(column.getName()));
         } catch (MaltChainedException var6) {
            var6.printStackTrace();
         }
      }

      return labelTypes;
   }

   public LabelSet getLabelSet() {
      SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
      LabelSet labelSet = new LabelSet();
      Iterator i$ = this.labels.keySet().iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();

         try {
            SymbolTable table = symbolTableHandler.getSymbolTable(column.getName());
            int code = table.getSymbolStringToCode((String)this.labels.get(column));
            labelSet.put(table, code);
         } catch (MaltChainedException var7) {
            var7.printStackTrace();
         }
      }

      return labelSet;
   }

   public void removeLabel(SymbolTable table) throws MaltChainedException {
      LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
      ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
      this.labels.remove(column);
   }

   public void removeLabels() throws MaltChainedException {
      this.labels.clear();
   }

   public LabeledStructure getBelongsToGraph() {
      return this.target.getBelongsToGraph();
   }

   public void setBelongsToGraph(LabeledStructure belongsToGraph) {
   }

   public void clear() throws MaltChainedException {
      this.labels.clear();
   }

   public int compareTo(LWEdge that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == that) {
         return 0;
      } else if (this.target.getIndex() < that.target.getIndex()) {
         return -1;
      } else if (this.target.getIndex() > that.target.getIndex()) {
         return 1;
      } else if (this.source.getIndex() < that.source.getIndex()) {
         return -1;
      } else if (this.source.getIndex() > that.source.getIndex()) {
         return 1;
      } else if (this.labels.equals(that.labels)) {
         return 0;
      } else {
         Iterator<ColumnDescription> itthis = this.labels.keySet().iterator();
         Iterator itthat = that.labels.keySet().iterator();

         while(itthis.hasNext() && itthat.hasNext()) {
            ColumnDescription keythis = (ColumnDescription)itthis.next();
            ColumnDescription keythat = (ColumnDescription)itthat.next();
            if (keythis.getPosition() < keythat.getPosition()) {
               return -1;
            }

            if (keythis.getPosition() > keythat.getPosition()) {
               return 1;
            }

            if (((String)this.labels.get(keythis)).compareTo((String)that.labels.get(keythat)) != 0) {
               return ((String)this.labels.get(keythis)).compareTo((String)that.labels.get(keythat));
            }
         }

         if (!itthis.hasNext() && itthat.hasNext()) {
            return -1;
         } else {
            return itthis.hasNext() && !itthat.hasNext() ? 1 : 0;
         }
      }
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + this.source.getIndex();
      result = 31 * result + this.target.getIndex();
      result = 31 * result + (this.labels == null ? 0 : this.labels.hashCode());
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
         LWEdge other = (LWEdge)obj;
         if (this.source.getIndex() != other.source.getIndex()) {
            return false;
         } else if (this.target.getIndex() != other.target.getIndex()) {
            return false;
         } else {
            if (this.labels == null) {
               if (other.labels != null) {
                  return false;
               }
            } else if (!this.labels.equals(other.labels)) {
               return false;
            }

            return true;
         }
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.source);
      sb.append(" -> ");
      sb.append(this.target);
      if (this.labels.size() > 0) {
         int i = 1;
         sb.append(" {");

         for(Iterator i$ = this.labels.keySet().iterator(); i$.hasNext(); ++i) {
            ColumnDescription column = (ColumnDescription)i$.next();
            sb.append(column.getName());
            sb.append('=');
            sb.append((String)this.labels.get(column));
            if (i < this.labels.size()) {
               sb.append(',');
            }
         }

         sb.append(" }");
      }

      return sb.toString();
   }
}
