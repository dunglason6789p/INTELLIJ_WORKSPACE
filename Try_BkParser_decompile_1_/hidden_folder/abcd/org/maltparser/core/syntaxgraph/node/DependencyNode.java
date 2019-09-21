/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;

public interface DependencyNode
extends ComparableNode {
    public boolean hasAtMostOneHead();

    public boolean hasHead();

    public Set<DependencyNode> getHeads() throws MaltChainedException;

    public Set<Edge> getHeadEdges() throws MaltChainedException;

    public DependencyNode getPredecessor();

    public DependencyNode getSuccessor();

    public DependencyNode getHead() throws MaltChainedException;

    public Edge getHeadEdge() throws MaltChainedException;

    public boolean hasAncestorInside(int var1, int var2) throws MaltChainedException;

    public void addHeadEdgeLabel(SymbolTable var1, String var2) throws MaltChainedException;

    public void addHeadEdgeLabel(SymbolTable var1, int var2) throws MaltChainedException;

    public void addHeadEdgeLabel(LabelSet var1) throws MaltChainedException;

    public boolean hasHeadEdgeLabel(SymbolTable var1) throws MaltChainedException;

    public String getHeadEdgeLabelSymbol(SymbolTable var1) throws MaltChainedException;

    public int getHeadEdgeLabelCode(SymbolTable var1) throws MaltChainedException;

    public boolean isHeadEdgeLabeled() throws MaltChainedException;

    public int nHeadEdgeLabels() throws MaltChainedException;

    public Set<SymbolTable> getHeadEdgeLabelTypes() throws MaltChainedException;

    public LabelSet getHeadEdgeLabelSet() throws MaltChainedException;

    public DependencyNode getAncestor() throws MaltChainedException;

    public DependencyNode getProperAncestor() throws MaltChainedException;

    public boolean hasDependent();

    public boolean hasLeftDependent();

    public DependencyNode getLeftDependent(int var1);

    public int getLeftDependentCount();

    public SortedSet<DependencyNode> getLeftDependents();

    public DependencyNode getLeftSibling() throws MaltChainedException;

    public DependencyNode getSameSideLeftSibling() throws MaltChainedException;

    public DependencyNode getClosestLeftDependent();

    public DependencyNode getLeftmostDependent();

    public DependencyNode getRightDependent(int var1);

    public int getRightDependentCount();

    public SortedSet<DependencyNode> getRightDependents();

    public DependencyNode getRightSibling() throws MaltChainedException;

    public DependencyNode getSameSideRightSibling() throws MaltChainedException;

    public DependencyNode getClosestRightDependent();

    public DependencyNode getRightmostDependent();

    public boolean hasRightDependent();

    public List<DependencyNode> getListOfDependents();

    public List<DependencyNode> getListOfLeftDependents();

    public List<DependencyNode> getListOfRightDependents();

    public boolean isProjective() throws MaltChainedException;

    public int getDependencyNodeDepth() throws MaltChainedException;

    public int getRank();

    public void setRank(int var1);

    public DependencyNode findComponent();

    public DependencyNode getComponent();

    public void setComponent(DependencyNode var1);
}

