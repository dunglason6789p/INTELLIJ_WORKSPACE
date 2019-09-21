/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.lw.graph.LWDependencyGraph;
import org.maltparser.core.lw.graph.LWEdge;
import org.maltparser.core.lw.graph.LWGraphException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;

public final class LWNode
implements DependencyNode,
Node {
    private final LWDependencyGraph graph;
    private int index;
    private final Map<Integer, String> labels;
    private Edge headEdge;

    protected LWNode(LWNode node) throws LWGraphException {
        this(node.graph, node);
    }

    protected LWNode(LWDependencyGraph _graph, LWNode node) throws LWGraphException {
        if (_graph == null) {
            throw new LWGraphException("The graph node must belong to a dependency graph.");
        }
        this.graph = _graph;
        this.index = node.index;
        this.labels = new HashMap<Integer, String>(node.labels);
        this.headEdge = node.headEdge;
    }

    protected LWNode(LWDependencyGraph _graph, int _index) throws LWGraphException {
        if (_graph == null) {
            throw new LWGraphException("The graph node must belong to a dependency graph.");
        }
        if (_index < 0) {
            throw new LWGraphException("Not allowed to have negative node index");
        }
        this.graph = _graph;
        this.index = _index;
        this.labels = new HashMap<Integer, String>();
        this.headEdge = null;
    }

    protected DependencyStructure getGraph() {
        return this.graph;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public void setIndex(int index) throws MaltChainedException {
        this.index = index;
    }

    public String getLabel(int columnPosition) {
        if (this.labels.containsKey(columnPosition)) {
            return this.labels.get(columnPosition);
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

    @Override
    public boolean isLabeled() {
        for (Integer key : this.labels.keySet()) {
            if (this.graph.getDataFormat().getColumnDescription(key).getCategory() != 1) continue;
            return true;
        }
        return false;
    }

    public boolean isHeadLabeled() {
        if (this.headEdge == null) {
            return false;
        }
        return this.headEdge.isLabeled();
    }

    public int getHeadIndex() {
        if (this.headEdge == null) {
            return -1;
        }
        return this.headEdge.getSource().getIndex();
    }

    public SortedMap<ColumnDescription, String> getLabels() {
        SortedMap<ColumnDescription, String> nodeLabels = Collections.synchronizedSortedMap(new TreeMap());
        for (Integer key : this.labels.keySet()) {
            nodeLabels.put(this.graph.getDataFormat().getColumnDescription(key), this.labels.get(key));
        }
        return nodeLabels;
    }

    @Override
    public DependencyNode getPredecessor() {
        return this.index > 1 ? this.graph.getNode(this.index - 1) : null;
    }

    @Override
    public DependencyNode getSuccessor() {
        return this.graph.getNode(this.index + 1);
    }

    @Override
    public boolean isRoot() {
        return this.index == 0;
    }

    @Override
    public boolean hasAtMostOneHead() {
        return true;
    }

    @Override
    public boolean hasHead() {
        return this.headEdge != null;
    }

    @Override
    public boolean hasDependent() {
        return this.graph.hasDependent(this.index);
    }

    @Override
    public boolean hasLeftDependent() {
        return this.graph.hasLeftDependent(this.index);
    }

    @Override
    public boolean hasRightDependent() {
        return this.graph.hasRightDependent(this.index);
    }

    public SortedSet<DependencyNode> getHeads() {
        SortedSet<DependencyNode> heads = Collections.synchronizedSortedSet(new TreeSet());
        DependencyNode head = this.getHead();
        if (head != null) {
            heads.add(head);
        }
        return heads;
    }

    @Override
    public DependencyNode getHead() {
        if (this.headEdge == null) {
            return null;
        }
        return this.graph.getNode(this.getHeadIndex());
    }

    @Override
    public DependencyNode getLeftDependent(int leftDependentIndex) {
        List<DependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
        if (leftDependentIndex >= 0 && leftDependentIndex < leftDependents.size()) {
            return leftDependents.get(leftDependentIndex);
        }
        return null;
    }

    @Override
    public int getLeftDependentCount() {
        return this.graph.getListOfLeftDependents(this.index).size();
    }

    @Override
    public SortedSet<DependencyNode> getLeftDependents() {
        return this.graph.getSortedSetOfLeftDependents(this.index);
    }

    @Override
    public List<DependencyNode> getListOfLeftDependents() {
        return this.graph.getListOfLeftDependents(this.index);
    }

    @Override
    public DependencyNode getLeftSibling() {
        if (this.headEdge == null) {
            return null;
        }
        int nodeDepedentPosition = 0;
        List<DependencyNode> headDependents = this.getHead().getListOfDependents();
        for (int i = 0; i < headDependents.size(); ++i) {
            if (headDependents.get(i).getIndex() != this.index) continue;
            nodeDepedentPosition = i;
            break;
        }
        return nodeDepedentPosition > 0 ? headDependents.get(nodeDepedentPosition - 1) : null;
    }

    @Override
    public DependencyNode getSameSideLeftSibling() {
        if (this.headEdge == null) {
            return null;
        }
        List<DependencyNode> headDependents = this.index < this.getHeadIndex() ? this.getHead().getListOfLeftDependents() : this.getHead().getListOfRightDependents();
        int nodeDepedentPosition = 0;
        for (int i = 0; i < headDependents.size(); ++i) {
            if (headDependents.get(i).getIndex() != this.index) continue;
            nodeDepedentPosition = i;
            break;
        }
        return nodeDepedentPosition > 0 ? headDependents.get(nodeDepedentPosition - 1) : null;
    }

    @Override
    public DependencyNode getClosestLeftDependent() {
        List<DependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
        return leftDependents.size() > 0 ? leftDependents.get(leftDependents.size() - 1) : null;
    }

    @Override
    public DependencyNode getLeftmostDependent() {
        List<DependencyNode> leftDependents = this.graph.getListOfLeftDependents(this.index);
        return leftDependents.size() > 0 ? leftDependents.get(0) : null;
    }

    @Override
    public DependencyNode getRightDependent(int rightDependentIndex) {
        List<DependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
        if (rightDependentIndex >= 0 && rightDependentIndex < rightDependents.size()) {
            return rightDependents.get(rightDependents.size() - 1 - rightDependentIndex);
        }
        return null;
    }

    @Override
    public int getRightDependentCount() {
        return this.graph.getListOfRightDependents(this.index).size();
    }

    @Override
    public SortedSet<DependencyNode> getRightDependents() {
        return this.graph.getSortedSetOfRightDependents(this.index);
    }

    @Override
    public List<DependencyNode> getListOfRightDependents() {
        return this.graph.getListOfRightDependents(this.index);
    }

    @Override
    public DependencyNode getRightSibling() {
        if (this.headEdge == null) {
            return null;
        }
        List<DependencyNode> headDependents = this.getHead().getListOfDependents();
        int nodeDepedentPosition = headDependents.size() - 1;
        for (int i = headDependents.size() - 1; i >= 0; --i) {
            if (headDependents.get(i).getIndex() != this.index) continue;
            nodeDepedentPosition = i;
            break;
        }
        return nodeDepedentPosition < headDependents.size() - 1 ? headDependents.get(nodeDepedentPosition + 1) : null;
    }

    @Override
    public DependencyNode getSameSideRightSibling() {
        if (this.headEdge == null) {
            return null;
        }
        List<DependencyNode> headDependents = this.index < this.getHeadIndex() ? this.getHead().getListOfLeftDependents() : this.getHead().getListOfRightDependents();
        int nodeDepedentPosition = headDependents.size() - 1;
        for (int i = headDependents.size() - 1; i >= 0; --i) {
            if (headDependents.get(i).getIndex() != this.index) continue;
            nodeDepedentPosition = i;
            break;
        }
        return nodeDepedentPosition < headDependents.size() - 1 ? headDependents.get(nodeDepedentPosition + 1) : null;
    }

    @Override
    public DependencyNode getClosestRightDependent() {
        List<DependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
        return rightDependents.size() > 0 ? rightDependents.get(0) : null;
    }

    @Override
    public DependencyNode getRightmostDependent() {
        List<DependencyNode> rightDependents = this.graph.getListOfRightDependents(this.index);
        return rightDependents.size() > 0 ? rightDependents.get(rightDependents.size() - 1) : null;
    }

    public SortedSet<DependencyNode> getDependents() {
        return this.graph.getSortedSetOfDependents(this.index);
    }

    @Override
    public List<DependencyNode> getListOfDependents() {
        return this.graph.getListOfDependents(this.index);
    }

    @Override
    public int getInDegree() {
        return this.hasHead();
    }

    @Override
    public int getOutDegree() {
        return this.graph.getListOfDependents(this.index).size();
    }

    @Override
    public DependencyNode getAncestor() throws MaltChainedException {
        if (!this.hasHead()) {
            return this;
        }
        DependencyNode tmp = this;
        while (tmp.hasHead()) {
            tmp = tmp.getHead();
        }
        return tmp;
    }

    @Override
    public DependencyNode getProperAncestor() throws MaltChainedException {
        if (!this.hasHead()) {
            return null;
        }
        DependencyNode tmp = this;
        while (tmp.hasHead() && !tmp.isRoot()) {
            tmp = tmp.getHead();
        }
        return tmp;
    }

    @Override
    public boolean hasAncestorInside(int left, int right) throws MaltChainedException {
        if (this.index == 0) {
            return false;
        }
        DependencyNode tmp = this;
        return tmp.getHead() != null && (tmp = tmp.getHead()).getIndex() >= left && tmp.getIndex() <= right;
    }

    @Override
    public boolean isProjective() throws MaltChainedException {
        int headIndex = this.getHeadIndex();
        if (headIndex > 0) {
            DependencyNode head = this.getHead();
            if (headIndex < this.index) {
                DependencyNode terminals = head;
                DependencyNode tmp = null;
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
                DependencyNode terminals = this;
                DependencyNode tmp = null;
                block2 : do {
                    if (terminals == null || terminals.getSuccessor() == null) {
                        return false;
                    }
                    if (terminals.getSuccessor() == head) break;
                    terminals = terminals.getSuccessor();
                    tmp = terminals;
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

    @Override
    public int getDependencyNodeDepth() throws MaltChainedException {
        DependencyNode tmp = this;
        int depth = 0;
        while (tmp.hasHead()) {
            ++depth;
            tmp = tmp.getHead();
        }
        return depth;
    }

    @Override
    public int getCompareToIndex() {
        return this.index;
    }

    @Override
    public ComparableNode getLeftmostProperDescendant() throws MaltChainedException {
        ComparableNode candidate = null;
        List<DependencyNode> dependents = this.graph.getListOfDependents(this.index);
        for (int i = 0; i < dependents.size(); ++i) {
            ComparableNode tmp;
            DependencyNode dep = dependents.get(i);
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

    @Override
    public ComparableNode getRightmostProperDescendant() throws MaltChainedException {
        ComparableNode candidate = null;
        List<DependencyNode> dependents = this.graph.getListOfDependents(this.index);
        for (int i = 0; i < dependents.size(); ++i) {
            ComparableNode tmp;
            DependencyNode dep = dependents.get(i);
            if (candidate == null || dep.getIndex() > candidate.getIndex()) {
                candidate = dep;
            }
            if ((tmp = dep.getRightmostProperDescendant()) == null || candidate != null && tmp.getIndex() <= candidate.getIndex()) continue;
            candidate = tmp;
        }
        return candidate;
    }

    @Override
    public int getLeftmostProperDescendantIndex() throws MaltChainedException {
        ComparableNode node = this.getLeftmostProperDescendant();
        return node != null ? node.getIndex() : -1;
    }

    @Override
    public int getRightmostProperDescendantIndex() throws MaltChainedException {
        ComparableNode node = this.getRightmostProperDescendant();
        return node != null ? node.getIndex() : -1;
    }

    @Override
    public ComparableNode getLeftmostDescendant() throws MaltChainedException {
        ComparableNode candidate = this;
        List<DependencyNode> dependents = this.graph.getListOfDependents(this.index);
        for (int i = 0; i < dependents.size(); ++i) {
            ComparableNode tmp;
            DependencyNode dep = dependents.get(i);
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

    @Override
    public ComparableNode getRightmostDescendant() throws MaltChainedException {
        ComparableNode candidate = this;
        List<DependencyNode> dependents = this.graph.getListOfDependents(this.index);
        for (int i = 0; i < dependents.size(); ++i) {
            ComparableNode tmp;
            DependencyNode dep = dependents.get(i);
            if (dep.getIndex() > candidate.getIndex()) {
                candidate = dep;
            }
            if ((tmp = dep.getRightmostDescendant()) == null || tmp.getIndex() <= candidate.getIndex()) continue;
            candidate = tmp;
        }
        return candidate;
    }

    @Override
    public int getLeftmostDescendantIndex() throws MaltChainedException {
        ComparableNode node = this.getLeftmostDescendant();
        return node != null ? node.getIndex() : this.getIndex();
    }

    @Override
    public int getRightmostDescendantIndex() throws MaltChainedException {
        ComparableNode node = this.getRightmostDescendant();
        return node != null ? node.getIndex() : this.getIndex();
    }

    @Override
    public SortedSet<Edge> getIncomingSecondaryEdges() throws MaltChainedException {
        throw new LWGraphException("Not implemented in the light-weight dependency graph package");
    }

    @Override
    public SortedSet<Edge> getOutgoingSecondaryEdges() throws MaltChainedException {
        throw new LWGraphException("Not implemented in the light-weight dependency graph package");
    }

    @Override
    public Set<Edge> getHeadEdges() {
        SortedSet<Edge> edges = Collections.synchronizedSortedSet(new TreeSet());
        if (this.hasHead()) {
            edges.add(this.headEdge);
        }
        return edges;
    }

    @Override
    public Edge getHeadEdge() {
        if (!this.hasHead()) {
            return null;
        }
        return this.headEdge;
    }

    @Override
    public void addHeadEdgeLabel(SymbolTable table, String symbol) throws MaltChainedException {
        if (this.headEdge != null) {
            this.headEdge.addLabel(table, symbol);
        }
    }

    @Override
    public void addHeadEdgeLabel(SymbolTable table, int code) throws MaltChainedException {
        if (this.headEdge != null) {
            this.headEdge.addLabel(table, code);
        }
    }

    @Override
    public void addHeadEdgeLabel(LabelSet labelSet) throws MaltChainedException {
        if (this.headEdge != null) {
            this.headEdge.addLabel(labelSet);
        }
    }

    @Override
    public boolean hasHeadEdgeLabel(SymbolTable table) throws MaltChainedException {
        if (this.headEdge != null) {
            return this.headEdge.hasLabel(table);
        }
        return false;
    }

    @Override
    public String getHeadEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
        if (this.headEdge != null) {
            return this.headEdge.getLabelSymbol(table);
        }
        return null;
    }

    @Override
    public int getHeadEdgeLabelCode(SymbolTable table) throws MaltChainedException {
        if (this.headEdge != null) {
            return this.headEdge.getLabelCode(table);
        }
        return 0;
    }

    @Override
    public Set<SymbolTable> getHeadEdgeLabelTypes() throws MaltChainedException {
        if (this.headEdge != null) {
            return this.headEdge.getLabelTypes();
        }
        return new HashSet<SymbolTable>();
    }

    @Override
    public LabelSet getHeadEdgeLabelSet() throws MaltChainedException {
        if (this.headEdge != null) {
            return this.headEdge.getLabelSet();
        }
        return new LabelSet();
    }

    @Override
    public void addIncomingEdge(Edge in) throws MaltChainedException {
        this.headEdge = in;
    }

    @Override
    public void addOutgoingEdge(Edge out) throws MaltChainedException {
        throw new LWGraphException("Not implemented in the light-weight dependency graph package");
    }

    @Override
    public void removeIncomingEdge(Edge in) throws MaltChainedException {
        if (this.headEdge.equals(in)) {
            this.headEdge = null;
        }
    }

    @Override
    public void removeOutgoingEdge(Edge out) throws MaltChainedException {
        throw new LWGraphException("Not implemented in the light-weight dependency graph package");
    }

    @Override
    public Iterator<Edge> getIncomingEdgeIterator() {
        return this.getHeadEdges().iterator();
    }

    @Override
    public Iterator<Edge> getOutgoingEdgeIterator() {
        List<DependencyNode> dependents = this.getListOfDependents();
        ArrayList<Edge> outEdges = new ArrayList<Edge>(dependents.size());
        for (int i = 0; i < dependents.size(); ++i) {
            try {
                outEdges.add(dependents.get(i).getHeadEdge());
                continue;
            }
            catch (MaltChainedException e) {
                e.printStackTrace();
            }
        }
        return outEdges.iterator();
    }

    @Override
    public void setRank(int r) {
    }

    @Override
    public DependencyNode getComponent() {
        return null;
    }

    @Override
    public void setComponent(DependencyNode x) {
    }

    @Override
    public DependencyNode findComponent() {
        return this.graph.findComponent(this.index);
    }

    @Override
    public int getRank() {
        return this.graph.getRank(this.index);
    }

    @Override
    public boolean isHeadEdgeLabeled() {
        if (this.headEdge != null) {
            return this.headEdge.isLabeled();
        }
        return false;
    }

    @Override
    public int nHeadEdgeLabels() {
        if (this.headEdge != null) {
            return this.headEdge.nLabels();
        }
        return 0;
    }

    public void addColumnLabels(String[] columnLabels) throws MaltChainedException {
        this.addColumnLabels(columnLabels, true);
    }

    public void addColumnLabels(String[] columnLabels, boolean addEdges) throws MaltChainedException {
        if (addEdges) {
            TreeMap<ColumnDescription, String> edgeLabels = new TreeMap<ColumnDescription, String>();
            int tmpHeadIndex = -1;
            if (columnLabels != null) {
                for (int i = 0; i < columnLabels.length; ++i) {
                    ColumnDescription column = this.graph.getDataFormat().getColumnDescription(i);
                    if (column.getCategory() == 2) {
                        tmpHeadIndex = Integer.parseInt(columnLabels[i]);
                        continue;
                    }
                    if (column.getCategory() == 1) {
                        this.addLabel(this.graph.getSymbolTables().addSymbolTable(column.getName()), columnLabels[i]);
                        continue;
                    }
                    if (column.getCategory() != 3) continue;
                    edgeLabels.put(column, columnLabels[i]);
                }
            }
            if (tmpHeadIndex == -1) {
                this.headEdge = null;
            } else {
                if (tmpHeadIndex < -1) {
                    throw new LWGraphException("Not allowed to have head index less than -1.");
                }
                if (this.index == 0 && tmpHeadIndex != -1) {
                    throw new LWGraphException("Not allowed to add head to a root node.");
                }
                if (this.index == tmpHeadIndex) {
                    throw new LWGraphException("Not allowed to add head to itself");
                }
                this.headEdge = new LWEdge(this.graph.getNode(tmpHeadIndex), this, edgeLabels);
            }
        } else {
            if (columnLabels != null) {
                for (int i = 0; i < columnLabels.length; ++i) {
                    ColumnDescription column = this.graph.getDataFormat().getColumnDescription(i);
                    if (column.getCategory() != 1) continue;
                    this.addLabel(this.graph.getSymbolTables().addSymbolTable(column.getName()), columnLabels[i]);
                }
            }
            this.headEdge = null;
        }
    }

    @Override
    public void addLabel(SymbolTable table, String symbol) throws MaltChainedException {
        ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
        table.addSymbol(symbol);
        this.labels.put(column.getPosition(), symbol);
    }

    @Override
    public void addLabel(SymbolTable table, int code) throws MaltChainedException {
        this.addLabel(table, table.getSymbolCodeToString(code));
    }

    @Override
    public void addLabel(LabelSet labels) throws MaltChainedException {
        for (SymbolTable table : labels.keySet()) {
            this.addLabel(table, (Integer)labels.get(table));
        }
    }

    @Override
    public boolean hasLabel(SymbolTable table) throws MaltChainedException {
        ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
        return this.labels.containsKey(column.getPosition());
    }

    @Override
    public String getLabelSymbol(SymbolTable table) throws MaltChainedException {
        ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
        return this.labels.get(column.getPosition());
    }

    @Override
    public int getLabelCode(SymbolTable table) throws MaltChainedException {
        ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
        return table.getSymbolStringToCode(this.labels.get(column.getPosition()));
    }

    @Override
    public int nLabels() {
        return this.labels.size();
    }

    @Override
    public Set<SymbolTable> getLabelTypes() {
        HashSet<SymbolTable> labelTypes = new HashSet<SymbolTable>();
        SymbolTableHandler symbolTableHandler = this.getBelongsToGraph().getSymbolTables();
        SortedSet<ColumnDescription> selectedColumns = this.graph.getDataFormat().getSelectedColumnDescriptions(this.labels.keySet());
        for (ColumnDescription column : selectedColumns) {
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
        SortedSet<ColumnDescription> selectedColumns = this.graph.getDataFormat().getSelectedColumnDescriptions(this.labels.keySet());
        for (ColumnDescription column : selectedColumns) {
            try {
                SymbolTable table = symbolTableHandler.getSymbolTable(column.getName());
                int code = table.getSymbolStringToCode(this.labels.get(column.getPosition()));
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
        ColumnDescription column = this.graph.getDataFormat().getColumnDescription(table.getName());
        this.labels.remove(column.getPosition());
    }

    @Override
    public void removeLabels() throws MaltChainedException {
        this.labels.clear();
    }

    @Override
    public LabeledStructure getBelongsToGraph() {
        return this.graph;
    }

    @Override
    public void setBelongsToGraph(LabeledStructure belongsToGraph) {
    }

    @Override
    public void clear() throws MaltChainedException {
        this.labels.clear();
    }

    @Override
    public int compareTo(ComparableNode that) {
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
        result = 31 * result + (this.headEdge == null ? 0 : this.headEdge.hashCode());
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
        LWNode other = (LWNode)obj;
        if (this.headEdge == null ? other.headEdge != null : !this.headEdge.equals(other.headEdge)) {
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
                sb.append(this.getHeadIndex());
            } else if (column.getCategory() == 1) {
                sb.append(this.labels.get(column.getPosition()));
            } else if (column.getCategory() == 3) {
                if (this.headEdge != null) {
                    sb.append(((LWEdge)this.headEdge).getLabel(column));
                } else {
                    sb.append(column.getDefaultOutput());
                }
            } else if (column.getCategory() == 7) {
                sb.append(column.getDefaultOutput());
            }
            sb.append('\t');
        }
        sb.setLength(sb.length() > 0 ? sb.length() - 1 : 0);
        return sb.toString();
    }
}

