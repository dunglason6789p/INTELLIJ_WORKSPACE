/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.lw.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.concurrent.graph.dataformat.ColumnDescription;
import org.maltparser.concurrent.graph.dataformat.DataFormat;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.lw.graph.LWEdge;
import org.maltparser.core.lw.graph.LWGraphException;
import org.maltparser.core.lw.graph.LWNode;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.RootLabels;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public final class LWDependencyGraph
implements DependencyStructure {
    private static final String TAB_SIGN = "\t";
    private final DataFormat dataFormat;
    private final SymbolTableHandler symbolTables;
    private final RootLabels rootLabels;
    private final List<LWNode> nodes;
    private final HashMap<Integer, ArrayList<String>> comments;

    public LWDependencyGraph(DataFormat _dataFormat, SymbolTableHandler _symbolTables) throws MaltChainedException {
        this.dataFormat = _dataFormat;
        this.symbolTables = _symbolTables;
        this.rootLabels = new RootLabels();
        this.nodes = new ArrayList<LWNode>();
        this.nodes.add(new LWNode(this, 0));
        this.comments = new HashMap();
    }

    public LWDependencyGraph(DataFormat _dataFormat, SymbolTableHandler _symbolTables, String[] inputTokens, String defaultRootLabel) throws MaltChainedException {
        this(_dataFormat, _symbolTables, inputTokens, defaultRootLabel, true);
    }

    public LWDependencyGraph(DataFormat _dataFormat, SymbolTableHandler _symbolTables, String[] inputTokens, String defaultRootLabel, boolean addEdges) throws MaltChainedException {
        this.dataFormat = _dataFormat;
        this.symbolTables = _symbolTables;
        this.rootLabels = new RootLabels();
        this.nodes = new ArrayList<LWNode>(inputTokens.length + 1);
        this.comments = new HashMap();
        this.resetTokens(inputTokens, defaultRootLabel, addEdges);
    }

    public void resetTokens(String[] inputTokens, String defaultRootLabel, boolean addEdges) throws MaltChainedException {
        int i;
        this.nodes.clear();
        this.comments.clear();
        this.symbolTables.cleanUp();
        this.nodes.add(new LWNode(this, 0));
        for (i = 0; i < inputTokens.length; ++i) {
            this.nodes.add(new LWNode(this, i + 1));
        }
        for (i = 0; i < inputTokens.length; ++i) {
            this.nodes.get(i + 1).addColumnLabels(inputTokens[i].split(TAB_SIGN), addEdges);
        }
        for (i = 0; i < this.nodes.size(); ++i) {
            if (this.nodes.get(i).getHeadIndex() < this.nodes.size()) continue;
            throw new LWGraphException("Not allowed to add a head node that doesn't exists");
        }
        for (i = 0; i < this.dataFormat.numberOfColumns(); ++i) {
            ColumnDescription column = this.dataFormat.getColumnDescription(i);
            if (column.isInternal() || column.getCategory() != 3) continue;
            this.rootLabels.setDefaultRootLabel(this.symbolTables.getSymbolTable(column.getName()), defaultRootLabel);
        }
    }

    public DataFormat getDataFormat() {
        return this.dataFormat;
    }

    public LWNode getNode(int nodeIndex) {
        if (nodeIndex < 0 || nodeIndex >= this.nodes.size()) {
            return null;
        }
        return this.nodes.get(nodeIndex);
    }

    public int nNodes() {
        return this.nodes.size();
    }

    protected boolean hasDependent(int nodeIndex) {
        for (int i = 1; i < this.nodes.size(); ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            return true;
        }
        return false;
    }

    protected boolean hasLeftDependent(int nodeIndex) {
        for (int i = 1; i < nodeIndex; ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            return true;
        }
        return false;
    }

    protected boolean hasRightDependent(int nodeIndex) {
        for (int i = nodeIndex + 1; i < this.nodes.size(); ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            return true;
        }
        return false;
    }

    protected List<DependencyNode> getListOfLeftDependents(int nodeIndex) {
        List<DependencyNode> leftDependents = Collections.synchronizedList(new ArrayList());
        for (int i = 1; i < nodeIndex; ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            leftDependents.add(this.nodes.get(i));
        }
        return leftDependents;
    }

    protected SortedSet<DependencyNode> getSortedSetOfLeftDependents(int nodeIndex) {
        SortedSet<DependencyNode> leftDependents = Collections.synchronizedSortedSet(new TreeSet());
        for (int i = 1; i < nodeIndex; ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            leftDependents.add(this.nodes.get(i));
        }
        return leftDependents;
    }

    protected List<DependencyNode> getListOfRightDependents(int nodeIndex) {
        List<DependencyNode> rightDependents = Collections.synchronizedList(new ArrayList());
        for (int i = nodeIndex + 1; i < this.nodes.size(); ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            rightDependents.add(this.nodes.get(i));
        }
        return rightDependents;
    }

    protected SortedSet<DependencyNode> getSortedSetOfRightDependents(int nodeIndex) {
        SortedSet<DependencyNode> rightDependents = Collections.synchronizedSortedSet(new TreeSet());
        for (int i = nodeIndex + 1; i < this.nodes.size(); ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            rightDependents.add(this.nodes.get(i));
        }
        return rightDependents;
    }

    protected List<DependencyNode> getListOfDependents(int nodeIndex) {
        List<DependencyNode> dependents = Collections.synchronizedList(new ArrayList());
        for (int i = 1; i < this.nodes.size(); ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            dependents.add(this.nodes.get(i));
        }
        return dependents;
    }

    protected SortedSet<DependencyNode> getSortedSetOfDependents(int nodeIndex) {
        SortedSet<DependencyNode> dependents = Collections.synchronizedSortedSet(new TreeSet());
        for (int i = 1; i < this.nodes.size(); ++i) {
            if (nodeIndex != this.nodes.get(i).getHeadIndex()) continue;
            dependents.add(this.nodes.get(i));
        }
        return dependents;
    }

    protected int getRank(int nodeIndex) {
        int i;
        int[] components = new int[this.nodes.size()];
        int[] ranks = new int[this.nodes.size()];
        for (i = 0; i < components.length; ++i) {
            components[i] = i;
            ranks[i] = 0;
        }
        for (i = 1; i < this.nodes.size(); ++i) {
            int dcIndex;
            int hcIndex;
            if (!this.nodes.get(i).hasHead() || (hcIndex = this.findComponent(this.nodes.get(i).getHead().getIndex(), components)) == (dcIndex = this.findComponent(this.nodes.get(i).getIndex(), components))) continue;
            this.link(hcIndex, dcIndex, components, ranks);
        }
        return ranks[nodeIndex];
    }

    protected DependencyNode findComponent(int nodeIndex) {
        int i;
        int[] components = new int[this.nodes.size()];
        int[] ranks = new int[this.nodes.size()];
        for (i = 0; i < components.length; ++i) {
            components[i] = i;
            ranks[i] = 0;
        }
        for (i = 1; i < this.nodes.size(); ++i) {
            int dcIndex;
            int hcIndex;
            if (!this.nodes.get(i).hasHead() || (hcIndex = this.findComponent(this.nodes.get(i).getHead().getIndex(), components)) == (dcIndex = this.findComponent(this.nodes.get(i).getIndex(), components))) continue;
            this.link(hcIndex, dcIndex, components, ranks);
        }
        return this.nodes.get(this.findComponent(nodeIndex, components));
    }

    private int[] findComponents() {
        int i;
        int[] components = new int[this.nodes.size()];
        int[] ranks = new int[this.nodes.size()];
        for (i = 0; i < components.length; ++i) {
            components[i] = i;
            ranks[i] = 0;
        }
        for (i = 1; i < this.nodes.size(); ++i) {
            int hcIndex;
            int dcIndex;
            if (!this.nodes.get(i).hasHead() || (hcIndex = this.findComponent(this.nodes.get(i).getHead().getIndex(), components)) == (dcIndex = this.findComponent(this.nodes.get(i).getIndex(), components))) continue;
            this.link(hcIndex, dcIndex, components, ranks);
        }
        return components;
    }

    private int findComponent(int xIndex, int[] components) {
        if (xIndex != components[xIndex]) {
            components[xIndex] = this.findComponent(components[xIndex], components);
        }
        return components[xIndex];
    }

    private int link(int xIndex, int yIndex, int[] components, int[] ranks) {
        if (ranks[xIndex] <= ranks[yIndex]) {
            components[xIndex] = yIndex;
            if (ranks[xIndex] == ranks[yIndex]) {
                int[] arrn = ranks;
                int n = yIndex;
                arrn[n] = arrn[n] + 1;
            }
            return yIndex;
        }
        components[yIndex] = xIndex;
        return xIndex;
    }

    @Override
    public TokenNode addTokenNode() throws MaltChainedException {
        throw new LWGraphException("Not implemented in the light-weight dependency graph package");
    }

    @Override
    public TokenNode addTokenNode(int index) throws MaltChainedException {
        throw new LWGraphException("Not implemented in the light-weight dependency graph package");
    }

    @Override
    public TokenNode getTokenNode(int index) {
        return null;
    }

    @Override
    public void addComment(String comment, int at_index) {
        ArrayList<String> commentList = this.comments.get(at_index);
        if (commentList == null) {
            commentList = this.comments.put(at_index, new ArrayList());
        }
        commentList.add(comment);
    }

    @Override
    public ArrayList<String> getComment(int at_index) {
        return this.comments.get(at_index);
    }

    @Override
    public boolean hasComments() {
        return this.comments.size() > 0;
    }

    @Override
    public int nTokenNode() {
        return this.nodes.size() - 1;
    }

    @Override
    public SortedSet<Integer> getTokenIndices() {
        SortedSet<Integer> indices = Collections.synchronizedSortedSet(new TreeSet());
        for (int i = 1; i < this.nodes.size(); ++i) {
            indices.add(i);
        }
        return indices;
    }

    @Override
    public int getHighestTokenIndex() {
        return this.nodes.size() - 1;
    }

    @Override
    public boolean hasTokens() {
        return this.nodes.size() > 1;
    }

    @Override
    public int getSentenceID() {
        return 0;
    }

    @Override
    public void setSentenceID(int sentenceID) {
    }

    @Override
    public void clear() throws MaltChainedException {
        this.nodes.clear();
    }

    @Override
    public SymbolTableHandler getSymbolTables() {
        return this.symbolTables;
    }

    @Override
    public void setSymbolTables(SymbolTableHandler symbolTables) {
    }

    @Override
    public void addLabel(Element element, String labelFunction, String label) throws MaltChainedException {
        element.addLabel(this.symbolTables.addSymbolTable(labelFunction), label);
    }

    @Override
    public LabelSet checkOutNewLabelSet() throws MaltChainedException {
        throw new LWGraphException("Not implemented in light-weight dependency graph");
    }

    @Override
    public void checkInLabelSet(LabelSet labelSet) throws MaltChainedException {
        throw new LWGraphException("Not implemented in light-weight dependency graph");
    }

    @Override
    public Edge addSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
        throw new LWGraphException("Not implemented in light-weight dependency graph");
    }

    @Override
    public void removeSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
        throw new LWGraphException("Not implemented in light-weight dependency graph");
    }

    @Override
    public DependencyNode addDependencyNode() throws MaltChainedException {
        LWNode node = new LWNode(this, this.nodes.size());
        this.nodes.add(node);
        return node;
    }

    @Override
    public DependencyNode addDependencyNode(int index) throws MaltChainedException {
        if (index == 0) {
            return this.nodes.get(0);
        }
        if (index == this.nodes.size()) {
            return this.addDependencyNode();
        }
        throw new LWGraphException("Not implemented in light-weight dependency graph");
    }

    @Override
    public DependencyNode getDependencyNode(int index) throws MaltChainedException {
        if (index < 0 || index >= this.nodes.size()) {
            return null;
        }
        return this.nodes.get(index);
    }

    @Override
    public int nDependencyNode() {
        return this.nodes.size();
    }

    @Override
    public int getHighestDependencyNodeIndex() {
        return this.nodes.size() - 1;
    }

    @Override
    public Edge addDependencyEdge(int headIndex, int dependentIndex) throws MaltChainedException {
        if (headIndex < 0 && headIndex >= this.nodes.size()) {
            throw new LWGraphException("The head doesn't exists");
        }
        if (dependentIndex < 0 && dependentIndex >= this.nodes.size()) {
            throw new LWGraphException("The dependent doesn't exists");
        }
        LWNode head = this.nodes.get(headIndex);
        LWNode dependent = this.nodes.get(dependentIndex);
        LWEdge headEdge = new LWEdge(head, dependent);
        dependent.addIncomingEdge(headEdge);
        return headEdge;
    }

    @Override
    public Edge moveDependencyEdge(int newHeadIndex, int dependentIndex) throws MaltChainedException {
        if (newHeadIndex < 0 && newHeadIndex >= this.nodes.size()) {
            throw new LWGraphException("The head doesn't exists");
        }
        if (dependentIndex < 0 && dependentIndex >= this.nodes.size()) {
            throw new LWGraphException("The dependent doesn't exists");
        }
        LWNode head = this.nodes.get(newHeadIndex);
        LWNode dependent = this.nodes.get(dependentIndex);
        Edge oldheadEdge = dependent.getHeadEdge();
        LWEdge headEdge = new LWEdge(head, dependent);
        headEdge.addLabel(oldheadEdge.getLabelSet());
        dependent.addIncomingEdge(headEdge);
        return headEdge;
    }

    @Override
    public void removeDependencyEdge(int headIndex, int dependentIndex) throws MaltChainedException {
        if (headIndex < 0 && headIndex >= this.nodes.size()) {
            throw new LWGraphException("The head doesn't exists");
        }
        if (dependentIndex < 0 && dependentIndex >= this.nodes.size()) {
            throw new LWGraphException("The dependent doesn't exists");
        }
        LWNode head = this.nodes.get(headIndex);
        LWNode dependent = this.nodes.get(dependentIndex);
        LWEdge headEdge = new LWEdge(head, dependent);
        dependent.removeIncomingEdge(headEdge);
    }

    @Override
    public void linkAllTreesToRoot() throws MaltChainedException {
        for (int i = 0; i < this.nodes.size(); ++i) {
            if (this.nodes.get(i).hasHead()) continue;
            LWNode head = this.nodes.get(0);
            LWNode dependent = this.nodes.get(i);
            LWEdge headEdge = new LWEdge(head, dependent);
            headEdge.addLabel(this.getDefaultRootEdgeLabels());
            dependent.addIncomingEdge(headEdge);
        }
    }

    @Override
    public int nEdges() {
        int n = 0;
        for (int i = 1; i < this.nodes.size(); ++i) {
            if (!this.nodes.get(i).hasHead()) continue;
            ++n;
        }
        return n;
    }

    @Override
    public SortedSet<Edge> getEdges() {
        SortedSet<Edge> edges = Collections.synchronizedSortedSet(new TreeSet());
        for (int i = 1; i < this.nodes.size(); ++i) {
            if (!this.nodes.get(i).hasHead()) continue;
            edges.add(this.nodes.get(i).getHeadEdge());
        }
        return edges;
    }

    @Override
    public SortedSet<Integer> getDependencyIndices() {
        SortedSet<Integer> indices = Collections.synchronizedSortedSet(new TreeSet());
        for (int i = 0; i < this.nodes.size(); ++i) {
            indices.add(i);
        }
        return indices;
    }

    @Override
    public DependencyNode getDependencyRoot() {
        return this.nodes.get(0);
    }

    @Override
    public boolean hasLabeledDependency(int index) {
        if (index < 0 || index >= this.nodes.size()) {
            return false;
        }
        if (!this.nodes.get(index).hasHead()) {
            return false;
        }
        return this.nodes.get(index).isHeadLabeled();
    }

    @Override
    public boolean isConnected() {
        int[] components = this.findComponents();
        int tmp = components[0];
        for (int i = 1; i < components.length; ++i) {
            if (tmp == components[i]) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isProjective() throws MaltChainedException {
        for (int i = 1; i < this.nodes.size(); ++i) {
            if (this.nodes.get(i).isProjective()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isSingleHeaded() {
        return true;
    }

    @Override
    public boolean isTree() {
        return this.isConnected() && this.isSingleHeaded();
    }

    @Override
    public int nNonProjectiveEdges() throws MaltChainedException {
        int c = 0;
        for (int i = 1; i < this.nodes.size(); ++i) {
            if (this.nodes.get(i).isProjective()) continue;
            ++c;
        }
        return c;
    }

    @Override
    public LabelSet getDefaultRootEdgeLabels() throws MaltChainedException {
        return this.rootLabels.getDefaultRootLabels();
    }

    @Override
    public String getDefaultRootEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
        return this.rootLabels.getDefaultRootLabelSymbol(table);
    }

    @Override
    public int getDefaultRootEdgeLabelCode(SymbolTable table) throws MaltChainedException {
        return this.rootLabels.getDefaultRootLabelCode(table);
    }

    @Override
    public void setDefaultRootEdgeLabel(SymbolTable table, String defaultRootSymbol) throws MaltChainedException {
        this.rootLabels.setDefaultRootLabel(table, defaultRootSymbol);
    }

    @Override
    public void setDefaultRootEdgeLabels(String rootLabelOption, SortedMap<String, SymbolTable> edgeSymbolTables) throws MaltChainedException {
        this.rootLabels.setRootLabels(rootLabelOption, edgeSymbolTables);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LWNode node : this.nodes) {
            sb.append(node.toString().trim());
            sb.append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }
}

