package org.maltparser.core.syntaxgraph.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.edge.Edge;

public class Token extends GraphNode implements TokenNode, DependencyNode, PhraseStructureNode {
   protected TokenNode predecessor = null;
   protected TokenNode successor = null;
   protected DependencyNode component;
   protected int rank;
   protected int index;
   protected PhraseStructureNode parent = null;
   protected final SortedSet<DependencyNode> heads = new TreeSet();
   protected final SortedSet<DependencyNode> leftDependents = new TreeSet();
   protected final SortedSet<DependencyNode> rightDependents = new TreeSet();

   public Token() throws MaltChainedException {
      this.clear();
   }

   public void setPredecessor(TokenNode predecessor) {
      this.predecessor = predecessor;
   }

   public void setSuccessor(TokenNode successor) {
      this.successor = successor;
   }

   public TokenNode getTokenNodePredecessor() {
      return this.predecessor;
   }

   public TokenNode getTokenNodeSuccessor() {
      return this.successor;
   }

   public DependencyNode getPredecessor() {
      return this.predecessor;
   }

   public DependencyNode getSuccessor() {
      return this.successor;
   }

   public int getRank() {
      return this.rank;
   }

   public void setRank(int r) {
      this.rank = r;
   }

   public DependencyNode findComponent() {
      return this.findComponent(this);
   }

   private DependencyNode findComponent(DependencyNode x) {
      if (x != x.getComponent()) {
         x.setComponent(this.findComponent(x.getComponent()));
      }

      return x.getComponent();
   }

   public DependencyNode getComponent() {
      return this.component;
   }

   public void setComponent(DependencyNode x) {
      this.component = x;
   }

   public void addIncomingEdge(Edge in) throws MaltChainedException {
      super.addIncomingEdge(in);
      if (in.getSource() != null) {
         if (in.getType() == 1 && in.getSource() instanceof DependencyNode) {
            this.heads.add((DependencyNode)in.getSource());
         } else if (in.getType() == 2 && in.getSource() instanceof PhraseStructureNode) {
            this.parent = (PhraseStructureNode)in.getSource();
         }
      }

   }

   public void removeIncomingEdge(Edge in) throws MaltChainedException {
      super.removeIncomingEdge(in);
      if (in.getSource() != null) {
         if (in.getType() == 1 && in.getSource() instanceof DependencyNode) {
            this.heads.remove((DependencyNode)in.getSource());
         } else if (in.getType() == 2 && in.getSource() instanceof PhraseStructureNode && in.getSource() == this.parent) {
            this.parent = null;
         }
      }

   }

   public void addOutgoingEdge(Edge out) throws MaltChainedException {
      super.addOutgoingEdge(out);
      if (out.getType() == 1 && out.getTarget() instanceof DependencyNode) {
         DependencyNode dependent = (DependencyNode)out.getTarget();
         if (this.compareTo((ComparableNode)dependent) > 0) {
            this.leftDependents.add(dependent);
         } else if (this.compareTo((ComparableNode)dependent) < 0) {
            this.rightDependents.add(dependent);
         }
      }

   }

   public void removeOutgoingEdge(Edge out) throws MaltChainedException {
      super.removeOutgoingEdge(out);
      if (out.getType() == 1 && out.getTarget() instanceof DependencyNode) {
         DependencyNode dependent = (DependencyNode)out.getTarget();
         if (this.compareTo((ComparableNode)dependent) > 0) {
            this.leftDependents.remove(dependent);
         } else if (this.compareTo((ComparableNode)dependent) < 0) {
            this.rightDependents.remove(dependent);
         }
      }

   }

   public void setIndex(int index) throws MaltChainedException {
      if (index > 0) {
         this.index = index;
      } else {
         throw new SyntaxGraphException("A terminal node must have a positive integer value and not index " + index + ". ");
      }
   }

   public int getIndex() {
      return this.index;
   }

   public int getCompareToIndex() {
      return this.index;
   }

   public boolean isRoot() {
      return false;
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
         for(tmp = this; ((DependencyNode)tmp).hasHead(); tmp = ((DependencyNode)tmp).getHead()) {
         }

         return (DependencyNode)tmp;
      }
   }

   public ComparableNode getLeftmostProperDescendant() throws MaltChainedException {
      ComparableNode candidate = null;
      ComparableNode tmp = null;
      Iterator i$ = this.leftDependents.iterator();

      DependencyNode rdep;
      while(i$.hasNext()) {
         rdep = (DependencyNode)i$.next();
         if (candidate == null) {
            candidate = rdep;
         } else if (rdep.getIndex() < ((ComparableNode)candidate).getIndex()) {
            candidate = rdep;
         }

         tmp = ((Token)rdep).getLeftmostProperDescendant();
         if (tmp != null) {
            if (candidate == null) {
               candidate = tmp;
            } else if (tmp.getIndex() < ((ComparableNode)candidate).getIndex()) {
               candidate = tmp;
            }

            if (((ComparableNode)candidate).getIndex() == 1) {
               return (ComparableNode)candidate;
            }
         }
      }

      i$ = this.rightDependents.iterator();

      while(i$.hasNext()) {
         rdep = (DependencyNode)i$.next();
         if (candidate == null) {
            candidate = rdep;
         } else if (rdep.getIndex() < ((ComparableNode)candidate).getIndex()) {
            candidate = rdep;
         }

         tmp = ((Token)rdep).getLeftmostProperDescendant();
         if (tmp != null) {
            if (candidate == null) {
               candidate = tmp;
            } else if (tmp.getIndex() < ((ComparableNode)candidate).getIndex()) {
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
      ComparableNode tmp = null;
      Iterator i$ = this.leftDependents.iterator();

      DependencyNode rdep;
      while(i$.hasNext()) {
         rdep = (DependencyNode)i$.next();
         if (candidate == null) {
            candidate = rdep;
         } else if (rdep.getIndex() > ((ComparableNode)candidate).getIndex()) {
            candidate = rdep;
         }

         tmp = ((Token)rdep).getRightmostProperDescendant();
         if (tmp != null) {
            if (candidate == null) {
               candidate = tmp;
            } else if (tmp.getIndex() > ((ComparableNode)candidate).getIndex()) {
               candidate = tmp;
            }
         }
      }

      i$ = this.rightDependents.iterator();

      while(i$.hasNext()) {
         rdep = (DependencyNode)i$.next();
         if (candidate == null) {
            candidate = rdep;
         } else if (rdep.getIndex() > ((ComparableNode)candidate).getIndex()) {
            candidate = rdep;
         }

         tmp = ((Token)rdep).getRightmostProperDescendant();
         if (tmp != null) {
            if (candidate == null) {
               candidate = tmp;
            } else if (tmp.getIndex() > ((ComparableNode)candidate).getIndex()) {
               candidate = tmp;
            }
         }
      }

      return (ComparableNode)candidate;
   }

   public ComparableNode getLeftmostDescendant() throws MaltChainedException {
      ComparableNode candidate = this;
      ComparableNode tmp = null;
      Iterator i$ = this.leftDependents.iterator();

      DependencyNode rdep;
      while(i$.hasNext()) {
         rdep = (DependencyNode)i$.next();
         if (candidate == null) {
            candidate = rdep;
         } else if (rdep.getIndex() < ((ComparableNode)candidate).getIndex()) {
            candidate = rdep;
         }

         tmp = ((Token)rdep).getLeftmostDescendant();
         if (tmp != null) {
            if (candidate == null) {
               candidate = tmp;
            } else if (tmp.getIndex() < ((ComparableNode)candidate).getIndex()) {
               candidate = tmp;
            }

            if (((ComparableNode)candidate).getIndex() == 1) {
               return (ComparableNode)candidate;
            }
         }
      }

      i$ = this.rightDependents.iterator();

      while(i$.hasNext()) {
         rdep = (DependencyNode)i$.next();
         if (candidate == null) {
            candidate = rdep;
         } else if (rdep.getIndex() < ((ComparableNode)candidate).getIndex()) {
            candidate = rdep;
         }

         tmp = ((Token)rdep).getLeftmostDescendant();
         if (tmp != null) {
            if (candidate == null) {
               candidate = tmp;
            } else if (tmp.getIndex() < ((ComparableNode)candidate).getIndex()) {
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
      ComparableNode tmp = null;
      Iterator i$ = this.leftDependents.iterator();

      DependencyNode rdep;
      while(i$.hasNext()) {
         rdep = (DependencyNode)i$.next();
         if (candidate == null) {
            candidate = rdep;
         } else if (rdep.getIndex() > ((ComparableNode)candidate).getIndex()) {
            candidate = rdep;
         }

         tmp = ((Token)rdep).getRightmostDescendant();
         if (tmp != null) {
            if (candidate == null) {
               candidate = tmp;
            } else if (tmp.getIndex() > ((ComparableNode)candidate).getIndex()) {
               candidate = tmp;
            }
         }
      }

      i$ = this.rightDependents.iterator();

      while(i$.hasNext()) {
         rdep = (DependencyNode)i$.next();
         if (candidate == null) {
            candidate = rdep;
         } else if (rdep.getIndex() > ((ComparableNode)candidate).getIndex()) {
            candidate = rdep;
         }

         tmp = ((Token)rdep).getRightmostDescendant();
         if (tmp != null) {
            if (candidate == null) {
               candidate = tmp;
            } else if (tmp.getIndex() > ((ComparableNode)candidate).getIndex()) {
               candidate = tmp;
            }
         }
      }

      return (ComparableNode)candidate;
   }

   public PhraseStructureNode getParent() {
      return this.parent;
   }

   public Edge getParentEdge() throws MaltChainedException {
      Iterator i$ = this.incomingEdges.iterator();

      Edge e;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         e = (Edge)i$.next();
      } while(e.getSource() != this.parent || e.getType() != 2);

      return e;
   }

   public String getParentEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
      Iterator i$ = this.incomingEdges.iterator();

      Edge e;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         e = (Edge)i$.next();
      } while(e.getSource() != this.parent || e.getType() != 2);

      return e.getLabelSymbol(table);
   }

   public int getParentEdgeLabelCode(SymbolTable table) throws MaltChainedException {
      Iterator i$ = this.incomingEdges.iterator();

      Edge e;
      do {
         if (!i$.hasNext()) {
            return -1;
         }

         e = (Edge)i$.next();
      } while(e.getSource() != this.parent || e.getType() != 2);

      return e.getLabelCode(table);
   }

   public boolean hasParentEdgeLabel(SymbolTable table) throws MaltChainedException {
      Iterator i$ = this.incomingEdges.iterator();

      Edge e;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         e = (Edge)i$.next();
      } while(e.getSource() != this.parent || e.getType() != 2);

      return e.hasLabel(table);
   }

   public boolean hasAtMostOneHead() {
      return this.heads.size() <= 1;
   }

   public boolean hasAncestorInside(int left, int right) throws MaltChainedException {
      if (this.getHead() != null) {
         DependencyNode tmp = this.getHead();
         if (tmp.getIndex() >= left && tmp.getIndex() <= right) {
            return true;
         }
      }

      return false;
   }

   public Set<Edge> getHeadEdges() throws MaltChainedException {
      return this.incomingEdges;
   }

   public Set<DependencyNode> getHeads() throws MaltChainedException {
      return this.heads;
   }

   public boolean hasHead() {
      return this.heads.size() != 0;
   }

   public DependencyNode getHead() throws MaltChainedException {
      if (this.heads.size() == 0) {
         return null;
      } else {
         if (this.heads.size() == 1) {
            Iterator i$ = this.heads.iterator();
            if (i$.hasNext()) {
               DependencyNode head = (DependencyNode)i$.next();
               return head;
            }
         }

         if (this.heads.size() > 1) {
            throw new SyntaxGraphException("The dependency node is multi-headed and it is ambigious to return a single-head dependency node. ");
         } else {
            return null;
         }
      }
   }

   public Edge getHeadEdge() throws MaltChainedException {
      if (this.heads.size() == 0) {
         return null;
      } else if (this.incomingEdges.size() == 1 && this.incomingEdges.first() instanceof DependencyNode) {
         return (Edge)this.incomingEdges.first();
      } else {
         if (this.heads.size() == 1) {
            Iterator i$ = this.incomingEdges.iterator();

            while(i$.hasNext()) {
               Edge e = (Edge)i$.next();
               if (e.getSource() == this.heads.first()) {
                  return e;
               }
            }
         }

         return null;
      }
   }

   public void addHeadEdgeLabel(SymbolTable table, String symbol) throws MaltChainedException {
      if (this.hasHead()) {
         this.getHeadEdge().addLabel(table, symbol);
      }

   }

   public void addHeadEdgeLabel(SymbolTable table, int code) throws MaltChainedException {
      if (this.hasHead()) {
         this.getHeadEdge().addLabel(table, code);
      }

   }

   public void addHeadEdgeLabel(LabelSet labelSet) throws MaltChainedException {
      if (this.hasHead()) {
         this.getHeadEdge().addLabel(labelSet);
      }

   }

   public boolean hasHeadEdgeLabel(SymbolTable table) throws MaltChainedException {
      return !this.hasHead() ? false : this.getHeadEdge().hasLabel(table);
   }

   public String getHeadEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
      return this.getHeadEdge().getLabelSymbol(table);
   }

   public int getHeadEdgeLabelCode(SymbolTable table) throws MaltChainedException {
      return !this.hasHead() ? 0 : this.getHeadEdge().getLabelCode(table);
   }

   public boolean isHeadEdgeLabeled() throws MaltChainedException {
      return !this.hasHead() ? false : this.getHeadEdge().isLabeled();
   }

   public int nHeadEdgeLabels() throws MaltChainedException {
      return !this.hasHead() ? 0 : this.getHeadEdge().nLabels();
   }

   public Set<SymbolTable> getHeadEdgeLabelTypes() throws MaltChainedException {
      return this.getHeadEdge().getLabelTypes();
   }

   public LabelSet getHeadEdgeLabelSet() throws MaltChainedException {
      return this.getHeadEdge().getLabelSet();
   }

   public boolean hasDependent() {
      return this.hasLeftDependent() || this.hasRightDependent();
   }

   public boolean hasLeftDependent() {
      return !this.leftDependents.isEmpty();
   }

   public DependencyNode getLeftDependent(int index) {
      if (0 <= index && index < this.leftDependents.size()) {
         int i = 0;

         for(Iterator i$ = this.leftDependents.iterator(); i$.hasNext(); ++i) {
            DependencyNode node = (DependencyNode)i$.next();
            if (i == index) {
               return node;
            }
         }
      }

      return null;
   }

   public int getLeftDependentCount() {
      return this.leftDependents.size();
   }

   public SortedSet<DependencyNode> getLeftDependents() {
      return this.leftDependents;
   }

   public DependencyNode getLeftSibling() throws MaltChainedException {
      if (this.getHead() == null) {
         return null;
      } else {
         DependencyNode candidate = null;

         Iterator i$;
         DependencyNode node;
         for(i$ = this.getHead().getLeftDependents().iterator(); i$.hasNext(); candidate = node) {
            node = (DependencyNode)i$.next();
            if (node == this) {
               return candidate;
            }
         }

         for(i$ = this.getHead().getRightDependents().iterator(); i$.hasNext(); candidate = node) {
            node = (DependencyNode)i$.next();
            if (node == this) {
               return candidate;
            }
         }

         return null;
      }
   }

   public DependencyNode getSameSideLeftSibling() throws MaltChainedException {
      if (this.getHead() == null) {
         return null;
      } else if (this.getIndex() < this.getHead().getIndex()) {
         try {
            return (DependencyNode)this.getHead().getLeftDependents().headSet(this).last();
         } catch (NoSuchElementException var2) {
            return null;
         }
      } else if (this.getIndex() > this.getHead().getIndex()) {
         try {
            return (DependencyNode)this.getHead().getRightDependents().headSet(this).last();
         } catch (NoSuchElementException var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   public DependencyNode getClosestLeftDependent() {
      try {
         return (DependencyNode)this.leftDependents.last();
      } catch (NoSuchElementException var2) {
         return null;
      }
   }

   public DependencyNode getLeftmostDependent() {
      Iterator i$ = this.leftDependents.iterator();
      if (i$.hasNext()) {
         DependencyNode dep = (DependencyNode)i$.next();
         return dep;
      } else {
         return null;
      }
   }

   public DependencyNode getRightDependent(int index) {
      int size = this.rightDependents.size();
      return index < size ? ((DependencyNode[])this.rightDependents.toArray(new DependencyNode[size]))[size - 1 - index] : null;
   }

   public int getRightDependentCount() {
      return this.rightDependents.size();
   }

   public SortedSet<DependencyNode> getRightDependents() {
      return this.rightDependents;
   }

   public DependencyNode getRightSibling() throws MaltChainedException {
      if (this.getHead() == null) {
         return null;
      } else {
         Iterator i$ = this.getHead().getLeftDependents().iterator();

         DependencyNode node;
         do {
            if (!i$.hasNext()) {
               i$ = this.getHead().getRightDependents().iterator();

               do {
                  if (!i$.hasNext()) {
                     return null;
                  }

                  node = (DependencyNode)i$.next();
               } while(node.getIndex() <= this.getIndex());

               return node;
            }

            node = (DependencyNode)i$.next();
         } while(node.getIndex() <= this.getIndex());

         return node;
      }
   }

   public DependencyNode getSameSideRightSibling() throws MaltChainedException {
      if (this.getHead() == null) {
         return null;
      } else {
         SortedSet tailSet;
         if (this.getIndex() < this.getHead().getIndex()) {
            tailSet = this.getHead().getLeftDependents().tailSet(this);
            return tailSet.size() <= 1 ? null : ((DependencyNode[])tailSet.toArray(new DependencyNode[tailSet.size()]))[1];
         } else if (this.getIndex() > this.getHead().getIndex()) {
            tailSet = this.getHead().getRightDependents().tailSet(this);
            return tailSet.size() <= 1 ? null : ((DependencyNode[])tailSet.toArray(new DependencyNode[tailSet.size()]))[1];
         } else {
            return null;
         }
      }
   }

   public DependencyNode getClosestRightDependent() {
      Iterator i$ = this.rightDependents.iterator();
      if (i$.hasNext()) {
         DependencyNode dep = (DependencyNode)i$.next();
         return dep;
      } else {
         return null;
      }
   }

   public DependencyNode getRightmostDependent() {
      int n = this.rightDependents.size();
      int i = 1;

      for(Iterator i$ = this.rightDependents.iterator(); i$.hasNext(); ++i) {
         DependencyNode node = (DependencyNode)i$.next();
         if (i == n) {
            return node;
         }
      }

      return null;
   }

   public List<DependencyNode> getListOfDependents() {
      List<DependencyNode> dependentList = new ArrayList();
      Iterator i$ = this.leftDependents.iterator();

      DependencyNode node;
      while(i$.hasNext()) {
         node = (DependencyNode)i$.next();
         dependentList.add(node);
      }

      i$ = this.rightDependents.iterator();

      while(i$.hasNext()) {
         node = (DependencyNode)i$.next();
         dependentList.add(node);
      }

      return dependentList;
   }

   public List<DependencyNode> getListOfLeftDependents() {
      List<DependencyNode> leftDependentList = new ArrayList();
      Iterator i$ = this.leftDependents.iterator();

      while(i$.hasNext()) {
         DependencyNode node = (DependencyNode)i$.next();
         leftDependentList.add(node);
      }

      return leftDependentList;
   }

   public List<DependencyNode> getListOfRightDependents() {
      List<DependencyNode> rightDependentList = new ArrayList();
      Iterator i$ = this.rightDependents.iterator();

      while(i$.hasNext()) {
         DependencyNode node = (DependencyNode)i$.next();
         rightDependentList.add(node);
      }

      return rightDependentList;
   }

   protected void getDependencyDominationSet(SortedSet<DependencyNode> dominationSet) {
      if (this.leftDependents.size() > 0 || this.rightDependents.size() > 0) {
         dominationSet.addAll(this.leftDependents);
         dominationSet.addAll(this.rightDependents);
         Iterator i$ = this.leftDependents.iterator();

         DependencyNode node;
         while(i$.hasNext()) {
            node = (DependencyNode)i$.next();
            ((Token)node).getDependencyDominationSet(dominationSet);
         }

         i$ = this.rightDependents.iterator();

         while(i$.hasNext()) {
            node = (DependencyNode)i$.next();
            ((Token)node).getDependencyDominationSet(dominationSet);
         }
      }

   }

   public boolean hasRightDependent() {
      return !this.rightDependents.isEmpty();
   }

   public boolean isProjective() throws MaltChainedException {
      if (this.hasHead() && !this.getHead().isRoot()) {
         DependencyNode head = this.getHead();
         DependencyNode tmp;
         if (this.getHead().getIndex() < this.getIndex()) {
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

   public void clear() throws MaltChainedException {
      super.clear();
      this.predecessor = null;
      this.successor = null;
      this.component = this;
      this.rank = 0;
      this.parent = null;
      this.heads.clear();
      this.leftDependents.clear();
      this.rightDependents.clear();
   }

   public int compareTo(ComparableNode that) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == that) {
         return 0;
      } else if (that instanceof TokenNode) {
         if (this.index < that.getCompareToIndex()) {
            return -1;
         } else {
            return this.index > that.getCompareToIndex() ? 1 : super.compareTo(that);
         }
      } else {
         if (that instanceof NonTerminalNode) {
            try {
               int thisLCorner = this.index;
               int thatLCorner = that.getLeftmostProperDescendantIndex();
               int thisRCorner = this.index;
               int thatRCorner = that.getRightmostProperDescendantIndex();
               if (thisLCorner != -1 && thatLCorner != -1 && thisRCorner != -1 && thatRCorner != -1) {
                  if (thisLCorner < thatLCorner && thisRCorner < thatRCorner) {
                     return -1;
                  }

                  if (thisLCorner > thatLCorner && thisRCorner > thatRCorner) {
                     return 1;
                  }

                  if (thisLCorner > thatLCorner && thisRCorner < thatRCorner) {
                     return -1;
                  }

                  if (thisLCorner < thatLCorner && thisRCorner > thatRCorner) {
                     return 1;
                  }
               } else {
                  if (thisLCorner != -1 && thatLCorner != -1) {
                     if (thisLCorner < thatLCorner) {
                        return -1;
                     }

                     if (thisLCorner > thatLCorner) {
                        return 1;
                     }
                  }

                  if (thisRCorner != -1 && thatRCorner != -1) {
                     if (thisRCorner < thatRCorner) {
                        return -1;
                     }

                     if (thisRCorner > thatRCorner) {
                        return 1;
                     }
                  }
               }
            } catch (MaltChainedException var9) {
               if (SystemLogger.logger().isDebugEnabled()) {
                  SystemLogger.logger().debug("", var9);
               } else {
                  SystemLogger.logger().error(var9.getMessageChain());
               }

               System.exit(1);
            }
         }

         if (this.index < that.getCompareToIndex()) {
            return -1;
         } else {
            return this.index > that.getCompareToIndex() ? 1 : super.compareTo(that);
         }
      }
   }

   public boolean equals(Object obj) {
      Token v = (Token)obj;
      return this.predecessor == v.predecessor && this.successor == v.successor ? super.equals(obj) : false;
   }

   public int hashCode() {
      int hash = 7;
      int hash = 31 * hash + (null == this.predecessor ? 0 : this.predecessor.hashCode());
      hash = 31 * hash + (null == this.successor ? 0 : this.successor.hashCode());
      return 31 * hash + super.hashCode();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      return sb.toString();
   }
}
