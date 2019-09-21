/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.ArrayList;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public interface TokenStructure
extends LabeledStructure {
    public TokenNode addTokenNode() throws MaltChainedException;

    public TokenNode addTokenNode(int var1) throws MaltChainedException;

    public void addComment(String var1, int var2);

    public ArrayList<String> getComment(int var1);

    public boolean hasComments();

    public TokenNode getTokenNode(int var1);

    public int nTokenNode();

    public SortedSet<Integer> getTokenIndices();

    public int getHighestTokenIndex();

    public boolean hasTokens();

    public int getSentenceID();

    public void setSentenceID(int var1);
}

