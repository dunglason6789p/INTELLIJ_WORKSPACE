package org.maltparser.core.lw.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;

public final class LWNode implements DependencyNode, Node {
   private final LWDependencyGraph graph;
   private int index;
   private final Map<Integer, String> labels;
   private Edge headEdge;

   protected LWNode(LWNode node) throws LWGraphException {
      this(node.graph, node);
   }

   protected LWNode(LWDependencyGraph _graph, LWNode node) throws LWGraphException {
      if (_graph == null) {
         throw new LWGraphException("The graph node must belong to a dependency graph.");
      } else {
         this.graph = _graph;
         this.index = node.index;
         this.labels = new HashMap(node.labels);
         this.headEdge = node.headEdge;
      }
   }

   protected LWNode(LWDependencyGraph _graph, int _index) throws LWGraphException {
      if (_graph == null) {
         throw new LWGraphException("The graph node must belong to a dependency graph.");
      } else if (_index < 0) {
         throw new LWGraphException("Not allowed to have negative node index");
      } else {
         this.graph = _graph;
         this.index = _index;
         this.labels = new HashMap();
         this.headEdge = null;
      }
   }

   protected DependencyStructure getGraph() {
      return this.graph;
   }

   public int getIndex() {
      return this.index;
   }

   public void setIndex(int index) throws MaltChainedException {
      this.index = index;
   }

   public String getLabel(int columnPosition) {
      if (this.labels.containsKey(columnPosition)) {
         return (String)this.labels.get(columnPosition);
      } else {
         return this.graph.getDataFormat().getColumnDescription(columnPosition).getCategory() == 7 ? this.graph.getDataFormat().getColumnDescription(columnPosition).getDefaultOutput() : "";
      }
   }

   public String getLabel(String columnName) {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(columnName);
      return column != null ? this.getLabel(column.getPosition()) : "";
   }

   public String getLabel(ColumnDescription column) {
      return this.getLabel(column.getPosition());
   }

   public boolean hasLabel(int columnPosition) {
      return this.labels.containsKey(columnPosition);
   }

   public boolean hasLabel(String columnName) {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(columnName);
      return column != null ? this.hasLabel(column.getPosition()) : false;
   }

   public boolean hasLabel(ColumnDescription column) {
      return this.labels.containsKey(column.getPosition());
   }

   public boolean isLabeled() {
      Iterator i$ = this.labels.keySet().iterator();

      Integer key;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         key = (Integer)i$.next();
      } while(this.graph.getDataFormat().getColumnDescription(key).getCategory() != 1);

      return true;
   }

   public boolean isHeadLabeled() {
      return this.headEdge == null ? false : this.headEdge.isLabeled();
   }

   public int getHeadIndex() {
      return this.headEdge == null ? -1 : this.headEdge.getSource().getIndex();
   }

   public SortedMap<ColumnDescription, String> getLabels() {
      SortedMap<ColumnDescription, String> nodeLabels = Collections.synchronizedSortedMap(new TreeMap());
      Iterator i$ = this.labels.keySet().iterator();

      while(i$.hasNext()) {
         Integer key = (Integer)i$.next();
         nodeLabels.put(this.graph.getDataFormat().getColumnDescription(key), this.labels.get(key));
      }

      return nodeLabels;
   }

   public DependencyNode getPredecessor() {
      return this.index > 1 ? this.graph.getNode(this.index - 1) : null;
   }

   public DependencyNode getSuccessor() {
      return this.graph.getNode(this.index + 1);
   }

   public boolean isRoot() {
      return this.index == 0;
   }

   public boolean hasAtMostOneHead() {
      return true;
   }

   public boolean hasHead() {
      return this.headEdge != null;
   }

   public boolean hasDependent() {
      return this.graph.hasDependent(this.index);
   }

   public boolean hasLeftDependent() {
      return this.graph.hasLeftDependent(this.index);
   }

   public boolean hasRightDependent() {
      return this.graph.hasRightDependent(this.index);
   }

   public SortedSet<DependencyNode> getHeads() {
      SortedSet<DependencyNode> heads = Collections.synchronizedSortedSet(new TreeSet());
      DependencyNode head = this.getHead();
      if (head != null) {
         heads.add(head);
      }

      return heads;
   }

   public DependencyNode getHead() {
      return this.headEdge == null ? null : this.graph.getNode(this.getHeadIndex());
   }

   public DependencyNode getLeftDependent(int leftDependentIndex) {
      List<DependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
      return leftDependentIndex >= 0 && leftDependentIndex < leftDependents.size() ? (DependencyNode)leftDependents.get(leftDependentIndex) : null;
   }

   public int getLeftDependentCount() {
      return this.graph.getListOfLeftDependents(this.index).size();
   }

   public SortedSet<DependencyNode> getLeftDependents() {
      return this.graph.getSortedSetOfLeftDependents(this.index);
   }

   public List<DependencyNode> getListOfLeftDependents() {
      return this.graph.getListOfLeftDependents(this.index);
   }

   public DependencyNode getLeftSibling() {
      if (this.headEdge == null) {
         return null;
      } else {
         int nodeDepedentPosition = 0;
         List<DependencyNode> headDependents = this.getHead().getListOfDependents();

         for(int i = 0; i < headDependents.size(); ++i) {
            if (((DependencyNode)headDependents.get(i)).getIndex() == this.index) {
               nodeDepedentPosition = i;
               break;
            }
         }

         return nodeDepedentPosition > 0 ? (DependencyNode)headDependents.get(nodeDepedentPosition - 1) : null;
      }
   }

   public DependencyNode getSameSideLeftSibling() {
      if (this.headEdge == null) {
         return null;
      } else {
         List headDependents;
         if (this.index < this.getHeadIndex()) {
            headDependents = this.getHead().getListOfLeftDependents();
         } else {
            headDependents = this.getHead().getListOfRightDependents();
         }

         int nodeDepedentPosition = 0;

         for(int i = 0; i < headDependents.size(); ++i) {
            if (((DependencyNode)headDependents.get(i)).getIndex() == this.index) {
               nodeDepedentPosition = i;
               break;
            }
         }

         return nodeDepedentPosition > 0 ? (DependencyNode)headDependents.get(nodeDepedentPosition - 1) : null;
      }
   }

   public DependencyNode getClosestLeftDependent() {
      List<DependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
      return leftDependents.size() > 0 ? (DependencyNode)leftDependents.get(leftDependents.size() - 1) : null;
   }

   public DependencyNode getLeftmostDependent() {
      List<DependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
      return leftDependents.size() > 0 ? (DependencyNode)leftDependents.get(0) : null;
   }

   public DependencyNode getRightDependent(int rightDependentIndex) {
      List<DependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
      return rightDependentIndex >= 0 && rightDependentIndex < rightDependents.size() ? (DependencyNode)rightDependents.get(rightDependents.size() - 1 - rightDependentIndex) : null;
   }

   public int getRightDependentCount() {
      return this.graph.getListOfRightDependents(this.index).size();
   }

   public SortedSet<DependencyNode> getRightDependents() {
      return this.graph.getSortedSetOfRightDependents(this.index);
   }

   public List<DependencyNode> getListOfRightDependents() {
      return this.graph.getListOfRightDependents(this.index);
   }

   public DependencyNode getRightSibling() {
      if (this.headEdge == null) {
         return null;
      } else {
         List<DependencyNode> headDependents = this.getHead().getListOfDependents();
         int nodeDepedentPosition = headDependents.size() - 1;

         for(int i = headDependents.size() - 1; i >= 0; --i) {
            if (((DependencyNode)headDependents.get(i)).getIndex() == this.index) {
               nodeDepedentPosition = i;
               break;
            }
         }

         return nodeDepedentPosition < headDependents.size() - 1 ? (DependencyNode)headDependents.get(nodeDepedentPosition + 1) : null;
      }
   }

   public DependencyNode getSameSideRightSibling() {
      if (this.headEdge == null) {
         return null;
      } else {
         List headDependents;
         if (this.index < this.getHeadIndex()) {
            headDependents = this.getHead().getListOfLeftDependents();
         } else {
            headDependents = this.getHead().getListOfRightDependents();
         }

         int nodeDepedentPosition = headDependents.size() - 1;

         for(int i = headDependents.size() - 1; i >= 0; --i) {
            if (((DependencyNode)headDependents.get(i)).getIndex() == this.index) {
               nodeDepedentPosition = i;
               break;
            }
         }

         return nodeDepedentPosition < headDependents.size() - 1 ? (DependencyNode)headDependents.get(nodeDepedentPosition + 1) : null;
      }
   }

   public DependencyNode getClosestRightDependent() {
      List<DependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
      return rightDependents.size() > 0 ? (DependencyNode)rightDependents.get(0) : null;
   }

   public DependencyNode getRightmostDependent() {
      List<DependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
      return rightDependents.size() > 0 ? (DependencyNode)rightDependents.get(rightDependents.size() - 1) : null;
   }

   public SortedSet<DependencyNode> getDependents() {
      return this.graph.getSortedSetOfDependents(this.index);
   }

   public List<DependencyNode> getListOfDependents() {
      return this.graph.getListOfDependents(this.index);
   }

   public int getInDegree() {
      return this.hasHead() ? 1 : 0;
   }

   public int getOutDegree() {
      return this.graph.getListOfDependents(this.index).size();
   }

   public DependencyNode getAncestor() throws MaltChainedException {
      if (!this.hasHead()) {
         return this;
      } else {
         Object tmp;
         for(tmp = this; ((DependencyNode)tmp).hasHead(); tmp = ((DependencyNode)tmp).getHead()) {
         }

         return (DependencyNode)tmp;
      }
   }

   public DependencyNode getProperAncestor() throws MaltChainedException {
      if (!this.hasHead()) {
         return null;
      } else {
         Object tmp;
         for(tmp = this; ((DependencyNode)tmp).hasHead() && !((DependencyNode)tmp).isRoot(); tmp = ((DependencyNode)tmp).getHead()) {
         }

         return (DependencyNode)tmp;
      }
   }

   public boolean hasAncestorInside(int left, int right) throws MaltChainedException {
      if (this.index == 0) {
         return false;
      } else {
         if (this.getHead() != null) {
            DependencyNode tmp = this.getHead();
            if (tmp.getIndex() >= left && tmp.getIndex() <= right) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isProjective() throws MaltChainedException {
      int headIndex = this.getHeadIndex();
      if (headIndex > 0) {
         DependencyNode head = this.getHead();
         DependencyNode tmp;
         if (headIndex < this.index) {
            DependencyNode terminals = head;
            tmp = null;

            while(true) {
               if (terminals == null || terminals.getSuccessor() == null) {
                  return false;
               }

               if (terminals.getSuccessor() == this) {
                  break;
               }

               for(tmp = terminals = terminals.getSuccessor(); tmp != this && tmp != head; tmp = tmp.getHead()) {
                  if (!tmp.hasHead()) {
                     return false;
                  }
               }
            }
         } else {
            DependencyNode terminals = this;
            tmp = null;

            while(true) {
               if (terminals == null || ((DependencyNode)terminals).getSuccessor() == null) {
                  return false;
               }

               if (((DependencyNode)terminals).getSuccessor() == head) {
                  break;
               }

               for(Object tmp = terminals = ((DependencyNode)terminals).getSuccessor(); tmp != this && tmp != head; tmp = ((DependencyNode)tmp).getHead()) {
                  if (!((DependencyNode)tmp).hasHead()) {
                     return false;
                  }
               }
            }
         }
      }

      return true;
   }

   public int getDependencyNodeDepth() throws MaltChainedException {
      DependencyNode tmp = this;

      int depth;
      for(depth = 0; ((DependencyNode)tmp).hasHead(); tmp = ((DependencyNode)tmp).getHead()) {
         ++depth;
      }

      return depth;
   }

   public int getCompareToIndex() {
      return this.index;
   }

   public ComparableNode getLeftmostProperDescendant() throws MaltChainedException {
      ComparableNode candidate = null;
      List<DependencyNode> dependents = this.graph.getListOfDependents(this.index);

      for(int i = 0; i < dependents.size(); ++i) {
         DependencyNode dep = (DependencyNode)dependents.get(i);
         if (candidate == null || dep.getIndex() < ((ComparableNode)candidate).getIndex()) {
            candidate = dep;
         }

         ComparableNode tmp = dep.getLeftmostProperDescendant();
         if (tmp != null) {
            if (candidate == null || tmp.getIndex() < ((ComparableNode)candidate).getIndex()) {
               candidate = tmp;
            }

            if (((ComparableNode)candidate).getIndex() == 1) {
               return (ComparableNode)candidate;
            }
         }
      }

      return (ComparableNode)candidate;
   }

   public ComparableNode getRightmostProperDescendant() throws MaltChainedException {
      ComparableNode candidate = null;
      List<DependencyNode> dependents = this.graph.getListOfDependents(this.index);

      for(int i = 0; i < dependents.size(); ++i) {
         DependencyNode dep = (DependencyNode)dependents.get(i);
         if (candidate == null || dep.getIndex() > ((ComparableNode)candidate).getIndex()) {
            candidate = dep;
         }

         ComparableNode tmp = dep.getRightmostProperDescendant();
         if (tmp != null && (candidate == null || tmp.getIndex() > ((ComparableNode)candidate).getIndex())) {
            candidate = tmp;
         }
      }

      return (ComparableNode)candidate;
   }

   public int getLeftmostProperDescendantIndex() throws MaltChainedException {
      ComparableNode node = this.getLeftmostProperDescendant();
      return node != null ? node.getIndex() : -1;
   }

   public int getRightmostProperDescendantIndex() throws MaltChainedException {
      ComparableNode node = this.getRightmostProperDescendant();
      return node != null ? node.getIndex() : -1;
   }

   public ComparableNode getLeftmostDescendant() throws MaltChainedException {
      ComparableNode candidate = this;
      List<DependencyNode> dependents = this.graph.getListOfDependents(this.index);

      for(int i = 0; i < dependents.size(); ++i) {
         DependencyNode dep = (DependencyNode)dependents.get(i);
         if (dep.getIndex() < ((ComparableNode)candidate).getIndex()) {
            candidate = dep;
         }

         ComparableNode tmp = dep.getLeftmostDescendant();
         if (tmp != null) {
            if (tmp.getIndex() < ((ComparableNode)candidate).getIndex()) {
               candidate = tmp;
            }

            if (((ComparableNode)candidate).getIndex() == 1) {
               return (ComparableNode)candidate;
            }
         }
      }

      return (ComparableNode)candidate;
   }

   public ComparableNode getRightmostDescendant() throws MaltChainedException {
      ComparableNode candidate = this;
      List<DependencyNode> dependents = this.graph.getListOfDependents(this.index);

      for(int i = 0; i < dependents.size(); ++i) {
         DependencyNode dep = (DependencyNode)dependents.get(i);
         if (dep.getIndex() > ((ComparableNode)candidate).getIndex()) {
            candidate = dep;
         }

         ComparableNode tmp = dep.getRightmostDescendant();
         if (tmp != null && tmp.getIndex() > ((ComparableNode)candidate).getIndex()) {
            candidate = tmp;
         }
      }

      return (ComparableNode)candidate;
   }

   public int getLeftmostDescendantIndex() throws MaltChainedException {
      ComparableNode node = this.getLeftmostDescendant();
      return node != null ? node.getIndex() : this.getIndex();
   }

   public int getRightmostDescendantIndex() throws MaltChainedException {
      ComparableNode node = this.getRightmostDescendant();
      return node != null ? node.getIndex() : this.getIndex();
   }

   public SortedSet<Edge> getIncomingSecondaryEdges() throws MaltChainedException {
      throw new LWGraphException("Not implemented in the light-weight dependency graph package");
   }

   public SortedSet<Edge> getOutgoingSecondaryEdges() throws MaltChainedException {
      throw new LWGraphException("Not implemented in the light-weight dependency graph package");
   }

   public Set<Edge> getHeadEdges() {
      SortedSet<Edge> edges = Collections.synchronizedSortedSet(new TreeSet());
      if (this.hasHead()) {
         edges.add(this.headEdge);
      }

      return edges;
   }

   public Edge getHeadEdge() {
      return !this.hasHead() ? null : this.headEdge;
   }

   public void addHeadEdgeLabel(SymbolTable table, String symbol) throws MaltChainedException {
      if (this.headEdge != null) {
         this.headEdge.addLabel(table, symbol);
      }

   }

   public void addHeadEdgeLabel(SymbolTable table, int code) throws MaltChainedException {
      if (this.headEdge != null) {
         this.headEdge.addLabel(table, code);
      }

   }

   public void addHeadEdgeLabel(LabelSet labelSet) throws MaltChainedException {
      if (this.headEdge != null) {
         this.headEdge.addLabel(labelSet);
      }

   }

   public boolean hasHeadEdgeLabel(SymbolTable table) throws MaltChainedException {
      return this.headEdge != null ? this.headEdge.hasLabel(table) : false;
   }

   public String getHeadEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
      return this.headEdge != null ? this.headEdge.getLabelSymbol(table) : null;
   }

   public int getHeadEdgeLabelCode(SymbolTable table) throws MaltChainedException {
      return this.headEdge != null ? this.headEdge.getLabelCode(table) : 0;
   }

   public Set<SymbolTable> getHeadEdgeLabelTypes() throws MaltChainedException {
      return (Set)(this.headEdge != null ? this.headEdge.getLabelTypes() : new HashSet());
   }

   public LabelSet getHeadEdgeLabelSet() throws MaltChainedException {
      return this.headEdge != null ? this.headEdge.getLabelSet() : new LabelSet();
   }

   public void addIncomingEdge(Edge in) throws MaltChainedException {
      this.headEdge = in;
   }

   public void addOutgoingEdge(Edge out) throws MaltChainedException {
      throw new LWGraphException("Not implemented in the light-weight dependency graph package");
   }

   public void removeIncomingEdge(Edge in) throws MaltChainedException {
      if (this.headEdge.equals(in)) {
         this.headEdge = null;
      }

   }

   public void removeOutgoingEdge(Edge out) throws MaltChainedException {
      throw new LWGraphException("Not implemented in the light-weight dependency graph package");
   }

   public Iterator<Edge> getIncomingEdgeIterator() {
      return this.getHeadEdges().iterator();
   }

   public Iterator<Edge> getOutgoingEdgeIterator() {
      List<DependencyNode> dependents = this.getListOfDependents();
      List<Edge> outEdges = new ArrayList(dependents.size());

      for(int i = 0; i < dependents.size(); ++i) {
         try {
            outEdges.add(((DependencyNode)dependents.get(i)).getHeadEdge());
         } catch (MaltChainedException var5) {
            var5.printStackTrace();
         }
      }

      return outEdges.iterator();
   }

   public void setRank(int r) {
   }

   public DependencyNode getComponent() {
      return null;
   }

   public void setComponent(DependencyNode x) {
   }

   public DependencyNode findComponent() {
      return this.graph.findComponent(this.index);
   }

   public int getRank() {
      return this.graph.getRank(this.index);
   }

   public boolean isHeadEdgeLabeled() {
      return this.headEdge != null ? this.headEdge.isLabeled() : false;
   }

   public int nHeadEdgeLabels() {
      return this.headEdge != null ? this.headEdge.nLabels() : 0;
   }

   public void addColumnLabels(String[] columnLabels) throws MaltChainedException {
      this.addColumnLabels(columnLabels, true);
   }

   public void addColumnLabels(String[] columnLabels, boolean addEdges) throws MaltChainedException {
      if (addEdges) {
         SortedMap<ColumnDescription, String> edgeLabels = new TreeMap();
         int tmpHeadIndex = -1;
         if (columnLabels != null) {
            for(int i = 0; i < columnLabels.length; ++i) {
               ColumnDescription column = this.graph.getDataFormat().getColumnDescription(i);
               if (column.getCategory() == 2) {
                  tmpHeadIndex = Integer.parseInt(columnLabels[i]);
               } else if (column.getCategory() == 1) {
                  this.addLabel(this.graph.getSymbolTables().addSymbolTable(column.getName()), columnLabels[i]);
               } else if (column.getCategory() == 3) {
                  edgeLabels.put(column, columnLabels[i]);
               }
            }
         }

         if (tmpHeadIndex == -1) {
            this.headEdge = null;
         } else {
            if (tmpHeadIndex < -1) {
               throw new LWGraphException("Not allowed to have head index less than -1.");
            }

            if (this.index == 0 && tmpHeadIndex != -1) {
               throw new LWGraphException("Not allowed to add head to a root node.");
            }

            if (this.index == tmpHeadIndex) {
               throw new LWGraphException("Not allowed to add head to itself");
            }

            this.headEdge = new LWEdge(this.graph.getNode(tmpHeadIndex), this, edgeLabels);
         }
      } else {
         if (columnLabels != null) {
            for(int i = 0; i < columnLabels.length; ++i) {
               ColumnDescription column = this.graph.getDataFormat().getColumnDescription(i);
               if (column.getCategory() == 1) {
                  this.addLabel(this.graph.getSymbolTables().addSymbolTable(column.getName()), columnLabels[i]);
               }
            }
         }

         this.headEdge = null;
      }

   }

   public void addLabel(SymbolTable table, String symbol) throws MaltChainedException {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
      table.addSymbol(symbol);
      this.labels.put(column.getPosition(), symbol);
   }

   public void addLabel(SymbolTable table, int code) throws MaltChainedException {
      this.addLabel(table, table.getSymbolCodeToString(code));
   }

   public void addLabel(LabelSet labels) throws MaltChainedException {
      Iterator i$ = labels.keySet().iterator();

      while(i$.hasNext()) {
         SymbolTable table = (SymbolTable)i$.next();
         this.addLabel(table, (Integer)labels.get(table));
      }

   }

   public boolean hasLabel(SymbolTable table) throws MaltChainedException {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
      return this.labels.containsKey(column.getPosition());
   }

   public String getLabelSymbol(SymbolTable table) throws MaltChainedException {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
      return (String)this.labels.get(column.getPosition());
   }

   public int getLabelCode(SymbolTable table) throws MaltChainedException {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
      return table.getSymbolStringToCode((String)this.labels.get(column.getPosition()));
   }

   public int nLabels() {
      return this.labels.size();
   }

   public Set<SymbolTable> getLabelTypes() {
      Set<SymbolTable> labelTypes = new HashSet();
      SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
      SortedSet<ColumnDescription> selectedColumns = this.graph.getDataFormat().getSelectedColumnDescriptions(this.labels.keySet());
      Iterator i$ = selectedColumns.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();

         try {
            labelTypes.add(symbolTableHandler.getSymbolTable(column.getName()));
         } catch (MaltChainedException var7) {
            var7.printStackTrace();
         }
      }

      return labelTypes;
   }

   public LabelSet getLabelSet() {
      SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
      LabelSet labelSet = new LabelSet();
      SortedSet<ColumnDescription> selectedColumns = this.graph.getDataFormat().getSelectedColumnDescriptions(this.labels.keySet());
      Iterator i$ = selectedColumns.iterator();

      while(i$.hasNext()) {
         ColumnDescription column = (ColumnDescription)i$.next();

         try {
            SymbolTable table = symbolTableHandler.getSymbolTable(column.getName());
            int code = table.getSymbolStringToCode((String)this.labels.get(column.getPosition()));
            labelSet.put(table, code);
         } catch (MaltChainedException var8) {
            var8.printStackTrace();
         }
      }

      return labelSet;
   }

   public void removeLabel(SymbolTable table) throws MaltChainedException {
      ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
      this.labels.remove(column.getPosition());
   }

   public void removeLabels() throws MaltChainedException {
      this.labels.clear();
   }

   public LabeledStructure getBelongsToGraph() {
      return this.graph;
   }

   public void setBelongsToGraph(LabeledStructure belongsToGraph) {
   }

   public void clear() throws MaltChainedException {
      this.labels.clear();
   }

   public int compareTo(ComparableNode that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == that) {
         return 0;
      } else if (this.index < that.getIndex()) {
         return -1;
      } else {
         return this.index > that.getIndex() ? 1 : 0;
      }
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.headEdge == null ? 0 : this.headEdge.hashCode());
      result = 31 * result + this.index;
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
         LWNode other = (LWNode)obj;
         if (this.headEdge == null) {
            if (other.headEdge != null) {
               return false;
            }
         } else if (!this.headEdge.equals(other.headEdge)) {
            return false;
         }

         if (this.index != other.index) {
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

      for(int i = 0; i < this.graph.getDataFormat().numberOfColumns(); ++i) {
         ColumnDescription column = this.graph.getDataFormat().getColumnDescription(i);
         if (!column.isInternal()) {
            if (column.getCategory() == 2) {
               sb.append(this.getHeadIndex());
            } else if (column.getCategory() == 1) {
               sb.append((String)this.labels.get(column.getPosition()));
            } else if (column.getCategory() == 3) {
               if (this.headEdge != null) {
                  sb.append(((LWEdge)this.headEdge).getLabel(column));
               } else {
                  sb.append(column.getDefaultOutput());
               }
            } else if (column.getCategory() == 7) {
               sb.append(column.getDefaultOutput());
            }

            sb.append('\t');
         }
      }

      sb.setLength(sb.length() > 0 ? sb.length() - 1 : 0);
      return sb.toString();
   }
}
