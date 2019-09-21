/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.SyntaxGraph;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;

public abstract class GraphElement
extends Observable
implements Element {
    private LabeledStructure belongsToGraph = null;
    private LabelSet labelSet = null;

    @Override
    public void addLabel(SymbolTable table, String symbol) throws MaltChainedException {
        table.addSymbol(symbol);
        this.addLabel(table, table.getSymbolStringToCode(symbol));
    }

    @Override
    public void addLabel(SymbolTable table, int code) throws MaltChainedException {
        if (table.getSymbolCodeToString(code) != null) {
            if (this.labelSet == null) {
                if (this.belongsToGraph == null) {
                    throw new SyntaxGraphException("The graph element doesn't belong to any graph. ");
                }
                this.labelSet = this.belongsToGraph.checkOutNewLabelSet();
            }
            this.labelSet.put(table, code);
            this.setChanged();
            this.notifyObservers(table);
        }
    }

    @Override
    public void addLabel(LabelSet labels) throws MaltChainedException {
        if (labels != null) {
            for (SymbolTable table : labels.keySet()) {
                this.addLabel(table, (Integer)labels.get(table));
            }
        }
    }

    @Override
    public boolean hasLabel(SymbolTable table) throws MaltChainedException {
        if (this.labelSet != null) {
            return this.labelSet.containsKey(table);
        }
        return false;
    }

    @Override
    public String getLabelSymbol(SymbolTable table) throws MaltChainedException {
        Integer code = (Integer)this.labelSet.get(table);
        if (code == null) {
            throw new SyntaxGraphException("No label symbol available for label '" + table.getName() + "'.");
        }
        return table.getSymbolCodeToString(code);
    }

    @Override
    public int getLabelCode(SymbolTable table) throws MaltChainedException {
        Integer code = (Integer)this.labelSet.get(table);
        if (code == null) {
            throw new SyntaxGraphException("No label symbol available for label '" + table.getName() + "'.");
        }
        return code;
    }

    @Override
    public boolean isLabeled() {
        if (this.labelSet == null) {
            return false;
        }
        return this.labelSet.size() > 0;
    }

    @Override
    public int nLabels() {
        if (this.labelSet == null) {
            return 0;
        }
        return this.labelSet.size();
    }

    @Override
    public Set<SymbolTable> getLabelTypes() {
        if (this.labelSet == null) {
            return new LinkedHashSet<SymbolTable>();
        }
        return this.labelSet.keySet();
    }

    @Override
    public LabelSet getLabelSet() {
        return this.labelSet;
    }

    @Override
    public void removeLabel(SymbolTable table) throws MaltChainedException {
        if (this.labelSet != null) {
            this.labelSet.remove(table);
        }
    }

    @Override
    public void removeLabels() throws MaltChainedException {
        if (this.labelSet != null && this.belongsToGraph != null) {
            this.belongsToGraph.checkInLabelSet(this.labelSet);
        }
        this.labelSet = null;
    }

    @Override
    public LabeledStructure getBelongsToGraph() {
        return this.belongsToGraph;
    }

    @Override
    public void setBelongsToGraph(LabeledStructure belongsToGraph) {
        this.belongsToGraph = belongsToGraph;
        this.addObserver((SyntaxGraph)belongsToGraph);
    }

    @Override
    public void clear() throws MaltChainedException {
        if (this.labelSet != null && this.belongsToGraph != null) {
            this.belongsToGraph.checkInLabelSet(this.labelSet);
        }
        this.labelSet = null;
        this.deleteObserver((SyntaxGraph)this.belongsToGraph);
        this.belongsToGraph = null;
    }

    public boolean equals(Object obj) {
        GraphElement ge = (GraphElement)obj;
        return this.belongsToGraph == ge.getBelongsToGraph() && this.labelSet == null ? ge.getLabelSet() == null : this.labelSet.equals(ge.getLabelSet());
    }

    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == this.belongsToGraph ? 0 : this.belongsToGraph.hashCode());
        return 31 * hash + (null == this.labelSet ? 0 : this.labelSet.hashCode());
    }

    public int compareTo(GraphElement o) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == o) {
            return 0;
        }
        if (this.labelSet == null && o.labelSet != null) {
            return -1;
        }
        if (this.labelSet != null && o.labelSet == null) {
            return 1;
        }
        if (this.labelSet == null && o.labelSet == null) {
            return 0;
        }
        int comparison = 0;
        for (SymbolTable table : this.labelSet.keySet()) {
            Integer ocode = (Integer)o.labelSet.get(table);
            Integer tcode = (Integer)this.labelSet.get(table);
            if (ocode == null || tcode == null || ocode.equals(tcode)) continue;
            try {
                comparison = table.getSymbolCodeToString(tcode).compareTo(table.getSymbolCodeToString(ocode));
                if (comparison == 0) continue;
                return comparison;
            }
            catch (MaltChainedException e) {
            }
        }
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.labelSet != null) {
            for (SymbolTable table : this.labelSet.keySet()) {
                try {
                    sb.append(table.getName());
                    sb.append(':');
                    sb.append(this.getLabelSymbol(table));
                }
                catch (MaltChainedException e) {
                    System.err.println("Print error : " + e.getMessageChain());
                }
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}

