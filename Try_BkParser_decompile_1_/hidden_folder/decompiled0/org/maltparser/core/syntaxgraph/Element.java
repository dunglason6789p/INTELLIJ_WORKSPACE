/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;

public interface Element {
    public void addLabel(SymbolTable var1, String var2) throws MaltChainedException;

    public void addLabel(SymbolTable var1, int var2) throws MaltChainedException;

    public void addLabel(LabelSet var1) throws MaltChainedException;

    public boolean hasLabel(SymbolTable var1) throws MaltChainedException;

    public String getLabelSymbol(SymbolTable var1) throws MaltChainedException;

    public int getLabelCode(SymbolTable var1) throws MaltChainedException;

    public boolean isLabeled();

    public int nLabels();

    public Set<SymbolTable> getLabelTypes();

    public LabelSet getLabelSet();

    public void removeLabel(SymbolTable var1) throws MaltChainedException;

    public void removeLabels() throws MaltChainedException;

    public LabeledStructure getBelongsToGraph();

    public void setBelongsToGraph(LabeledStructure var1);

    public void clear() throws MaltChainedException;
}

