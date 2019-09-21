/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.LabelSet;

public interface LabeledStructure {
    public SymbolTableHandler getSymbolTables();

    public void setSymbolTables(SymbolTableHandler var1);

    public void addLabel(Element var1, String var2, String var3) throws MaltChainedException;

    public LabelSet checkOutNewLabelSet() throws MaltChainedException;

    public void checkInLabelSet(LabelSet var1) throws MaltChainedException;

    public void clear() throws MaltChainedException;
}

