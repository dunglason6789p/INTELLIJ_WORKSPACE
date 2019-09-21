package org.maltparser.concurrent.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public final class ConcurrentDependencyGraph {
   private static final String TAB_SIGN = "\t";
   private final DataFormat dataFormat;
   private final ConcurrentDependencyNode[] nodes;

   public ConcurrentDependencyGraph(ConcurrentDependencyGraph graph) throws ConcurrentGraphException {
      this.dataFormat = graph.dataFormat;
      this.nodes = new ConcurrentDependencyNode[graph.nodes.length + 1];

      for(int i = 0; i < graph.nodes.length; ++i) {
         this.nodes[i] = new ConcurrentDependencyNode(this, graph.nodes[i]);
      }

   }

   public ConcurrentDependencyGraph(DataFormat dataFormat, String[] inputTokens) throws ConcurrentGraphException {
      this.dataFormat = dataFormat;
      this.nodes = new ConcurrentDependencyNode[inputTokens.length + 1];
      this.nodes[0] = new ConcurrentDependencyNode(this, 0, (String[])null);

      int i;
      for(i = 0; i < inputTokens.length; ++i) {
         String[] columns = inputTokens[i].split("\t");
         this.nodes[i + 1] = new ConcurrentDependencyNode(this, i + 1, columns);
      }

      for(i = 0; i < this.nodes.length; ++i) {
         if (this.nodes[i].getHeadIndex() >= this.nodes.length) {
            throw new ConcurrentGraphException("Not allowed to add a head node that doesn't exists");
         }
      }

   }

   public ConcurrentDependencyGraph(DataFormat dataFormat, DependencyStructure sourceGraph, String defaultRootLabel) throws MaltChainedException {
      this.dataFormat = dataFormat;
      this.nodes = new ConcurrentDependencyNode[sourceGraph.nDependencyNode()];
      this.nodes[0] = new ConcurrentDependencyNode(this, 0, (String[])null);

      int index;
      String[] columns;
      for(Iterator i$ = sourceGraph.getTokenIndices().iterator(); i$.hasNext(); this.nodes[index] = new ConcurrentDependencyNode(this, index, columns)) {
         index = (Integer)i$.next();
         DependencyNode gnode = sourceGraph.getDependencyNode(index);
         columns = new String[dataFormat.numberOfColumns()];

         for(int i = 0; i < dataFormat.numberOfColumns(); ++i) {
            ColumnDescription column = dataFormat.getColumnDescription(i);
            if (!column.isInternal()) {
               if (column.getCategory() == 1) {
                  columns[i] = gnode.getLabelSymbol(sourceGraph.getSymbolTables().getSymbolTable(column.getName()));
               } else if (column.getCategory() == 2) {
                  if (gnode.hasHead()) {
                     columns[i] = Integer.toString(gnode.getHeadEdge().getSource().getIndex());
                  } else {
                     columns[i] = Integer.toString(-1);
                  }
               } else if (column.getCategory() == 3) {
                  SymbolTable sourceTable = sourceGraph.getSymbolTables().getSymbolTable(column.getName());
                  if (gnode.getHeadEdge().hasLabel(sourceTable)) {
                     columns[i] = gnode.getHeadEdge().getLabelSymbol(sourceTable);
                  } else {
                     columns[i] = defaultRootLabel;
                  }
               } else {
                  columns[i] = "_";
               }
            }
         }
      }

   }

   protected ConcurrentDependencyGraph(DataFormat dataFormat, ConcurrentDependencyNode[] inputNodes) throws ConcurrentGraphException {
      this.dataFormat = dataFormat;
      this.nodes = new ConcurrentDependencyNode[inputNodes.length];

      int i;
      for(i = 0; i < inputNodes.length; ++i) {
         this.nodes[i] = inputNodes[i];
      }

      for(i = 0; i < this.nodes.length; ++i) {
         if (this.nodes[i].getHeadIndex() >= this.nodes.length) {
            throw new ConcurrentGraphException("Not allowed to add a head node that doesn't exists");
         }
      }

   }

   public DataFormat getDataFormat() {
      return this.dataFormat;
   }

   public ConcurrentDependencyNode getRoot() {
      return this.nodes[0];
   }

   public ConcurrentDependencyNode getDependencyNode(int index) {
      return index >= 0 && index < this.nodes.length ? this.nodes[index] : null;
   }

   public ConcurrentDependencyNode getTokenNode(int index) {
      return index > 0 && index < this.nodes.length ? this.nodes[index] : null;
   }

   public int nDependencyNodes() {
      return this.nodes.length;
   }

   public int nTokenNodes() {
      return this.nodes.length - 1;
   }

   public int getHighestDependencyNodeIndex() {
      return this.nodes.length - 1;
   }

   public int getHighestTokenIndex() {
      return this.nodes.length == 1 ? -1 : this.nodes.length - 1;
   }

   public boolean hasTokens() {
      return this.nodes.length > 1;
   }

   public int nEdges() {
      int n = 0;

      for(int i = 1; i < this.nodes.length; ++i) {
         if (this.nodes[i].hasHead()) {
            ++n;
         }
      }

      return n;
   }

   public SortedSet<ConcurrentDependencyEdge> getEdges() throws ConcurrentGraphException {
      SortedSet<ConcurrentDependencyEdge> edges = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 1; i < this.nodes.length; ++i) {
         ConcurrentDependencyEdge edge = this.nodes[i].getHeadEdge();
         if (edge != null) {
            edges.add(edge);
         }
      }

      return edges;
   }

   public SortedSet<Integer> getDependencyIndices() {
      SortedSet<Integer> indices = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 0; i < this.nodes.length; ++i) {
         indices.add(i);
      }

      return indices;
   }

   public SortedSet<Integer> getTokenIndices() {
      SortedSet<Integer> indices = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 1; i < this.nodes.length; ++i) {
         indices.add(i);
      }

      return indices;
   }

   public boolean hasLabeledDependency(int index) {
      if (index >= 0 && index < this.nodes.length) {
         return !this.nodes[index].hasHead() ? false : this.nodes[index].isHeadLabeled();
      } else {
         return false;
      }
   }

   public boolean isConnected() {
      int[] components = this.findComponents();
      int tmp = components[0];

      for(int i = 1; i < components.length; ++i) {
         if (tmp != components[i]) {
            return false;
         }
      }

      return true;
   }

   public boolean isProjective() {
      for(int i = 1; i < this.nodes.length; ++i) {
         if (!this.nodes[i].isProjective()) {
            return false;
         }
      }

      return true;
   }

   public boolean isSingleHeaded() {
      return true;
   }

   public boolean isTree() {
      return this.isConnected() && this.isSingleHeaded();
   }

   public int nNonProjectiveEdges() {
      int c = 0;

      for(int i = 1; i < this.nodes.length; ++i) {
         if (!this.nodes[i].isProjective()) {
            ++c;
         }
      }

      return c;
   }

   protected boolean hasDependent(int nodeIndex) {
      for(int i = 1; i < this.nodes.length; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            return true;
         }
      }

      return false;
   }

   protected boolean hasLeftDependent(int nodeIndex) {
      for(int i = 1; i < nodeIndex; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            return true;
         }
      }

      return false;
   }

   protected boolean hasRightDependent(int nodeIndex) {
      for(int i = nodeIndex + 1; i < this.nodes.length; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            return true;
         }
      }

      return false;
   }

   protected List<ConcurrentDependencyNode> getListOfLeftDependents(int nodeIndex) {
      List<ConcurrentDependencyNode> leftDependents = Collections.synchronizedList(new ArrayList());

      for(int i = 1; i < nodeIndex; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            leftDependents.add(this.nodes[i]);
         }
      }

      return leftDependents;
   }

   protected SortedSet<ConcurrentDependencyNode> getSortedSetOfLeftDependents(int nodeIndex) {
      SortedSet<ConcurrentDependencyNode> leftDependents = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 1; i < nodeIndex; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            leftDependents.add(this.nodes[i]);
         }
      }

      return leftDependents;
   }

   protected List<ConcurrentDependencyNode> getListOfRightDependents(int nodeIndex) {
      List<ConcurrentDependencyNode> rightDependents = Collections.synchronizedList(new ArrayList());

      for(int i = nodeIndex + 1; i < this.nodes.length; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            rightDependents.add(this.nodes[i]);
         }
      }

      return rightDependents;
   }

   protected SortedSet<ConcurrentDependencyNode> getSortedSetOfRightDependents(int nodeIndex) {
      SortedSet<ConcurrentDependencyNode> rightDependents = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = nodeIndex + 1; i < this.nodes.length; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            rightDependents.add(this.nodes[i]);
         }
      }

      return rightDependents;
   }

   protected List<ConcurrentDependencyNode> getListOfDependents(int nodeIndex) {
      List<ConcurrentDependencyNode> dependents = Collections.synchronizedList(new ArrayList());

      for(int i = 1; i < this.nodes.length; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            dependents.add(this.nodes[i]);
         }
      }

      return dependents;
   }

   protected SortedSet<ConcurrentDependencyNode> getSortedSetOfDependents(int nodeIndex) {
      SortedSet<ConcurrentDependencyNode> dependents = Collections.synchronizedSortedSet(new TreeSet());

      for(int i = 1; i < this.nodes.length; ++i) {
         if (nodeIndex == this.nodes[i].getHeadIndex()) {
            dependents.add(this.nodes[i]);
         }
      }

      return dependents;
   }

   protected int getRank(int nodeIndex) {
      int[] components = new int[this.nodes.length];
      int[] ranks = new int[this.nodes.length];

      int i;
      for(i = 0; i < components.length; ++i) {
         components[i] = i;
         ranks[i] = 0;
      }

      for(i = 1; i < this.nodes.length; ++i) {
         if (this.nodes[i].hasHead()) {
            int hcIndex = this.findComponent(this.nodes[i].getHead().getIndex(), components);
            int dcIndex = this.findComponent(this.nodes[i].getIndex(), components);
            if (hcIndex != dcIndex) {
               this.link(hcIndex, dcIndex, components, ranks);
            }
         }
      }

      return ranks[nodeIndex];
   }

   protected ConcurrentDependencyNode findComponent(int nodeIndex) {
      int[] components = new int[this.nodes.length];
      int[] ranks = new int[this.nodes.length];

      int i;
      for(i = 0; i < components.length; ++i) {
         components[i] = i;
         ranks[i] = 0;
      }

      for(i = 1; i < this.nodes.length; ++i) {
         if (this.nodes[i].hasHead()) {
            int hcIndex = this.findComponent(this.nodes[i].getHead().getIndex(), components);
            int dcIndex = this.findComponent(this.nodes[i].getIndex(), components);
            if (hcIndex != dcIndex) {
               this.link(hcIndex, dcIndex, components, ranks);
            }
         }
      }

      return this.nodes[this.findComponent(nodeIndex, components)];
   }

   private int[] findComponents() {
      int[] components = new int[this.nodes.length];
      int[] ranks = new int[this.nodes.length];

      int i;
      for(i = 0; i < components.length; ++i) {
         components[i] = i;
         ranks[i] = 0;
      }

      for(i = 1; i < this.nodes.length; ++i) {
         if (this.nodes[i].hasHead()) {
            int hcIndex = this.findComponent(this.nodes[i].getHead().getIndex(), components);
            int dcIndex = this.findComponent(this.nodes[i].getIndex(), components);
            if (hcIndex != dcIndex) {
               this.link(hcIndex, dcIndex, components, ranks);
            }
         }
      }

      return components;
   }

   private int findComponent(int xIndex, int[] components) {
      if (xIndex != components[xIndex]) {
         components[xIndex] = this.findComponent(components[xIndex], components);
      }

      return components[xIndex];
   }

   private int link(int xIndex, int yIndex, int[] components, int[] ranks) {
      if (ranks[xIndex] > ranks[yIndex]) {
         components[yIndex] = xIndex;
         return xIndex;
      } else {
         components[xIndex] = yIndex;
         if (ranks[xIndex] == ranks[yIndex]) {
            int var10002 = ranks[yIndex]++;
         }

         return yIndex;
      }
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.dataFormat == null ? 0 : this.dataFormat.hashCode());
      result = 31 * result + Arrays.hashCode(this.nodes);
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
         ConcurrentDependencyGraph other = (ConcurrentDependencyGraph)obj;
         if (this.dataFormat == null) {
            if (other.dataFormat != null) {
               return false;
            }
         } else if (!this.dataFormat.equals(other.dataFormat)) {
            return false;
         }

         return Arrays.equals(this.nodes, other.nodes);
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();

      for(int i = 1; i < this.nodes.length; ++i) {
         sb.append(this.nodes[i]);
         sb.append('\n');
      }

      return sb.toString();
   }
}
