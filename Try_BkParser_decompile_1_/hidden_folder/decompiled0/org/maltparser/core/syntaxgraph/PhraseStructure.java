/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.SecEdgeStructure;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;

public interface PhraseStructure
extends TokenStructure,
SecEdgeStructure {
    public PhraseStructureNode addTerminalNode() throws MaltChainedException;

    public PhraseStructureNode addTerminalNode(int var1) throws MaltChainedException;

    public PhraseStructureNode getTerminalNode(int var1);

    public int nTerminalNode();

    public Edge addPhraseStructureEdge(PhraseStructureNode var1, PhraseStructureNode var2) throws MaltChainedException;

    public void removePhraseStructureEdge(PhraseStructureNode var1, PhraseStructureNode var2) throws MaltChainedException;

    public int nEdges();

    public PhraseStructureNode getPhraseStructureRoot();

    public PhraseStructureNode getNonTerminalNode(int var1) throws MaltChainedException;

    public PhraseStructureNode addNonTerminalNode() throws MaltChainedException;

    public PhraseStructureNode addNonTerminalNode(int var1) throws MaltChainedException;

    public int getHighestNonTerminalIndex();

    public Set<Integer> getNonTerminalIndices();

    public boolean hasNonTerminals();

    public int nNonTerminals();

    public boolean isContinuous();

    public boolean isContinuousExcludeTerminalsAttachToRoot();
}

