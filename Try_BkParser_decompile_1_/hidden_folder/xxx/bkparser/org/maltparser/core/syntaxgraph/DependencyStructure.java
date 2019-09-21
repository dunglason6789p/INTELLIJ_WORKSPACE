package org.maltparser.core.syntaxgraph;

import java.util.SortedMap;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public interface DependencyStructure extends TokenStructure, SecEdgeStructure {
   DependencyNode addDependencyNode() throws MaltChainedException;

   DependencyNode addDependencyNode(int var1) throws MaltChainedException;

   DependencyNode getDependencyNode(int var1) throws MaltChainedException;

   int nDependencyNode();

   int getHighestDependencyNodeIndex();

   Edge addDependencyEdge(int var1, int var2) throws MaltChainedException;

   Edge moveDependencyEdge(int var1, int var2) throws MaltChainedException;

   void removeDependencyEdge(int var1, int var2) throws MaltChainedException;

   int nEdges();

   SortedSet<Edge> getEdges();

   SortedSet<Integer> getDependencyIndices();

   DependencyNode getDependencyRoot();

   boolean hasLabeledDependency(int var1) throws MaltChainedException;

   boolean isConnected();

   boolean isProjective() throws MaltChainedException;

   boolean isSingleHeaded();

   boolean isTree();

   int nNonProjectiveEdges() throws MaltChainedException;

   void linkAllTreesToRoot() throws MaltChainedException;

   LabelSet getDefaultRootEdgeLabels() throws MaltChainedException;

   String getDefaultRootEdgeLabelSymbol(SymbolTable var1) throws MaltChainedException;

   int getDefaultRootEdgeLabelCode(SymbolTable var1) throws MaltChainedException;

   void setDefaultRootEdgeLabel(SymbolTable var1, String var2) throws MaltChainedException;

   void setDefaultRootEdgeLabels(String var1, SortedMap<String, SymbolTable> var2) throws MaltChainedException;
}
