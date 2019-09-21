/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.propagation;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.io.dataformat.DataFormatInstance;
import org.maltparser.core.propagation.PropagationException;
import org.maltparser.core.propagation.spec.PropagationSpec;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;

public class Propagation {
    private SymbolTable fromTable;
    private SymbolTable toTable;
    private SymbolTable deprelTable;
    private final SortedSet<String> forSet;
    private final SortedSet<String> overSet;
    private final Pattern symbolSeparator;

    public Propagation(PropagationSpec spec, DataFormatInstance dataFormatInstance, SymbolTableHandler tableHandler) throws MaltChainedException {
        String[] items;
        ColumnDescription fromColumn = dataFormatInstance.getColumnDescriptionByName(spec.getFrom());
        if (fromColumn == null) {
            throw new PropagationException("The symbol table '" + spec.getFrom() + " does not exists.");
        }
        this.fromTable = tableHandler.getSymbolTable(spec.getFrom());
        ColumnDescription toColumn = dataFormatInstance.getColumnDescriptionByName(spec.getTo());
        if (toColumn == null) {
            toColumn = dataFormatInstance.addInternalColumnDescription(tableHandler, spec.getTo(), fromColumn);
            this.toTable = tableHandler.getSymbolTable(spec.getTo());
        }
        this.forSet = new TreeSet<String>();
        if (spec.getFor() != null && spec.getFor().length() > 0) {
            for (String item : items = spec.getFor().split("\\|")) {
                this.forSet.add(item);
            }
        }
        this.overSet = new TreeSet<String>();
        if (spec.getOver() != null && spec.getOver().length() > 0) {
            for (String item : items = spec.getOver().split("\\|")) {
                this.overSet.add(item);
            }
        }
        this.deprelTable = tableHandler.getSymbolTable("DEPREL");
        this.symbolSeparator = Pattern.compile("\\|");
    }

    public void propagate(Edge e) throws MaltChainedException {
        if (e != null && e.hasLabel(this.deprelTable) && !e.getSource().isRoot() && (this.overSet.size() == 0 || this.overSet.contains(e.getLabelSymbol(this.deprelTable)))) {
            DependencyNode to = (DependencyNode)((Object)e.getSource());
            DependencyNode from = (DependencyNode)((Object)e.getTarget());
            String fromSymbol = null;
            if (e.hasLabel(this.fromTable)) {
                fromSymbol = e.getLabelSymbol(this.fromTable);
            } else if (from.hasLabel(this.fromTable)) {
                fromSymbol = from.getLabelSymbol(this.fromTable);
            }
            String propSymbol = null;
            if (to.hasLabel(this.toTable)) {
                propSymbol = this.union(fromSymbol, to.getLabelSymbol(this.toTable));
            } else if (this.forSet.size() == 0 || this.forSet.contains(fromSymbol)) {
                propSymbol = fromSymbol;
            }
            if (propSymbol != null) {
                to.addLabel(this.toTable, propSymbol);
            }
        }
    }

    private String union(String fromSymbol, String toSymbol) {
        int i;
        TreeSet<String> symbolSet = new TreeSet<String>();
        if (fromSymbol != null && fromSymbol.length() != 0) {
            String[] fromSymbols = this.symbolSeparator.split(fromSymbol);
            for (i = 0; i < fromSymbols.length; ++i) {
                if (this.forSet.size() != 0 && !this.forSet.contains(fromSymbols[i])) continue;
                symbolSet.add(fromSymbols[i]);
            }
        }
        if (toSymbol != null && toSymbol.length() != 0) {
            String[] toSymbols = this.symbolSeparator.split(toSymbol);
            for (i = 0; i < toSymbols.length; ++i) {
                symbolSet.add(toSymbols[i]);
            }
        }
        if (symbolSet.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String symbol : symbolSet) {
                sb.append(symbol);
                sb.append('|');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
        return "";
    }

    public String toString() {
        return "Propagation [forSet=" + this.forSet + ", fromTable=" + this.fromTable + ", overSet=" + this.overSet + ", toTable=" + this.toTable + "]";
    }
}

