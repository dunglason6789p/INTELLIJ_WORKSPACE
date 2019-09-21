package org.maltparser.core.syntaxgraph;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.ds2ps.LosslessMapping;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.edge.GraphEdge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.NonTerminal;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.Root;
import org.maltparser.core.syntaxgraph.node.Token;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class MappablePhraseStructureGraph extends Sentence implements DependencyStructure, PhraseStructure {
   private final ObjectPoolList<Edge> edgePool;
   private final SortedSet<Edge> graphEdges;
   private Root root;
   private boolean singleHeadedConstraint;
   private final SortedMap<Integer, NonTerminal> nonTerminalNodes;
   private final ObjectPoolList<NonTerminal> nonTerminalPool;
   private LosslessMapping mapping;
   private RootLabels rootLabels;

   public MappablePhraseStructureGraph(SymbolTableHandler symbolTables) throws MaltChainedException {
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
      this.nonTerminalNodes = new TreeMap();
      this.nonTerminalPool = new ObjectPoolList<NonTerminal>() {
         protected NonTerminal create() throws MaltChainedException {
            return new NonTerminal();
         }

         public void resetObject(NonTerminal o) throws MaltChainedException {
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
      } else if (headIndex > 0) {
         head = this.getOrAddTerminalNode(headIndex);
      }

      if (dependentIndex > 0) {
         dependent = this.getOrAddTerminalNode(dependentIndex);
      }

      return this.addDependencyEdge((DependencyNode)head, dependent);
   }

   public Edge addDependencyEdge(DependencyNode head, DependencyNode dependent) throws MaltChainedException {
      if (head != null && dependent != null && head.getBelongsToGraph() == this && dependent.getBelongsToGraph() == this) {
         if (!dependent.isRoot()) {
            if (this.singleHeadedConstraint && dependent.hasHead()) {
               throw new SyntaxGraphException("The dependent already have a head. ");
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

   public Edge moveDependencyEdge(DependencyNode newHead, DependencyNode dependent) throws MaltChainedException {
      if (dependent != null && dependent.hasHead() && newHead.getBelongsToGraph() == this && dependent.getBelongsToGraph() == this) {
         Edge headEdge = dependent.getHeadEdge();
         LabelSet labels = null;
         if (headEdge.isLabeled()) {
            labels = this.checkOutNewLabelSet();
            Iterator i$ = headEdge.getLabelTypes().iterator();

            while(i$.hasNext()) {
               SymbolTable table = (SymbolTable)i$.next();
               labels.put(table, headEdge.getLabelCode(table));
            }
         }

         headEdge.clear();
         headEdge.setBelongsToGraph(this);
         headEdge.setEdge((Node)newHead, (Node)dependent, 1);
         if (labels != null) {
            headEdge.addLabel(labels);
            labels.clear();
            this.checkInLabelSet(labels);
         }

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
      if (head != null && dependent != null && head.getBelongsToGraph() == this && dependent.getBelongsToGraph() == this) {
         if (!dependent.isRoot()) {
            Iterator ie = dependent.getIncomingEdgeIterator();

            while(ie.hasNext()) {
               Edge e = (Edge)ie.next();
               if (e.getSource() == head) {
                  ie.remove();
                  this.graphEdges.remove(e);
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
      if (source != null && target != null && source.getBelongsToGraph() == this && target.getBelongsToGraph() == this) {
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
      if (source != null && target != null && source.getBelongsToGraph() == this && target.getBelongsToGraph() == this) {
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

   public void linkAllTerminalsToRoot() throws MaltChainedException {
      this.clear();
      Iterator i$ = this.terminalNodes.keySet().iterator();

      while(i$.hasNext()) {
         int i = (Integer)i$.next();
         DependencyNode node = (DependencyNode)this.terminalNodes.get(i);
         this.addDependencyEdge(this.root, node);
      }

   }

   public void linkAllTreesToRoot() throws MaltChainedException {
      Iterator i$ = this.terminalNodes.keySet().iterator();

      while(i$.hasNext()) {
         int i = (Integer)i$.next();
         if (!((Token)this.terminalNodes.get(i)).hasHead()) {
            Edge e = this.addDependencyEdge(this.root, (DependencyNode)this.terminalNodes.get(i));
            this.mapping.updatePhraseStructureGraph(this, e, false);
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
      this.root.setBelongsToGraph(this);
      this.nonTerminalPool.checkInAll();
      this.nonTerminalNodes.clear();
      if (this.mapping != null) {
         this.mapping.clear();
      }

      super.clear();
      ++this.numberOfComponents;
   }

   public DependencyNode getDependencyRoot() {
      return this.root;
   }

   public PhraseStructureNode addTerminalNode() throws MaltChainedException {
      return this.addTokenNode();
   }

   public PhraseStructureNode addTerminalNode(int index) throws MaltChainedException {
      return this.addTokenNode(index);
   }

   public PhraseStructureNode getTerminalNode(int index) {
      return this.getTokenNode(index);
   }

   public int nTerminalNode() {
      return this.nTokenNode();
   }

   public PhraseStructureNode addNonTerminalNode(int index) throws MaltChainedException {
      NonTerminal node = (NonTerminal)this.nonTerminalPool.checkOut();
      node.setIndex(index);
      node.setBelongsToGraph(this);
      this.nonTerminalNodes.put(index, node);
      return node;
   }

   public PhraseStructureNode addNonTerminalNode() throws MaltChainedException {
      int index = this.getHighestNonTerminalIndex();
      return index > 0 ? this.addNonTerminalNode(index + 1) : this.addNonTerminalNode(1);
   }

   public PhraseStructureNode getNonTerminalNode(int index) throws MaltChainedException {
      return (PhraseStructureNode)this.nonTerminalNodes.get(index);
   }

   public int getHighestNonTerminalIndex() {
      try {
         return (Integer)this.nonTerminalNodes.lastKey();
      } catch (NoSuchElementException var2) {
         return 0;
      }
   }

   public Set<Integer> getNonTerminalIndices() {
      return new TreeSet(this.nonTerminalNodes.keySet());
   }

   public boolean hasNonTerminals() {
      return !this.nonTerminalNodes.isEmpty();
   }

   public int nNonTerminals() {
      return this.nonTerminalNodes.size();
   }

   public PhraseStructureNode getPhraseStructureRoot() {
      return this.root;
   }

   public Edge addPhraseStructureEdge(PhraseStructureNode parent, PhraseStructureNode child) throws MaltChainedException {
      if (parent != null && child != null) {
         if (parent.getBelongsToGraph() == this && child.getBelongsToGraph() == this) {
            if (parent == child) {
               throw new MaltChainedException("It is not allowed to add a phrase structure edge connecting the same node in sentence " + this.getSentenceID());
            } else if (parent instanceof NonTerminalNode && !child.isRoot()) {
               Edge e = (Edge)this.edgePool.checkOut();
               e.setBelongsToGraph(this);
               e.setEdge((Node)parent, (Node)child, 2);
               this.graphEdges.add(e);
               return e;
            } else {
               throw new MaltChainedException("Parent or child node is not of correct node type.");
            }
         } else {
            throw new MaltChainedException("Parent or child node is not a member of the graph in sentence " + this.getSentenceID());
         }
      } else {
         throw new MaltChainedException("Parent or child node is missing in sentence " + this.getSentenceID());
      }
   }

   public void update(Observable o, Object arg) {
      if (o instanceof Edge && this.mapping != null) {
         try {
            this.mapping.update(this, (Edge)o, arg);
         } catch (MaltChainedException var4) {
            if (SystemLogger.logger().isDebugEnabled()) {
               SystemLogger.logger().debug("", var4);
            } else {
               SystemLogger.logger().error(var4.getMessageChain());
            }

            System.exit(1);
         }
      }

   }

   public LosslessMapping getMapping() {
      return this.mapping;
   }

   public void setMapping(LosslessMapping mapping) {
      this.mapping = mapping;
   }

   public void addLabel(Element element, String labelFunction, String label) throws MaltChainedException {
      super.addLabel(element, labelFunction, label);
   }

   public void removePhraseStructureEdge(PhraseStructureNode parent, PhraseStructureNode child) throws MaltChainedException {
      if (parent != null && child != null) {
         if (parent instanceof NonTerminalNode && !child.isRoot()) {
            Iterator i$ = this.graphEdges.iterator();

            while(i$.hasNext()) {
               Edge e = (Edge)i$.next();
               if (e.getSource() == parent && e.getTarget() == child) {
                  e.clear();
                  this.graphEdges.remove(e);
                  if (e instanceof GraphEdge) {
                     this.edgePool.checkIn(e);
                  }
               }
            }

         } else {
            throw new SyntaxGraphException("Head node is not a root node or a terminal node.");
         }
      } else {
         throw new MaltChainedException("Parent or child node is missing.");
      }
   }

   public boolean isContinuous() {
      Iterator i$ = this.nonTerminalNodes.keySet().iterator();

      NonTerminalNode node;
      do {
         if (!i$.hasNext()) {
            return true;
         }

         int index = (Integer)i$.next();
         node = (NonTerminalNode)this.nonTerminalNodes.get(index);
      } while(node.isContinuous());

      return false;
   }

   public boolean isContinuousExcludeTerminalsAttachToRoot() {
      Iterator i$ = this.nonTerminalNodes.keySet().iterator();

      NonTerminalNode node;
      do {
         if (!i$.hasNext()) {
            return true;
         }

         int index = (Integer)i$.next();
         node = (NonTerminalNode)this.nonTerminalNodes.get(index);
      } while(node.isContinuousExcludeTerminalsAttachToRoot());

      return false;
   }

   public String toStringTerminalNode(TokenNode node) {
      StringBuilder sb = new StringBuilder();
      DependencyNode depnode = node;
      sb.append(node.toString().trim());
      if (node.hasHead()) {
         sb.append('\t');

         try {
            sb.append(depnode.getHead().getIndex());
            sb.append('\t');
            sb.append(depnode.getHeadEdge().toString());
         } catch (MaltChainedException var5) {
            System.err.println(var5);
         }
      }

      sb.append('\n');
      return sb.toString();
   }

   public String toStringNonTerminalNode(NonTerminalNode node) {
      StringBuilder sb = new StringBuilder();
      sb.append(node.toString().trim());
      sb.append('\n');
      Iterator ie = ((Node)node).getOutgoingEdgeIterator();

      while(ie.hasNext()) {
         Edge e = (Edge)ie.next();
         if (e.getTarget() instanceof TokenNode) {
            sb.append("   T");
            sb.append(e.getTarget().getIndex());
         }

         if (e.getTarget() instanceof NonTerminalNode) {
            sb.append("   N");
            sb.append(e.getTarget().getIndex());
         }

         sb.append('\t');
         sb.append(e.toString());
         sb.append('\n');
      }

      return sb.toString();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      Iterator i$ = this.terminalNodes.keySet().iterator();

      int index;
      while(i$.hasNext()) {
         index = (Integer)i$.next();
         sb.append(this.toStringTerminalNode((TokenNode)this.terminalNodes.get(index)));
      }

      sb.append('\n');
      sb.append(this.toStringNonTerminalNode((NonTerminalNode)this.getPhraseStructureRoot()));
      i$ = this.nonTerminalNodes.keySet().iterator();

      while(i$.hasNext()) {
         index = (Integer)i$.next();
         sb.append(this.toStringNonTerminalNode((NonTerminalNode)this.nonTerminalNodes.get(index)));
      }

      return sb.toString();
   }
}
