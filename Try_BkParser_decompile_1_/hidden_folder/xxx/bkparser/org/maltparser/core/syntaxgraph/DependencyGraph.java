package org.maltparser.core.syntaxgraph;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.edge.GraphEdge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.Root;
import org.maltparser.core.syntaxgraph.node.Token;

public class DependencyGraph extends Sentence implements DependencyStructure {
   private final ObjectPoolList<Edge> edgePool;
   private final SortedSet<Edge> graphEdges;
   private final Root root;
   private boolean singleHeadedConstraint;
   private RootLabels rootLabels;

   public DependencyGraph(SymbolTableHandler symbolTables) throws MaltChainedException {
      super(symbolTables);
      this.setSingleHeadedConstraint(true);
      this.root = new Root();
      this.root.setBelongsToGraph(this);
      this.graphEdges = new TreeSet();
      this.edgePool = new ObjectPoolList<Edge>() {
         protected Edge create() {
            return new GraphEdge();
         }

         public void resetObject(Edge o) throws MaltChainedException {
            o.clear();
         }
      };
      this.clear();
   }

   public DependencyNode addDependencyNode() throws MaltChainedException {
      return this.addTokenNode();
   }

   public DependencyNode addDependencyNode(int index) throws MaltChainedException {
      return (DependencyNode)(index == 0 ? this.root : this.addTokenNode(index));
   }

   public DependencyNode getDependencyNode(int index) throws MaltChainedException {
      return (DependencyNode)(index == 0 ? this.root : this.getTokenNode(index));
   }

   public int nDependencyNode() {
      return this.nTokenNode() + 1;
   }

   public int getHighestDependencyNodeIndex() {
      return this.hasTokens() ? this.getHighestTokenIndex() : 0;
   }

   public Edge addDependencyEdge(int headIndex, int dependentIndex) throws MaltChainedException {
      DependencyNode head = null;
      DependencyNode dependent = null;
      if (headIndex == 0) {
         head = this.root;
      } else {
         head = this.getOrAddTerminalNode(headIndex);
      }

      if (dependentIndex > 0) {
         dependent = this.getOrAddTerminalNode(dependentIndex);
      }

      return this.addDependencyEdge((DependencyNode)head, dependent);
   }

   protected Edge addDependencyEdge(DependencyNode head, DependencyNode dependent) throws MaltChainedException {
      if (head != null && dependent != null) {
         if (!dependent.isRoot()) {
            if (this.singleHeadedConstraint && dependent.hasHead()) {
               return this.moveDependencyEdge(head, dependent);
            } else {
               DependencyNode hc = head.findComponent();
               DependencyNode dc = dependent.findComponent();
               if (hc != dc) {
                  this.link(hc, dc);
                  --this.numberOfComponents;
               }

               Edge e = (Edge)this.edgePool.checkOut();
               e.setBelongsToGraph(this);
               e.setEdge((Node)head, (Node)dependent, 1);
               this.graphEdges.add(e);
               return e;
            }
         } else {
            throw new SyntaxGraphException("Head node is not a root node or a terminal node.");
         }
      } else {
         throw new SyntaxGraphException("Head or dependent node is missing.");
      }
   }

   public Edge moveDependencyEdge(int newHeadIndex, int dependentIndex) throws MaltChainedException {
      DependencyNode newHead = null;
      DependencyNode dependent = null;
      if (newHeadIndex == 0) {
         newHead = this.root;
      } else if (newHeadIndex > 0) {
         newHead = (DependencyNode)this.terminalNodes.get(newHeadIndex);
      }

      if (dependentIndex > 0) {
         dependent = (DependencyNode)this.terminalNodes.get(dependentIndex);
      }

      return this.moveDependencyEdge((DependencyNode)newHead, dependent);
   }

   protected Edge moveDependencyEdge(DependencyNode newHead, DependencyNode dependent) throws MaltChainedException {
      if (dependent != null && dependent.hasHead()) {
         Edge headEdge = dependent.getHeadEdge();
         LabelSet labels = this.checkOutNewLabelSet();
         Iterator i$ = headEdge.getLabelTypes().iterator();

         while(i$.hasNext()) {
            SymbolTable table = (SymbolTable)i$.next();
            labels.put(table, headEdge.getLabelCode(table));
         }

         headEdge.clear();
         headEdge.setBelongsToGraph(this);
         headEdge.setEdge((Node)newHead, (Node)dependent, 1);
         headEdge.addLabel(labels);
         labels.clear();
         this.checkInLabelSet(labels);
         return headEdge;
      } else {
         return null;
      }
   }

   public void removeDependencyEdge(int headIndex, int dependentIndex) throws MaltChainedException {
      Node head = null;
      Node dependent = null;
      if (headIndex == 0) {
         head = this.root;
      } else if (headIndex > 0) {
         head = (Node)this.terminalNodes.get(headIndex);
      }

      if (dependentIndex > 0) {
         dependent = (Node)this.terminalNodes.get(dependentIndex);
      }

      this.removeDependencyEdge((Node)head, dependent);
   }

   protected void removeDependencyEdge(Node head, Node dependent) throws MaltChainedException {
      if (head != null && dependent != null) {
         if (!dependent.isRoot()) {
            Iterator ie = dependent.getIncomingEdgeIterator();

            while(ie.hasNext()) {
               Edge e = (Edge)ie.next();
               if (e.getSource() == head) {
                  this.graphEdges.remove(e);
                  ie.remove();
                  this.edgePool.checkIn(e);
               }
            }

         } else {
            throw new SyntaxGraphException("Head node is not a root node or a terminal node.");
         }
      } else {
         throw new SyntaxGraphException("Head or dependent node is missing.");
      }
   }

   public Edge addSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
      if (source != null && target != null) {
         if (!target.isRoot()) {
            Edge e = (Edge)this.edgePool.checkOut();
            e.setBelongsToGraph(this);
            e.setEdge((Node)source, (Node)target, 3);
            this.graphEdges.add(e);
            return e;
         } else {
            return null;
         }
      } else {
         throw new SyntaxGraphException("Head or dependent node is missing.");
      }
   }

   public void removeSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
      if (source != null && target != null) {
         if (!target.isRoot()) {
            Iterator ie = ((Node)target).getIncomingEdgeIterator();

            while(ie.hasNext()) {
               Edge e = (Edge)ie.next();
               if (e.getSource() == source) {
                  ie.remove();
                  this.graphEdges.remove(e);
                  this.edgePool.checkIn(e);
               }
            }
         }

      } else {
         throw new SyntaxGraphException("Head or dependent node is missing.");
      }
   }

   public boolean hasLabeledDependency(int index) throws MaltChainedException {
      return this.getDependencyNode(index).hasHead() && this.getDependencyNode(index).getHeadEdge().isLabeled();
   }

   public boolean isConnected() {
      return this.numberOfComponents == 1;
   }

   public boolean isProjective() throws MaltChainedException {
      Iterator i$ = this.terminalNodes.keySet().iterator();

      int i;
      do {
         if (!i$.hasNext()) {
            return true;
         }

         i = (Integer)i$.next();
      } while(((Token)this.terminalNodes.get(i)).isProjective());

      return false;
   }

   public boolean isTree() {
      return this.isConnected() && this.isSingleHeaded();
   }

   public boolean isSingleHeaded() {
      Iterator i$ = this.terminalNodes.keySet().iterator();

      int i;
      do {
         if (!i$.hasNext()) {
            return true;
         }

         i = (Integer)i$.next();
      } while(((Token)this.terminalNodes.get(i)).hasAtMostOneHead());

      return false;
   }

   public boolean isSingleHeadedConstraint() {
      return this.singleHeadedConstraint;
   }

   public void setSingleHeadedConstraint(boolean singleHeadedConstraint) {
      this.singleHeadedConstraint = singleHeadedConstraint;
   }

   public int nNonProjectiveEdges() throws MaltChainedException {
      int c = 0;
      Iterator i$ = this.terminalNodes.keySet().iterator();

      while(i$.hasNext()) {
         int i = (Integer)i$.next();
         if (!((Token)this.terminalNodes.get(i)).isProjective()) {
            ++c;
         }
      }

      return c;
   }

   public int nEdges() {
      return this.graphEdges.size();
   }

   public SortedSet<Edge> getEdges() {
      return this.graphEdges;
   }

   public SortedSet<Integer> getDependencyIndices() {
      SortedSet<Integer> indices = new TreeSet(this.terminalNodes.keySet());
      indices.add(0);
      return indices;
   }

   protected DependencyNode link(DependencyNode x, DependencyNode y) throws MaltChainedException {
      if (x.getRank() > y.getRank()) {
         y.setComponent(x);
         return x;
      } else {
         x.setComponent(y);
         if (x.getRank() == y.getRank()) {
            y.setRank(y.getRank() + 1);
         }

         return y;
      }
   }

   public void linkAllTreesToRoot() throws MaltChainedException {
      Iterator i$ = this.terminalNodes.keySet().iterator();

      while(i$.hasNext()) {
         int i = (Integer)i$.next();
         if (!((Token)this.terminalNodes.get(i)).hasHead()) {
            this.addDependencyEdge(this.root, (DependencyNode)this.terminalNodes.get(i));
         }
      }

   }

   public LabelSet getDefaultRootEdgeLabels() throws MaltChainedException {
      return this.rootLabels == null ? null : this.rootLabels.getDefaultRootLabels();
   }

   public String getDefaultRootEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
      return this.rootLabels == null ? null : this.rootLabels.getDefaultRootLabelSymbol(table);
   }

   public int getDefaultRootEdgeLabelCode(SymbolTable table) throws MaltChainedException {
      return this.rootLabels == null ? -1 : this.rootLabels.getDefaultRootLabelCode(table);
   }

   public void setDefaultRootEdgeLabel(SymbolTable table, String defaultRootSymbol) throws MaltChainedException {
      if (this.rootLabels == null) {
         this.rootLabels = new RootLabels();
      }

      this.rootLabels.setDefaultRootLabel(table, defaultRootSymbol);
   }

   public void setDefaultRootEdgeLabels(String rootLabelOption, SortedMap<String, SymbolTable> edgeSymbolTables) throws MaltChainedException {
      if (this.rootLabels == null) {
         this.rootLabels = new RootLabels();
      }

      this.rootLabels.setRootLabels(rootLabelOption, edgeSymbolTables);
   }

   public void clear() throws MaltChainedException {
      this.edgePool.checkInAll();
      this.graphEdges.clear();
      this.root.clear();
      super.clear();
      ++this.numberOfComponents;
   }

   public DependencyNode getDependencyRoot() {
      return this.root;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.terminalNodes.keySet().iterator();

      while(i$.hasNext()) {
         int index = (Integer)i$.next();
         sb.append(((Token)this.terminalNodes.get(index)).toString().trim());
         sb.append('\n');
      }

      sb.append('\n');
      return sb.toString();
   }
}
