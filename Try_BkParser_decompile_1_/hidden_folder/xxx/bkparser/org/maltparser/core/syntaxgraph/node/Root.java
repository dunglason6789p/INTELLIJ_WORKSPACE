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
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.headrules.Direction;
import org.maltparser.core.syntaxgraph.headrules.HeadRules;

public class Root extends GraphNode implements DependencyNode, PhraseStructureNode, NonTerminalNode {
   protected final SortedSet<DependencyNode> leftDependents = new TreeSet();
   protected final SortedSet<DependencyNode> rightDependents = new TreeSet();
   protected final SortedSet<PhraseStructureNode> children = new TreeSet();
   protected DependencyNode component;
   protected int rank;

   public Root() throws MaltChainedException {
      this.clear();
   }

   public void addIncomingEdge(Edge in) throws MaltChainedException {
      throw new SyntaxGraphException("It is not allowed for a root node to have an incoming edge");
   }

   public void removeIncomingEdge(Edge in) {
   }

   public void addOutgoingEdge(Edge out) throws MaltChainedException {
      super.addOutgoingEdge(out);
      if (out.getTarget() != null) {
         if (out.getType() == 1 && out.getTarget() instanceof DependencyNode) {
            Node dependent = out.getTarget();
            if (this.compareTo((ComparableNode)dependent) > 0) {
               this.leftDependents.add((DependencyNode)dependent);
            } else if (this.compareTo((ComparableNode)dependent) < 0) {
               this.rightDependents.add((DependencyNode)dependent);
            }
         } else if (out.getType() == 2 && out.getTarget() instanceof PhraseStructureNode) {
            this.children.add((PhraseStructureNode)out.getTarget());
         }
      }

   }

   public void removeOutgoingEdge(Edge out) throws MaltChainedException {
      super.removeOutgoingEdge(out);
      if (out.getTarget() != null) {
         if (out.getType() == 1 && out.getTarget() instanceof DependencyNode) {
            Node dependent = out.getTarget();
            if (this.compareTo((ComparableNode)dependent) > 0) {
               this.leftDependents.remove((DependencyNode)dependent);
            } else if (this.compareTo((ComparableNode)dependent) < 0) {
               this.rightDependents.remove((DependencyNode)dependent);
            }
         } else if (out.getType() == 2 && out.getTarget() instanceof PhraseStructureNode) {
            this.children.remove((PhraseStructureNode)out.getTarget());
         }
      }

   }

   public DependencyNode getPredecessor() {
      return null;
   }

   public DependencyNode getSuccessor() {
      return null;
   }

   public DependencyNode getAncestor() throws MaltChainedException {
      return this;
   }

   public DependencyNode getProperAncestor() throws MaltChainedException {
      return null;
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

   public boolean isContinuous() {
      return true;
   }

   public boolean isContinuousExcludeTerminalsAttachToRoot() {
      return true;
   }

   public PhraseStructureNode getParent() {
      return null;
   }

   public Edge getParentEdge() throws MaltChainedException {
      return null;
   }

   public String getParentEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
      return null;
   }

   public int getParentEdgeLabelCode(SymbolTable table) throws MaltChainedException {
      return -1;
   }

   public boolean hasParentEdgeLabel(SymbolTable table) throws MaltChainedException {
      return false;
   }

   public SortedSet<PhraseStructureNode> getChildren() {
      return new TreeSet(this.children);
   }

   public PhraseStructureNode getChild(int index) {
      return index >= 0 && index < this.children.size() ? ((PhraseStructureNode[])this.children.toArray(new PhraseStructureNode[this.children.size()]))[index] : null;
   }

   public PhraseStructureNode getLeftChild() {
      Iterator i$ = this.children.iterator();
      if (i$.hasNext()) {
         PhraseStructureNode node = (PhraseStructureNode)i$.next();
         return node;
      } else {
         return null;
      }
   }

   public PhraseStructureNode getRightChild() {
      int n = this.children.size();
      int i = 1;
      Iterator i$ = this.children.iterator();

      PhraseStructureNode node;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         node = (PhraseStructureNode)i$.next();
      } while(i != n);

      return node;
   }

   public int nChildren() {
      return this.children.size();
   }

   public boolean hasNonTerminalChildren() {
      Iterator i$ = this.children.iterator();

      PhraseStructureNode node;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         node = (PhraseStructureNode)i$.next();
      } while(!(node instanceof NonTerminal));

      return true;
   }

   public boolean hasTerminalChildren() {
      Iterator i$ = this.children.iterator();

      PhraseStructureNode node;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         node = (PhraseStructureNode)i$.next();
      } while(!(node instanceof Token));

      return true;
   }

   public int getHeight() {
      int max = -1;
      Iterator i$ = this.children.iterator();

      while(i$.hasNext()) {
         PhraseStructureNode node = (PhraseStructureNode)i$.next();
         if (node instanceof Token) {
            if (max < 0) {
               max = 0;
            }
         } else {
            int nodeheight = ((NonTerminalNode)node).getHeight();
            if (max < nodeheight) {
               max = nodeheight;
            }
         }
      }

      return max + 1;
   }

   public TokenNode getLexicalHead(HeadRules headRules) throws MaltChainedException {
      return this.identifyHead(headRules);
   }

   public PhraseStructureNode getHeadChild(HeadRules headRules) throws MaltChainedException {
      return this.identifyHeadChild(headRules);
   }

   public TokenNode getLexicalHead() throws MaltChainedException {
      return this.identifyHead((HeadRules)null);
   }

   public PhraseStructureNode getHeadChild() throws MaltChainedException {
      return this.identifyHeadChild((HeadRules)null);
   }

   private PhraseStructureNode identifyHeadChild(HeadRules headRules) throws MaltChainedException {
      PhraseStructureNode headChild = headRules == null ? null : headRules.getHeadChild(this);
      if (headChild == null) {
         Direction direction = headRules == null ? Direction.LEFT : headRules.getDefaultDirection(this);
         if (direction == Direction.LEFT) {
            if ((headChild = this.leftmostTerminalChild()) == null) {
               headChild = this.leftmostNonTerminalChild();
            }
         } else if ((headChild = this.rightmostTerminalChild()) == null) {
            headChild = this.rightmostNonTerminalChild();
         }
      }

      return headChild;
   }

   public TokenNode identifyHead(HeadRules headRules) throws MaltChainedException {
      PhraseStructureNode headChild = this.identifyHeadChild(headRules);
      TokenNode lexicalHead = null;
      if (headChild instanceof NonTerminalNode) {
         lexicalHead = ((NonTerminalNode)headChild).identifyHead(headRules);
      } else if (headChild instanceof TokenNode) {
         lexicalHead = (TokenNode)headChild;
      }

      Iterator i$ = this.children.iterator();

      while(i$.hasNext()) {
         PhraseStructureNode node = (PhraseStructureNode)i$.next();
         if (node != headChild && node instanceof NonTerminalNode) {
            ((NonTerminalNode)node).identifyHead(headRules);
         }
      }

      return lexicalHead;
   }

   private PhraseStructureNode leftmostTerminalChild() {
      Iterator i$ = this.children.iterator();

      PhraseStructureNode node;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         node = (PhraseStructureNode)i$.next();
      } while(!(node instanceof TokenNode));

      return node;
   }

   private PhraseStructureNode leftmostNonTerminalChild() {
      Iterator i$ = this.children.iterator();

      PhraseStructureNode node;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         node = (PhraseStructureNode)i$.next();
      } while(!(node instanceof NonTerminalNode));

      return node;
   }

   private PhraseStructureNode rightmostTerminalChild() {
      try {
         if (this.children.last() instanceof TokenNode) {
            return (PhraseStructureNode)this.children.last();
         }
      } catch (NoSuchElementException var4) {
      }

      PhraseStructureNode candidate = null;
      Iterator i$ = this.children.iterator();

      while(i$.hasNext()) {
         PhraseStructureNode node = (PhraseStructureNode)i$.next();
         if (node instanceof TokenNode) {
            candidate = node;
         }
      }

      return candidate;
   }

   private PhraseStructureNode rightmostNonTerminalChild() {
      try {
         if (this.children.last() instanceof NonTerminalNode) {
            return (PhraseStructureNode)this.children.last();
         }
      } catch (NoSuchElementException var4) {
      }

      PhraseStructureNode candidate = null;
      Iterator i$ = this.children.iterator();

      while(i$.hasNext()) {
         PhraseStructureNode node = (PhraseStructureNode)i$.next();
         if (node instanceof NonTerminalNode) {
            candidate = node;
         }
      }

      return candidate;
   }

   public boolean hasAtMostOneHead() {
      return true;
   }

   public boolean hasAncestorInside(int left, int right) throws MaltChainedException {
      return false;
   }

   public boolean hasHead() {
      return false;
   }

   public DependencyNode getHead() throws MaltChainedException {
      return null;
   }

   public Edge getHeadEdge() throws MaltChainedException {
      return null;
   }

   public void addHeadEdgeLabel(SymbolTable table, String symbol) throws MaltChainedException {
   }

   public void addHeadEdgeLabel(SymbolTable table, int code) throws MaltChainedException {
   }

   public void addHeadEdgeLabel(LabelSet labelSet) throws MaltChainedException {
   }

   public int getHeadEdgeLabelCode(SymbolTable table) throws MaltChainedException {
      return 0;
   }

   public LabelSet getHeadEdgeLabelSet() throws MaltChainedException {
      return null;
   }

   public String getHeadEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
      return null;
   }

   public Set<SymbolTable> getHeadEdgeLabelTypes() throws MaltChainedException {
      return null;
   }

   public boolean hasHeadEdgeLabel(SymbolTable table) throws MaltChainedException {
      return false;
   }

   public boolean isHeadEdgeLabeled() throws MaltChainedException {
      return false;
   }

   public int nHeadEdgeLabels() throws MaltChainedException {
      return 0;
   }

   public Set<Edge> getHeadEdges() throws MaltChainedException {
      return null;
   }

   public Set<DependencyNode> getHeads() throws MaltChainedException {
      return null;
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
      return null;
   }

   public DependencyNode getSameSideLeftSibling() throws MaltChainedException {
      return null;
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
      return null;
   }

   public DependencyNode getSameSideRightSibling() throws MaltChainedException {
      return null;
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

   public boolean hasRightDependent() {
      return !this.rightDependents.isEmpty();
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

   public boolean isProjective() throws MaltChainedException {
      return true;
   }

   public int getDependencyNodeDepth() throws MaltChainedException {
      return 0;
   }

   public int getIndex() {
      return 0;
   }

   public int getCompareToIndex() {
      return 0;
   }

   public boolean isRoot() {
      return true;
   }

   public ComparableNode getLeftmostProperDescendant() throws MaltChainedException {
      NonTerminalNode node = this;

      PhraseStructureNode candidate;
      for(candidate = null; node != null; node = (NonTerminalNode)candidate) {
         candidate = ((NonTerminalNode)node).getLeftChild();
         if (candidate == null || candidate instanceof TokenNode) {
            break;
         }
      }

      if (candidate == null && candidate instanceof NonTerminalNode) {
         candidate = null;
         DependencyNode dep = null;
         Iterator i$ = ((TokenStructure)this.getBelongsToGraph()).getTokenIndices().iterator();

         while(i$.hasNext()) {
            int index = (Integer)i$.next();

            for(dep = ((TokenStructure)this.getBelongsToGraph()).getTokenNode(index); dep != null; dep = ((DependencyNode)dep).getHead()) {
               if (dep == this) {
                  return (ComparableNode)dep;
               }
            }
         }
      }

      return candidate;
   }

   public ComparableNode getRightmostProperDescendant() throws MaltChainedException {
      NonTerminalNode node = this;

      PhraseStructureNode candidate;
      for(candidate = null; node != null; node = (NonTerminalNode)candidate) {
         candidate = ((NonTerminalNode)node).getRightChild();
         if (candidate == null || candidate instanceof TokenNode) {
            break;
         }
      }

      if (candidate == null && candidate instanceof NonTerminalNode) {
         candidate = null;
         DependencyNode dep = null;

         for(int i = ((TokenStructure)this.getBelongsToGraph()).getHighestTokenIndex(); i > 0; --i) {
            for(dep = ((TokenStructure)this.getBelongsToGraph()).getTokenNode(i); dep != null; dep = ((DependencyNode)dep).getHead()) {
               if (dep == this) {
                  return (ComparableNode)dep;
               }
            }
         }
      }

      return candidate;
   }

   public ComparableNode getLeftmostDescendant() throws MaltChainedException {
      return this.getLeftmostProperDescendant();
   }

   public ComparableNode getRightmostDescendant() throws MaltChainedException {
      return this.getRightmostProperDescendant();
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

   public void setIndex(int index) throws MaltChainedException {
   }

   public void clear() throws MaltChainedException {
      super.clear();
      this.component = this;
      this.rank = 0;
      this.leftDependents.clear();
      this.rightDependents.clear();
      this.children.clear();
   }

   public int compareTo(ComparableNode o) {
      int BEFORE = true;
      int EQUAL = false;
      int AFTER = true;
      if (this == o) {
         return 0;
      } else {
         try {
            int thisLCorner = this.getLeftmostProperDescendantIndex();
            int thatLCorner = o instanceof TokenNode ? o.getCompareToIndex() : o.getLeftmostProperDescendantIndex();
            int thisRCorner = this.getRightmostProperDescendantIndex();
            int thatRCorner = o instanceof TokenNode ? o.getCompareToIndex() : o.getRightmostProperDescendantIndex();
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

         if (0 < o.getCompareToIndex()) {
            return -1;
         } else {
            return 0 > o.getCompareToIndex() ? 1 : super.compareTo(o);
         }
      }
   }

   public boolean equals(Object obj) {
      return super.equals(obj);
   }

   public int hashCode() {
      return 217 + super.hashCode();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(super.toString());
      return sb.toString();
   }
}
