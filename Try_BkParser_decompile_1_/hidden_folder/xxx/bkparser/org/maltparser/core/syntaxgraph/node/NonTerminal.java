package org.maltparser.core.syntaxgraph.node;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.headrules.Direction;
import org.maltparser.core.syntaxgraph.headrules.HeadRules;

public class NonTerminal extends GraphNode implements PhraseStructureNode, NonTerminalNode {
   public static final int INDEX_OFFSET = 10000000;
   protected final SortedSet<PhraseStructureNode> children = new TreeSet();
   protected PhraseStructureNode parent;
   protected int index = -1;

   public NonTerminal() throws MaltChainedException {
   }

   public void addIncomingEdge(Edge in) throws MaltChainedException {
      super.addIncomingEdge(in);
      if (in.getType() == 2 && in.getSource() instanceof PhraseStructureNode) {
         this.parent = (PhraseStructureNode)in.getSource();
      }

   }

   public void removeIncomingEdge(Edge in) throws MaltChainedException {
      super.removeIncomingEdge(in);
      if (in.getType() == 2 && in.getSource() instanceof PhraseStructureNode && in.getSource() == this.parent) {
         this.parent = null;
      }

   }

   public void addOutgoingEdge(Edge out) throws MaltChainedException {
      super.addOutgoingEdge(out);
      if (out.getType() == 2 && out.getTarget() instanceof PhraseStructureNode) {
         this.children.add((PhraseStructureNode)out.getTarget());
      }

   }

   public void removeOutgoingEdge(Edge out) throws MaltChainedException {
      super.removeOutgoingEdge(out);
      if (out.getType() == 2 && out.getTarget() instanceof PhraseStructureNode) {
         this.children.remove(out.getTarget());
      }

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

   public SortedSet<PhraseStructureNode> getChildren() {
      return new TreeSet(this.children);
   }

   public PhraseStructureNode getChild(int index) {
      if (index >= 0 && index < this.children.size()) {
         int i = 0;

         for(Iterator i$ = this.children.iterator(); i$.hasNext(); ++i) {
            PhraseStructureNode node = (PhraseStructureNode)i$.next();
            if (i == index) {
               return node;
            }
         }
      }

      return null;
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

      for(Iterator i$ = this.children.iterator(); i$.hasNext(); ++i) {
         PhraseStructureNode node = (PhraseStructureNode)i$.next();
         if (i == n) {
            return node;
         }
      }

      return null;
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

   public int getIndex() {
      return this.index;
   }

   public int getCompareToIndex() {
      return this.index + 10000000;
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

   public boolean isContinuous() {
      int lcorner = this.getLeftmostProperDescendant().getIndex();
      int rcorner = this.getRightmostProperDescendant().getIndex();
      if (lcorner == rcorner) {
         return true;
      } else {
         for(TokenNode terminal = ((TokenStructure)this.getBelongsToGraph()).getTokenNode(lcorner); terminal.getIndex() != rcorner; terminal = terminal.getTokenNodeSuccessor()) {
            for(PhraseStructureNode tmp = terminal.getParent(); tmp != this; tmp = tmp.getParent()) {
               if (tmp == null) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public boolean isContinuousExcludeTerminalsAttachToRoot() {
      int lcorner = this.getLeftmostProperDescendant().getIndex();
      int rcorner = this.getRightmostProperDescendant().getIndex();
      if (lcorner == rcorner) {
         return true;
      } else {
         TokenNode terminal = ((TokenStructure)this.getBelongsToGraph()).getTokenNode(lcorner);

         while(true) {
            while(terminal.getIndex() != rcorner) {
               if (terminal.getParent() != null && terminal.getParent().isRoot()) {
                  terminal = terminal.getTokenNodeSuccessor();
               } else {
                  for(PhraseStructureNode tmp = terminal.getParent(); tmp != this; tmp = tmp.getParent()) {
                     if (tmp == null) {
                        return false;
                     }
                  }

                  terminal = terminal.getTokenNodeSuccessor();
               }
            }

            return true;
         }
      }
   }

   public boolean isRoot() {
      return false;
   }

   public ComparableNode getLeftmostProperDescendant() {
      NonTerminalNode node = this;

      for(PhraseStructureNode candidate = null; node != null; node = (NonTerminalNode)candidate) {
         candidate = ((NonTerminalNode)node).getLeftChild();
         if (candidate == null || candidate instanceof TokenNode) {
            return candidate;
         }
      }

      return null;
   }

   public ComparableNode getRightmostProperDescendant() {
      NonTerminalNode node = this;

      for(PhraseStructureNode candidate = null; node != null; node = (NonTerminalNode)candidate) {
         candidate = ((NonTerminalNode)node).getRightChild();
         if (candidate == null || candidate instanceof TokenNode) {
            return candidate;
         }
      }

      return null;
   }

   public ComparableNode getLeftmostDescendant() throws MaltChainedException {
      return this.getLeftmostProperDescendant();
   }

   public ComparableNode getRightmostDescendant() throws MaltChainedException {
      return this.getRightmostProperDescendant();
   }

   public void setIndex(int index) throws MaltChainedException {
      if (index > 0) {
         this.index = index;
      } else {
         throw new SyntaxGraphException("The index must be a positive index");
      }
   }

   public void clear() throws MaltChainedException {
      super.clear();
      this.children.clear();
      this.parent = null;
      this.index = -1;
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

         if (this.getCompareToIndex() < o.getCompareToIndex()) {
            return -1;
         } else {
            return this.getCompareToIndex() > o.getCompareToIndex() ? 1 : super.compareTo(o);
         }
      }
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof NonTerminal) ? false : super.equals(obj);
      }
   }

   public int hashCode() {
      return 217 + super.hashCode();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getIndex());
      sb.append('\t');

      for(Iterator i$ = this.getLabelTypes().iterator(); i$.hasNext(); sb.append('\t')) {
         SymbolTable table = (SymbolTable)i$.next();

         try {
            sb.append(this.getLabelSymbol(table));
         } catch (MaltChainedException var5) {
            System.err.println("Print error : " + var5.getMessageChain());
         }
      }

      return sb.toString();
   }
}
