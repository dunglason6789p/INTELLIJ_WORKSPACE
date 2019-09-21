/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.headrules.HeadRules;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public interface NonTerminalNode
extends PhraseStructureNode {
    public TokenNode identifyHead(HeadRules var1) throws MaltChainedException;

    public TokenNode getLexicalHead(HeadRules var1) throws MaltChainedException;

    public TokenNode getLexicalHead() throws MaltChainedException;

    public PhraseStructureNode getHeadChild(HeadRules var1) throws MaltChainedException;

    public PhraseStructureNode getHeadChild() throws MaltChainedException;

    public SortedSet<PhraseStructureNode> getChildren();

    public PhraseStructureNode getChild(int var1);

    public PhraseStructureNode getLeftChild();

    public PhraseStructureNode getRightChild();

    public int nChildren();

    public boolean hasNonTerminalChildren();

    public boolean hasTerminalChildren();

    public int getHeight();

    public boolean isContinuous();

    public boolean isContinuousExcludeTerminalsAttachToRoot();
}

