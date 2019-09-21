/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.SortedMap;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.SecEdgeStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public interface DependencyStructure
extends TokenStructure,
SecEdgeStructure {
    public DependencyNode addDependencyNode() throws MaltChainedException;

    public DependencyNode addDependencyNode(int var1) throws MaltChainedException;

    public DependencyNode getDependencyNode(int var1) throws MaltChainedException;

    public int nDependencyNode();

    public int getHighestDependencyNodeIndex();

    public Edge addDependencyEdge(int var1, int var2) throws MaltChainedException;

    public Edge moveDependencyEdge(int var1, int var2) throws MaltChainedException;

    public void removeDependencyEdge(int var1, int var2) throws MaltChainedException;

    public int nEdges();

    public SortedSet<Edge> getEdges();

    public SortedSet<Integer> getDependencyIndices();

    public DependencyNode getDependencyRoot();

    public boolean hasLabeledDependency(int var1) throws MaltChainedException;

    public boolean isConnected();

    public boolean isProjective() throws MaltChainedException;

    public boolean isSingleHeaded();

    public boolean isTree();

    public int nNonProjectiveEdges() throws MaltChainedException;

    public void linkAllTreesToRoot() throws MaltChainedException;

    public LabelSet getDefaultRootEdgeLabels() throws MaltChainedException;

    public String getDefaultRootEdgeLabelSymbol(SymbolTable var1) throws MaltChainedException;

    public int getDefaultRootEdgeLabelCode(SymbolTable var1) throws MaltChainedException;

    public void setDefaultRootEdgeLabel(SymbolTable var1, String var2) throws MaltChainedException;

    public void setDefaultRootEdgeLabels(String var1, SortedMap<String, SymbolTable> var2) throws MaltChainedException;
}

