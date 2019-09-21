package org.maltparser.core.syntaxgraph.node;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;

public interface DependencyNode extends ComparableNode {
   boolean hasAtMostOneHead();

   boolean hasHead();

   Set<DependencyNode> getHeads() throws MaltChainedException;

   Set<Edge> getHeadEdges() throws MaltChainedException;

   DependencyNode getPredecessor();

   DependencyNode getSuccessor();

   DependencyNode getHead() throws MaltChainedException;

   Edge getHeadEdge() throws MaltChainedException;

   boolean hasAncestorInside(int var1, int var2) throws MaltChainedException;

   void addHeadEdgeLabel(SymbolTable var1, String var2) throws MaltChainedException;

   void addHeadEdgeLabel(SymbolTable var1, int var2) throws MaltChainedException;

   void addHeadEdgeLabel(LabelSet var1) throws MaltChainedException;

   boolean hasHeadEdgeLabel(SymbolTable var1) throws MaltChainedException;

   String getHeadEdgeLabelSymbol(SymbolTable var1) throws MaltChainedException;

   int getHeadEdgeLabelCode(SymbolTable var1) throws MaltChainedException;

   boolean isHeadEdgeLabeled() throws MaltChainedException;

   int nHeadEdgeLabels() throws MaltChainedException;

   Set<SymbolTable> getHeadEdgeLabelTypes() throws MaltChainedException;

   LabelSet getHeadEdgeLabelSet() throws MaltChainedException;

   DependencyNode getAncestor() throws MaltChainedException;

   DependencyNode getProperAncestor() throws MaltChainedException;

   boolean hasDependent();

   boolean hasLeftDependent();

   DependencyNode getLeftDependent(int var1);

   int getLeftDependentCount();

   SortedSet<DependencyNode> getLeftDependents();

   DependencyNode getLeftSibling() throws MaltChainedException;

   DependencyNode getSameSideLeftSibling() throws MaltChainedException;

   DependencyNode getClosestLeftDependent();

   DependencyNode getLeftmostDependent();

   DependencyNode getRightDependent(int var1);

   int getRightDependentCount();

   SortedSet<DependencyNode> getRightDependents();

   DependencyNode getRightSibling() throws MaltChainedException;

   DependencyNode getSameSideRightSibling() throws MaltChainedException;

   DependencyNode getClosestRightDependent();

   DependencyNode getRightmostDependent();

   boolean hasRightDependent();

   List<DependencyNode> getListOfDependents();

   List<DependencyNode> getListOfLeftDependents();

   List<DependencyNode> getListOfRightDependents();

   boolean isProjective() throws MaltChainedException;

   int getDependencyNodeDepth() throws MaltChainedException;

   int getRank();

   void setRank(int var1);

   DependencyNode findComponent();

   DependencyNode getComponent();

   void setComponent(DependencyNode var1);
}
