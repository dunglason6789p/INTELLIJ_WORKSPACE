/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.concurrent.graph;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.concurrent.graph.ConcurrentDependencyEdge;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;
import org.maltparser.concurrent.graph.ConcurrentGraphException;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;

public final class ConcurrentDependencyNode
implements Comparable<ConcurrentDependencyNode> {
    private final ConcurrentDependencyGraph graph;
    private final int index;
    private final SortedMap<Integer, String> labels;
    private final int headIndex;

    protected ConcurrentDependencyNode(ConcurrentDependencyNode node) throws ConcurrentGraphException {
        this(node.graph, node);
    }

    protected ConcurrentDependencyNode(ConcurrentDependencyGraph _graph, ConcurrentDependencyNode node) throws ConcurrentGraphException {
        if (_graph == null) {
            throw new ConcurrentGraphException("The graph node must belong to a dependency graph.");
        }
        this.graph = _graph;
        this.index = node.index;
        this.labels = new TreeMap<Integer, String>(node.labels);
        this.headIndex = node.headIndex;
    }

    protected ConcurrentDependencyNode(ConcurrentDependencyGraph _graph, int _index, SortedMap<Integer, String> _labels, int _headIndex) throws ConcurrentGraphException {
        if (_graph == null) {
            throw new ConcurrentGraphException("The graph node must belong to a dependency graph.");
        }
        if (_index < 0) {
            throw new ConcurrentGraphException("Not allowed to have negative node index");
        }
        if (_headIndex < -1) {
            throw new ConcurrentGraphException("Not allowed to have head index less than -1.");
        }
        if (_index == 0 && _headIndex != -1) {
            throw new ConcurrentGraphException("Not allowed to add head to a root node.");
        }
        if (_index == _headIndex) {
            throw new ConcurrentGraphException("Not allowed to add head to itself");
        }
        this.graph = _graph;
        this.index = _index;
        this.labels = new TreeMap<Integer, String>(_labels);
        this.headIndex = _headIndex;
    }

    protected ConcurrentDependencyNode(ConcurrentDependencyGraph _graph, int _index, String[] _labels) throws ConcurrentGraphException {
        if (_graph == null) {
            throw new ConcurrentGraphException("The graph node must belong to a dependency graph.");
        }
        if (_index < 0) {
            throw new ConcurrentGraphException("Not allowed to have negative node index");
        }
        this.graph = _graph;
        this.index = _index;
        this.labels = new TreeMap<Integer, String>();
        int tmpHeadIndex = -1;
        if (_labels != null) {
            for (int i = 0; i < _labels.length; ++i) {
                int columnCategory = this.graph.getDataFormat().getColumnDescription(i).getCategory();
                if (columnCategory == 2) {
                    tmpHeadIndex = Integer.parseInt(_labels[i]);
                    continue;
                }
                if (columnCategory != 1 && columnCategory != 3) continue;
                this.labels.put(i, _labels[i]);
            }
        }
        this.headIndex = tmpHeadIndex;
        if (this.headIndex < -1) {
            throw new ConcurrentGraphException("Not allowed to have head index less than -1.");
        }
        if (this.index == 0 && this.headIndex != -1) {
            throw new ConcurrentGraphException("Not allowed to add head to a root node.");
        }
        if (this.index == this.headIndex) {
            throw new ConcurrentGraphException("Not allowed to add head to itself");
        }
    }

    public int getIndex() {
        return this.index;
    }

    public String getLabel(int columnPosition) {
        if (this.labels.containsKey(columnPosition)) {
            return (String)this.labels.get(columnPosition);
        }
        if (this.graph.getDataFormat().getColumnDescription(columnPosition).getCategory() == 7) {
            return this.graph.getDataFormat().getColumnDescription(columnPosition).getDefaultOutput();
        }
        return "";
    }

    public String getLabel(String columnName) {
        ColumnDescription column = this.graph.getDataFormat().getColumnDescription(columnName);
        if (column != null) {
            return this.getLabel(column.getPosition());
        }
        return "";
    }

    public String getLabel(ColumnDescription column) {
        return this.getLabel(column.getPosition());
    }

    public boolean hasLabel(int columnPosition) {
        return this.labels.containsKey(columnPosition);
    }

    public boolean hasLabel(String columnName) {
        ColumnDescription column = this.graph.getDataFormat().getColumnDescription(columnName);
        if (column != null) {
            return this.hasLabel(column.getPosition());
        }
        return false;
    }

    public boolean hasLabel(ColumnDescription column) {
        return this.labels.containsKey(column.getPosition());
    }

    public boolean isLabeled() {
        for (Integer key : this.labels.keySet()) {
            if (this.graph.getDataFormat().getColumnDescription(key).getCategory() != 1) continue;
            return true;
        }
        return false;
    }

    public boolean isHeadLabeled() {
        for (Integer key : this.labels.keySet()) {
            if (this.graph.getDataFormat().getColumnDescription(key).getCategory() != 3) continue;
            return true;
        }
        return false;
    }

    public int getHeadIndex() {
        return this.headIndex;
    }

    public SortedMap<ColumnDescription, String> getNodeLabels() {
        SortedMap<ColumnDescription, String> nodeLabels = Collections.synchronizedSortedMap(new TreeMap());
        for (Integer key : this.labels.keySet()) {
            if (this.graph.getDataFormat().getColumnDescription(key).getCategory() != 1) continue;
            nodeLabels.put(this.graph.getDataFormat().getColumnDescription(key), (String)this.labels.get(key));
        }
        return nodeLabels;
    }

    public SortedMap<ColumnDescription, String> getEdgeLabels() {
        SortedMap<ColumnDescription, String> edgeLabels = Collections.synchronizedSortedMap(new TreeMap());
        for (Integer key : this.labels.keySet()) {
            if (this.graph.getDataFormat().getColumnDescription(key).getCategory() != 3) continue;
            edgeLabels.put(this.graph.getDataFormat().getColumnDescription(key), (String)this.labels.get(key));
        }
        return edgeLabels;
    }

    public ConcurrentDependencyNode getPredecessor() {
        return this.index > 1 ? this.graph.getDependencyNode(this.index - 1) : null;
    }

    public ConcurrentDependencyNode getSuccessor() {
        return this.graph.getDependencyNode(this.index + 1);
    }

    public boolean isRoot() {
        return this.index == 0;
    }

    public boolean hasAtMostOneHead() {
        return true;
    }

    public boolean hasHead() {
        return this.headIndex != -1;
    }

    public boolean hasDependent() {
        return this.graph.hasDependent(this.index);
    }

    public boolean hasLeftDependent() {
        return this.graph.hasLeftDependent(this.index);
    }

    public boolean hasRightDependent() {
        return this.graph.hasRightDependent(this.index);
    }

    public SortedSet<ConcurrentDependencyNode> getHeads() {
        SortedSet<ConcurrentDependencyNode> heads = Collections.synchronizedSortedSet(new TreeSet());
        ConcurrentDependencyNode head = this.getHead();
        if (head != null) {
            heads.add(head);
        }
        return heads;
    }

    public ConcurrentDependencyNode getHead() {
        return this.graph.getDependencyNode(this.headIndex);
    }

    public ConcurrentDependencyNode getLeftDependent(int leftDependentIndex) {
        List<ConcurrentDependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
        if (leftDependentIndex >= 0 && leftDependentIndex < leftDependents.size()) {
            return leftDependents.get(leftDependentIndex);
        }
        return null;
    }

    public int getLeftDependentCount() {
        return this.graph.getListOfLeftDependents(this.index).size();
    }

    public SortedSet<ConcurrentDependencyNode> getLeftDependents() {
        return this.graph.getSortedSetOfLeftDependents(this.index);
    }

    public List<ConcurrentDependencyNode> getListOfLeftDependents() {
        return this.graph.getListOfLeftDependents(this.index);
    }

    public ConcurrentDependencyNode getLeftSibling() {
        if (this.headIndex == -1) {
            return null;
        }
        int nodeDepedentPosition = 0;
        List<ConcurrentDependencyNode> headDependents = this.getHead().getListOfDependents();
        for (int i = 0; i < headDependents.size(); ++i) {
            if (headDependents.get(i).getIndex() != this.index) continue;
            nodeDepedentPosition = i;
            break;
        }
        return nodeDepedentPosition > 0 ? headDependents.get(nodeDepedentPosition - 1) : null;
    }

    public ConcurrentDependencyNode getSameSideLeftSibling() {
        if (this.headIndex == -1) {
            return null;
        }
        List<ConcurrentDependencyNode> headDependents = this.index < this.headIndex ? this.getHead().getListOfLeftDependents() : this.getHead().getListOfRightDependents();
        int nodeDepedentPosition = 0;
        for (int i = 0; i < headDependents.size(); ++i) {
            if (headDependents.get(i).getIndex() != this.index) continue;
            nodeDepedentPosition = i;
            break;
        }
        return nodeDepedentPosition > 0 ? headDependents.get(nodeDepedentPosition - 1) : null;
    }

    public ConcurrentDependencyNode getClosestLeftDependent() {
        List<ConcurrentDependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
        return leftDependents.size() > 0 ? leftDependents.get(leftDependents.size() - 1) : null;
    }

    public ConcurrentDependencyNode getLeftmostDependent() {
        List<ConcurrentDependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
        return leftDependents.size() > 0 ? leftDependents.get(0) : null;
    }

    public ConcurrentDependencyNode getRightDependent(int rightDependentIndex) {
        List<ConcurrentDependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
        if (rightDependentIndex >= 0 && rightDependentIndex < rightDependents.size()) {
            return rightDependents.get(rightDependents.size() - 1 - rightDependentIndex);
        }
        return null;
    }

    public int getRightDependentCount() {
        return this.graph.getListOfRightDependents(this.index).size();
    }

    public SortedSet<ConcurrentDependencyNode> getRightDependents() {
        return this.graph.getSortedSetOfRightDependents(this.index);
    }

    public List<ConcurrentDependencyNode> getListOfRightDependents() {
        return this.graph.getListOfRightDependents(this.index);
    }

    public ConcurrentDependencyNode getRightSibling() {
        if (this.headIndex == -1) {
            return null;
        }
        List<ConcurrentDependencyNode> headDependents = this.getHead().getListOfDependents();
        int nodeDepedentPosition = headDependents.size() - 1;
        for (int i = headDependents.size() - 1; i >= 0; --i) {
            if (headDependents.get(i).getIndex() != this.index) continue;
            nodeDepedentPosition = i;
            break;
        }
        return nodeDepedentPosition < headDependents.size() - 1 ? headDependents.get(nodeDepedentPosition + 1) : null;
    }

    public ConcurrentDependencyNode getSameSideRightSibling() {
        if (this.headIndex == -1) {
            return null;
        }
        List<ConcurrentDependencyNode> headDependents = this.index < this.headIndex ? this.getHead().getListOfLeftDependents() : this.getHead().getListOfRightDependents();
        int nodeDepedentPosition = headDependents.size() - 1;
        for (int i = headDependents.size() - 1; i >= 0; --i) {
            if (headDependents.get(i).getIndex() != this.index) continue;
            nodeDepedentPosition = i;
            break;
        }
        return nodeDepedentPosition < headDependents.size() - 1 ? headDependents.get(nodeDepedentPosition + 1) : null;
    }

    public ConcurrentDependencyNode getClosestRightDependent() {
        List<ConcurrentDependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
        return rightDependents.size() > 0 ? rightDependents.get(0) : null;
    }

    public ConcurrentDependencyNode getRightmostDependent() {
        List<ConcurrentDependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
        return rightDependents.size() > 0 ? rightDependents.get(rightDependents.size() - 1) : null;
    }

    public SortedSet<ConcurrentDependencyNode> getDependents() {
        return this.graph.getSortedSetOfDependents(this.index);
    }

    public List<ConcurrentDependencyNode> getListOfDependents() {
        return this.graph.getListOfDependents(this.index);
    }

    public int getInDegree() {
        return this.hasHead();
    }

    public int getOutDegree() {
        return this.graph.getListOfDependents(this.index).size();
    }

    public ConcurrentDependencyNode getAncestor() {
        if (!this.hasHead()) {
            return this;
        }
        ConcurrentDependencyNode tmp = this;
        while (tmp.hasHead()) {
            tmp = tmp.getHead();
        }
        return tmp;
    }

    public ConcurrentDependencyNode getProperAncestor() {
        if (!this.hasHead()) {
            return null;
        }
        ConcurrentDependencyNode tmp = this;
        while (tmp.hasHead() && !tmp.isRoot()) {
            tmp = tmp.getHead();
        }
        return tmp;
    }

    public boolean hasAncestorInside(int left, int right) {
        if (this.index == 0) {
            return false;
        }
        ConcurrentDependencyNode tmp = this;
        return tmp.getHead() != null && (tmp = tmp.getHead()).getIndex() >= left && tmp.getIndex() <= right;
    }

    public boolean isProjective() {
        if (this.headIndex > 0) {
            ConcurrentDependencyNode head = this.getHead();
            if (this.headIndex < this.index) {
                ConcurrentDependencyNode terminals = head;
                ConcurrentDependencyNode tmp = null;
                block0 : do {
                    if (terminals == null || terminals.getSuccessor() == null) {
                        return false;
                    }
                    if (terminals.getSuccessor() != this) {
                        tmp = terminals = terminals.getSuccessor();
                        do {
                            if (tmp == this || tmp == head) continue block0;
                            if (!tmp.hasHead()) {
                                return false;
                            }
                            tmp = tmp.getHead();
                        } while (true);
                    }
                    break;
                } while (true);
            } else {
                ConcurrentDependencyNode terminals = this;
                ConcurrentDependencyNode tmp = null;
                block2 : do {
                    if (terminals == null || terminals.getSuccessor() == null) {
                        return false;
                    }
                    if (terminals.getSuccessor() == head) break;
                    tmp = terminals = terminals.getSuccessor();
                    do {
                        if (tmp == this || tmp == head) continue block2;
                        if (!tmp.hasHead()) {
                            return false;
                        }
                        tmp = tmp.getHead();
                    } while (true);
                    break;
                } while (true);
            }
        }
        return true;
    }

    public int getDependencyNodeDepth() {
        ConcurrentDependencyNode tmp = this;
        int depth = 0;
        while (tmp.hasHead()) {
            ++depth;
            tmp = tmp.getHead();
        }
        return depth;
    }

    public ConcurrentDependencyNode getLeftmostProperDescendant() {
        ConcurrentDependencyNode candidate = null;
        List<ConcurrentDependencyNode> dependents = this.graph.getListOfDependents(this.index);
        for (int i = 0; i < dependents.size(); ++i) {
            ConcurrentDependencyNode tmp;
            ConcurrentDependencyNode dep = dependents.get(i);
            if (candidate == null || dep.getIndex() < candidate.getIndex()) {
                candidate = dep;
            }
            if ((tmp = dep.getLeftmostProperDescendant()) == null) continue;
            if (candidate == null || tmp.getIndex() < candidate.getIndex()) {
                candidate = tmp;
            }
            if (candidate.getIndex() != 1) continue;
            return candidate;
        }
        return candidate;
    }

    public ConcurrentDependencyNode getRightmostProperDescendant() {
        ConcurrentDependencyNode candidate = null;
        List<ConcurrentDependencyNode> dependents = this.graph.getListOfDependents(this.index);
        for (int i = 0; i < dependents.size(); ++i) {
            ConcurrentDependencyNode tmp;
            ConcurrentDependencyNode dep = dependents.get(i);
            if (candidate == null || dep.getIndex() > candidate.getIndex()) {
                candidate = dep;
            }
            if ((tmp = dep.getRightmostProperDescendant()) == null || candidate != null && tmp.getIndex() <= candidate.getIndex()) continue;
            candidate = tmp;
        }
        return candidate;
    }

    public int getLeftmostProperDescendantIndex() {
        ConcurrentDependencyNode node = this.getLeftmostProperDescendant();
        return node != null ? node.getIndex() : -1;
    }

    public int getRightmostProperDescendantIndex() {
        ConcurrentDependencyNode node = this.getRightmostProperDescendant();
        return node != null ? node.getIndex() : -1;
    }

    public ConcurrentDependencyNode getLeftmostDescendant() {
        ConcurrentDependencyNode candidate = this;
        List<ConcurrentDependencyNode> dependents = this.graph.getListOfDependents(this.index);
        for (int i = 0; i < dependents.size(); ++i) {
            ConcurrentDependencyNode tmp;
            ConcurrentDependencyNode dep = dependents.get(i);
            if (dep.getIndex() < candidate.getIndex()) {
                candidate = dep;
            }
            if ((tmp = dep.getLeftmostDescendant()) == null) continue;
            if (tmp.getIndex() < candidate.getIndex()) {
                candidate = tmp;
            }
            if (candidate.getIndex() != 1) continue;
            return candidate;
        }
        return candidate;
    }

    public ConcurrentDependencyNode getRightmostDescendant() {
        ConcurrentDependencyNode candidate = this;
        List<ConcurrentDependencyNode> dependents = this.graph.getListOfDependents(this.index);
        for (int i = 0; i < dependents.size(); ++i) {
            ConcurrentDependencyNode tmp;
            ConcurrentDependencyNode dep = dependents.get(i);
            if (dep.getIndex() > candidate.getIndex()) {
                candidate = dep;
            }
            if ((tmp = dep.getRightmostDescendant()) == null || tmp.getIndex() <= candidate.getIndex()) continue;
            candidate = tmp;
        }
        return candidate;
    }

    public int getLeftmostDescendantIndex() {
        ConcurrentDependencyNode node = this.getLeftmostDescendant();
        return node != null ? node.getIndex() : this.getIndex();
    }

    public int getRightmostDescendantIndex() {
        ConcurrentDependencyNode node = this.getRightmostDescendant();
        return node != null ? node.getIndex() : this.getIndex();
    }

    public ConcurrentDependencyNode findComponent() {
        return this.graph.findComponent(this.index);
    }

    public int getRank() {
        return this.graph.getRank(this.index);
    }

    public ConcurrentDependencyEdge getHeadEdge() throws ConcurrentGraphException {
        if (!this.hasHead()) {
            return null;
        }
        return new ConcurrentDependencyEdge(this.graph.getDataFormat(), this.getHead(), this, this.labels);
    }

    public SortedSet<ConcurrentDependencyEdge> getHeadEdges() throws ConcurrentGraphException {
        SortedSet<ConcurrentDependencyEdge> edges = Collections.synchronizedSortedSet(new TreeSet());
        if (this.hasHead()) {
            edges.add(new ConcurrentDependencyEdge(this.graph.getDataFormat(), this.getHead(), this, this.labels));
        }
        return edges;
    }

    public boolean isHeadEdgeLabeled() {
        return this.getEdgeLabels().size() > 0;
    }

    public int nHeadEdgeLabels() {
        return this.getEdgeLabels().size();
    }

    public DataFormat getDataFormat() {
        return this.graph.getDataFormat();
    }

    @Override
    public int compareTo(ConcurrentDependencyNode that) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == that) {
            return 0;
        }
        if (this.index < that.getIndex()) {
            return -1;
        }
        return this.index > that.getIndex();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.headIndex;
        result = 31 * result + this.index;
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
        ConcurrentDependencyNode other = (ConcurrentDependencyNode)obj;
        if (this.headIndex != other.headIndex) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        return !(this.labels == null ? other.labels != null : !this.labels.equals(other.labels));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.graph.getDataFormat().numberOfColumns(); ++i) {
            ColumnDescription column = this.graph.getDataFormat().getColumnDescription(i);
            if (column.isInternal()) continue;
            if (column.getCategory() == 2) {
                sb.append(this.headIndex);
            } else if (column.getCategory() == 1 || column.getCategory() == 3) {
                sb.append((String)this.labels.get(column.getPosition()));
            } else if (column.getCategory() == 7) {
                sb.append(column.getDefaultOutput());
            }
            sb.append('\t');
        }
        sb.setLength(sb.length() > 0 ? sb.length() - 1 : 0);
        return sb.toString();
    }
}

