/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.Observer;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;

public abstract class SyntaxGraph
implements LabeledStructure,
Observer {
    protected SymbolTableHandler symbolTables;
    protected final ObjectPoolList<LabelSet> labelSetPool;
    protected int numberOfComponents;

    public SyntaxGraph(SymbolTableHandler symbolTables) throws MaltChainedException {
        this.symbolTables = symbolTables;
        this.labelSetPool = new ObjectPoolList<LabelSet>(){

            @Override
            protected LabelSet create() {
                return new LabelSet(6);
            }

            @Override
            public void resetObject(LabelSet o) throws MaltChainedException {
                o.clear();
            }
        };
    }

    @Override
    public SymbolTableHandler getSymbolTables() {
        return this.symbolTables;
    }

    @Override
    public void setSymbolTables(SymbolTableHandler symbolTables) {
        this.symbolTables = symbolTables;
    }

    @Override
    public void addLabel(Element element, String labelFunction, String label) throws MaltChainedException {
        element.addLabel(this.symbolTables.addSymbolTable(labelFunction), label);
    }

    @Override
    public LabelSet checkOutNewLabelSet() throws MaltChainedException {
        return this.labelSetPool.checkOut();
    }

    @Override
    public void checkInLabelSet(LabelSet labelSet) throws MaltChainedException {
        this.labelSetPool.checkIn(labelSet);
    }

    @Override
    public void clear() throws MaltChainedException {
        this.numberOfComponents = 0;
        this.labelSetPool.checkInAll();
    }

}

