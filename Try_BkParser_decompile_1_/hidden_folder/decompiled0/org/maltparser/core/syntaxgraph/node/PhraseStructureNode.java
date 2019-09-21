/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;

public interface PhraseStructureNode
extends ComparableNode {
    public PhraseStructureNode getParent();

    public Edge getParentEdge() throws MaltChainedException;

    public String getParentEdgeLabelSymbol(SymbolTable var1) throws MaltChainedException;

    public int getParentEdgeLabelCode(SymbolTable var1) throws MaltChainedException;

    public boolean hasParentEdgeLabel(SymbolTable var1) throws MaltChainedException;
}

