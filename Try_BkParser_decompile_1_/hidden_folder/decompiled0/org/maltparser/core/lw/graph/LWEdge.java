/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.lw.graph.LWDependencyGraph;
import org.maltparser.core.lw.graph.LWGraphException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.Node;

public final class LWEdge
implements Edge,
Comparable<LWEdge> {
    private final Node source;
    private final Node target;
    private final SortedMap<ColumnDescription, String> labels;

    protected LWEdge(LWEdge edge) throws LWGraphException {
        this.source = edge.source;
        this.target = edge.target;
        this.labels = new TreeMap<ColumnDescription, String>(edge.labels);
    }

    protected LWEdge(Node _source, Node _target, SortedMap<ColumnDescription, String> _labels) throws MaltChainedException {
        if (_source.getBelongsToGraph() != _target.getBelongsToGraph()) {
            throw new LWGraphException("The source node and target node must belong to the same dependency graph.");
        }
        this.source = _source;
        this.target = _target;
        this.labels = _labels;
        SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
        for (ColumnDescription column : this.labels.keySet()) {
            SymbolTable table = symbolTableHandler.addSymbolTable(column.getName());
            table.addSymbol((String)this.labels.get(column));
        }
    }

    protected LWEdge(Node _source, Node _target) throws MaltChainedException {
        if (_source.getBelongsToGraph() != _target.getBelongsToGraph()) {
            throw new LWGraphException("The source node and target node must belong to the same dependency graph.");
        }
        this.source = _source;
        this.target = _target;
        this.labels = new TreeMap<ColumnDescription, String>();
    }

    @Override
    public Node getSource() {
        return this.source;
    }

    @Override
    public Node getTarget() {
        return this.target;
    }

    public String getLabel(ColumnDescription column) {
        if (this.labels.containsKey(column)) {
            return (String)this.labels.get(column);
        }
        if (column.getCategory() == 7) {
            return column.getDefaultOutput();
        }
        return "";
    }

    @Override
    public int nLabels() {
        return this.labels.size();
    }

    @Override
    public boolean isLabeled() {
        return this.labels.size() > 0;
    }

    @Override
    public void setEdge(Node source, Node target, int type) throws MaltChainedException {
        throw new LWGraphException("Not implemented in light-weight dependency graph");
    }

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public void addLabel(SymbolTable table, String symbol) throws MaltChainedException {
        LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
        ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
        table.addSymbol(symbol);
        this.labels.put(column, symbol);
    }

    @Override
    public void addLabel(SymbolTable table, int code) throws MaltChainedException {
        this.addLabel(table, table.getSymbolCodeToString(code));
    }

    @Override
    public void addLabel(LabelSet labelSet) throws MaltChainedException {
        for (SymbolTable table : labelSet.keySet()) {
            this.addLabel(table, (Integer)labelSet.get(table));
        }
    }

    @Override
    public boolean hasLabel(SymbolTable table) throws MaltChainedException {
        if (table == null) {
            return false;
        }
        LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
        ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
        return this.labels.containsKey(column);
    }

    @Override
    public String getLabelSymbol(SymbolTable table) throws MaltChainedException {
        LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
        ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
        return (String)this.labels.get(column);
    }

    @Override
    public int getLabelCode(SymbolTable table) throws MaltChainedException {
        LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
        ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
        return table.getSymbolStringToCode((String)this.labels.get(column));
    }

    @Override
    public Set<SymbolTable> getLabelTypes() {
        HashSet<SymbolTable> labelTypes = new HashSet<SymbolTable>();
        SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
        for (ColumnDescription column : this.labels.keySet()) {
            try {
                labelTypes.add(symbolTableHandler.getSymbolTable(column.getName()));
            }
            catch (MaltChainedException e) {
                e.printStackTrace();
            }
        }
        return labelTypes;
    }

    @Override
    public LabelSet getLabelSet() {
        SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
        LabelSet labelSet = new LabelSet();
        for (ColumnDescription column : this.labels.keySet()) {
            try {
                SymbolTable table = symbolTableHandler.getSymbolTable(column.getName());
                int code = table.getSymbolStringToCode((String)this.labels.get(column));
                labelSet.put(table, code);
            }
            catch (MaltChainedException e) {
                e.printStackTrace();
            }
        }
        return labelSet;
    }

    @Override
    public void removeLabel(SymbolTable table) throws MaltChainedException {
        LWDependencyGraph graph = (LWDependencyGraph)this.getBelongsToGraph();
        ColumnDescription column = graph.getDataFormat().getColumnDescription(table.getName());
        this.labels.remove(column);
    }

    @Override
    public void removeLabels() throws MaltChainedException {
        this.labels.clear();
    }

    @Override
    public LabeledStructure getBelongsToGraph() {
        return this.target.getBelongsToGraph();
    }

    @Override
    public void setBelongsToGraph(LabeledStructure belongsToGraph) {
    }

    @Override
    public void clear() throws MaltChainedException {
        this.labels.clear();
    }

    @Override
    public int compareTo(LWEdge that) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == that) {
            return 0;
        }
        if (this.target.getIndex() < that.target.getIndex()) {
            return -1;
        }
        if (this.target.getIndex() > that.target.getIndex()) {
            return 1;
        }
        if (this.source.getIndex() < that.source.getIndex()) {
            return -1;
        }
        if (this.source.getIndex() > that.source.getIndex()) {
            return 1;
        }
        if (this.labels.equals(that.labels)) {
            return 0;
        }
        Iterator<ColumnDescription> itthis = this.labels.keySet().iterator();
        Iterator<ColumnDescription> itthat = that.labels.keySet().iterator();
        while (itthis.hasNext() && itthat.hasNext()) {
            ColumnDescription keythis = itthis.next();
            ColumnDescription keythat = itthat.next();
            if (keythis.getPosition() < keythat.getPosition()) {
                return -1;
            }
            if (keythis.getPosition() > keythat.getPosition()) {
                return 1;
            }
            if (((String)this.labels.get(keythis)).compareTo((String)that.labels.get(keythat)) == 0) continue;
            return ((String)this.labels.get(keythis)).compareTo((String)that.labels.get(keythat));
        }
        if (!itthis.hasNext() && itthat.hasNext()) {
            return -1;
        }
        return itthis.hasNext() && !itthat.hasNext();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.source.getIndex();
        result = 31 * result + this.target.getIndex();
        result = 31 * result + (this.labels == null ? 0 : this.labels.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        LWEdge other = (LWEdge)obj;
        if (this.source.getIndex() != other.source.getIndex()) {
            return false;
        }
        if (this.target.getIndex() != other.target.getIndex()) {
            return false;
        }
        return !(this.labels == null ? other.labels != null : !this.labels.equals(other.labels));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.source);
        sb.append(" -> ");
        sb.append(this.target);
        if (this.labels.size() > 0) {
            int i = 1;
            sb.append(" {");
            for (ColumnDescription column : this.labels.keySet()) {
                sb.append(column.getName());
                sb.append('=');
                sb.append((String)this.labels.get(column));
                if (i < this.labels.size()) {
                    sb.append(',');
                }
                ++i;
            }
            sb.append(" }");
        }
        return sb.toString();
    }
}

