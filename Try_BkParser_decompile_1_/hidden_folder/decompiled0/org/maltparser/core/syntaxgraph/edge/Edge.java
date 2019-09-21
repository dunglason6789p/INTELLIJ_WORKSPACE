/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.edge;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.node.Node;

public interface Edge
extends Element {
    public static final int DEPENDENCY_EDGE = 1;
    public static final int PHRASE_STRUCTURE_EDGE = 2;
    public static final int SECONDARY_EDGE = 3;

    public void setEdge(Node var1, Node var2, int var3) throws MaltChainedException;

    public Node getSource();

    public Node getTarget();

    public int getType();
}

