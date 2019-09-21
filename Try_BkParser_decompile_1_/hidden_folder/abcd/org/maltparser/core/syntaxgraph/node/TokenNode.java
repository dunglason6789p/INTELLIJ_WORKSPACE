/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

public interface TokenNode
extends DependencyNode,
PhraseStructureNode {
    public void setPredecessor(TokenNode var1);

    public void setSuccessor(TokenNode var1);

    public TokenNode getTokenNodePredecessor();

    public TokenNode getTokenNodeSuccessor();
}

