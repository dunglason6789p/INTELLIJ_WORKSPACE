/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;

public interface SecEdgeStructure {
    public Edge addSecondaryEdge(ComparableNode var1, ComparableNode var2) throws MaltChainedException;

    public void removeSecondaryEdge(ComparableNode var1, ComparableNode var2) throws MaltChainedException;
}

