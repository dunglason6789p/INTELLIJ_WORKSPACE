/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent.graph;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.concurrent.graph.ConcurrentDependencyNode;
import org.maltparser.concurrent.graph.ConcurrentGraphException;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;

public final class ConcurrentDependencyEdge
implements Comparable<ConcurrentDependencyEdge> {
    private final ConcurrentDependencyNode source;
    private final ConcurrentDependencyNode target;
    private final SortedMap<Integer, String> labels;

    protected ConcurrentDependencyEdge(ConcurrentDependencyEdge edge) throws ConcurrentGraphException {
        this.source = edge.source;
        this.target = edge.target;
        this.labels = new TreeMap<Integer, String>(edge.labels);
    }

    protected ConcurrentDependencyEdge(DataFormat dataFormat, ConcurrentDependencyNode _source, ConcurrentDependencyNode _target, SortedMap<Integer, String> _labels) throws ConcurrentGraphException {
        if (_source == null) {
            throw new ConcurrentGraphException("Not allowed to have an edge without a source node");
        }
        if (_target == null) {
            throw new ConcurrentGraphException("Not allowed to have an edge without a target node");
        }
        this.source = _source;
        this.target = _target;
        if (this.target.getIndex() == 0) {
            throw new ConcurrentGraphException("Not allowed to have an edge target as root node");
        }
        this.labels = new TreeMap<Integer, String>();
        if (_labels != null) {
            for (Integer i : _labels.keySet()) {
                if (dataFormat.getColumnDescription(i).getCategory() != 3) continue;
                this.labels.put(i, (String)_labels.get(i));
            }
        }
    }

    public ConcurrentDependencyNode getSource() {
        return this.source;
    }

    public ConcurrentDependencyNode getTarget() {
        return this.target;
    }

    public String getLabel(ColumnDescription column) {
        if (this.labels.containsKey(column.getPosition())) {
            return (String)this.labels.get(column.getPosition());
        }
        if (column.getCategory() == 7) {
            return column.getDefaultOutput();
        }
        return "";
    }

    public String getLabel(String columnName) {
        ColumnDescription column = this.source.getDataFormat().getColumnDescription(columnName);
        if (column != null) {
            if (this.labels.containsKey(column.getPosition())) {
                return (String)this.labels.get(column.getPosition());
            }
            if (column.getCategory() == 7) {
                return column.getDefaultOutput();
            }
        }
        return "";
    }

    public int nLabels() {
        return this.labels.size();
    }

    public boolean isLabeled() {
        return this.labels.size() > 0;
    }

    @Override
    public int compareTo(ConcurrentDependencyEdge that) {
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
        Iterator<Integer> itthis = this.labels.keySet().iterator();
        Iterator<Integer> itthat = that.labels.keySet().iterator();
        while (itthis.hasNext() && itthat.hasNext()) {
            int keythat;
            int keythis = itthis.next();
            if (keythis < (keythat = itthat.next().intValue())) {
                return -1;
            }
            if (keythis > keythat) {
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
        result = 31 * result + (this.source == null ? 0 : this.source.hashCode());
        result = 31 * result + (this.target == null ? 0 : this.target.hashCode());
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
        ConcurrentDependencyEdge other = (ConcurrentDependencyEdge)obj;
        if (this.source == null ? other.source != null : !this.source.equals(other.source)) {
            return false;
        }
        if (this.target == null ? other.target != null : !this.target.equals(other.target)) {
            return false;
        }
        return !(this.labels == null ? other.labels != null : !this.labels.equals(other.labels));
    }
}

