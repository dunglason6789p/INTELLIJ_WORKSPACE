package org.maltparser.core.syntaxgraph;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.edge.GraphEdge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.NonTerminal;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.Root;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class PhraseStructureGraph extends Sentence implements PhraseStructure {
   protected final ObjectPoolList<Edge> edgePool;
   protected final SortedSet<Edge> graphEdges;
   protected final SortedMap<Integer, NonTerminal> nonTerminalNodes;
   protected final ObjectPoolList<NonTerminal> nonTerminalPool;
   protected final Root root = new Root();

   public PhraseStructureGraph(SymbolTableHandler symbolTables) throws MaltChainedException {
      super(symbolTables);
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
         if (parent instanceof NonTerminalNode && !child.isRoot()) {
            Edge e = (Edge)this.edgePool.checkOut();
            e.setBelongsToGraph(this);
            e.setEdge((Node)parent, (Node)child, 2);
            this.graphEdges.add(e);
            return e;
         } else {
            throw new MaltChainedException("Parent or child node is not of correct node type.");
         }
      } else {
         throw new MaltChainedException("Parent or child node is missing.");
      }
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

   public int nEdges() {
      return this.graphEdges.size();
   }

   public SortedSet<Edge> getEdges() {
      return this.graphEdges;
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

   public void clear() throws MaltChainedException {
      this.edgePool.checkInAll();
      this.graphEdges.clear();
      this.root.clear();
      this.root.setBelongsToGraph(this);
      this.nonTerminalPool.checkInAll();
      this.nonTerminalNodes.clear();
      super.clear();
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
