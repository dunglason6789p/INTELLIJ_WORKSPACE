/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.RootLabels;
import org.maltparser.core.syntaxgraph.Sentence;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.edge.GraphEdge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.GraphNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.Root;
import org.maltparser.core.syntaxgraph.node.Token;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class DependencyGraph
extends Sentence
implements DependencyStructure {
    private final ObjectPoolList<Edge> edgePool;
    private final SortedSet<Edge> graphEdges;
    private final Root root;
    private boolean singleHeadedConstraint;
    private RootLabels rootLabels;

    public DependencyGraph(SymbolTableHandler symbolTables) throws MaltChainedException {
        super(symbolTables);
        this.setSingleHeadedConstraint(true);
        this.root = new Root();
        this.root.setBelongsToGraph(this);
        this.graphEdges = new TreeSet<Edge>();
        this.edgePool = new ObjectPoolList<Edge>(){

            @Override
            protected Edge create() {
                return new GraphEdge();
            }

            @Override
            public void resetObject(Edge o) throws MaltChainedException {
                o.clear();
            }
        };
        this.clear();
    }

    @Override
    public DependencyNode addDependencyNode() throws MaltChainedException {
        return this.addTokenNode();
    }

    @Override
    public DependencyNode addDependencyNode(int index) throws MaltChainedException {
        if (index == 0) {
            return this.root;
        }
        return this.addTokenNode(index);
    }

    @Override
    public DependencyNode getDependencyNode(int index) throws MaltChainedException {
        if (index == 0) {
            return this.root;
        }
        return this.getTokenNode(index);
    }

    @Override
    public int nDependencyNode() {
        return this.nTokenNode() + 1;
    }

    @Override
    public int getHighestDependencyNodeIndex() {
        if (this.hasTokens()) {
            return this.getHighestTokenIndex();
        }
        return 0;
    }

    @Override
    public Edge addDependencyEdge(int headIndex, int dependentIndex) throws MaltChainedException {
        GraphNode head = null;
        Token dependent = null;
        head = headIndex == 0 ? this.root : this.getOrAddTerminalNode(headIndex);
        if (dependentIndex > 0) {
            dependent = this.getOrAddTerminalNode(dependentIndex);
        }
        return this.addDependencyEdge((DependencyNode)((Object)head), dependent);
    }

    protected Edge addDependencyEdge(DependencyNode head, DependencyNode dependent) throws MaltChainedException {
        if (head == null || dependent == null) {
            throw new SyntaxGraphException("Head or dependent node is missing.");
        }
        if (!dependent.isRoot()) {
            DependencyNode dc;
            if (this.singleHeadedConstraint && dependent.hasHead()) {
                return this.moveDependencyEdge(head, dependent);
            }
            DependencyNode hc = head.findComponent();
            if (hc != (dc = dependent.findComponent())) {
                this.link(hc, dc);
                --this.numberOfComponents;
            }
            Edge e = this.edgePool.checkOut();
            e.setBelongsToGraph(this);
            e.setEdge((Node)((Object)head), (Node)((Object)dependent), 1);
            this.graphEdges.add(e);
            return e;
        }
        throw new SyntaxGraphException("Head node is not a root node or a terminal node.");
    }

    @Override
    public Edge moveDependencyEdge(int newHeadIndex, int dependentIndex) throws MaltChainedException {
        DependencyNode newHead = null;
        DependencyNode dependent = null;
        if (newHeadIndex == 0) {
            newHead = this.root;
        } else if (newHeadIndex > 0) {
            newHead = (DependencyNode)this.terminalNodes.get(newHeadIndex);
        }
        if (dependentIndex > 0) {
            dependent = (DependencyNode)this.terminalNodes.get(dependentIndex);
        }
        return this.moveDependencyEdge(newHead, dependent);
    }

    protected Edge moveDependencyEdge(DependencyNode newHead, DependencyNode dependent) throws MaltChainedException {
        if (dependent == null || !dependent.hasHead()) {
            return null;
        }
        Edge headEdge = dependent.getHeadEdge();
        LabelSet labels = this.checkOutNewLabelSet();
        for (SymbolTable table : headEdge.getLabelTypes()) {
            labels.put(table, headEdge.getLabelCode(table));
        }
        headEdge.clear();
        headEdge.setBelongsToGraph(this);
        headEdge.setEdge((Node)((Object)newHead), (Node)((Object)dependent), 1);
        headEdge.addLabel(labels);
        labels.clear();
        this.checkInLabelSet(labels);
        return headEdge;
    }

    @Override
    public void removeDependencyEdge(int headIndex, int dependentIndex) throws MaltChainedException {
        Node head = null;
        Node dependent = null;
        if (headIndex == 0) {
            head = this.root;
        } else if (headIndex > 0) {
            head = (Node)this.terminalNodes.get(headIndex);
        }
        if (dependentIndex > 0) {
            dependent = (Node)this.terminalNodes.get(dependentIndex);
        }
        this.removeDependencyEdge(head, dependent);
    }

    protected void removeDependencyEdge(Node head, Node dependent) throws MaltChainedException {
        if (head == null || dependent == null) {
            throw new SyntaxGraphException("Head or dependent node is missing.");
        }
        if (!dependent.isRoot()) {
            Iterator<Edge> ie = dependent.getIncomingEdgeIterator();
            while (ie.hasNext()) {
                Edge e = ie.next();
                if (e.getSource() != head) continue;
                this.graphEdges.remove(e);
                ie.remove();
                this.edgePool.checkIn(e);
            }
        } else {
            throw new SyntaxGraphException("Head node is not a root node or a terminal node.");
        }
    }

    @Override
    public Edge addSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
        if (source == null || target == null) {
            throw new SyntaxGraphException("Head or dependent node is missing.");
        }
        if (!target.isRoot()) {
            Edge e = this.edgePool.checkOut();
            e.setBelongsToGraph(this);
            e.setEdge((Node)source, (Node)target, 3);
            this.graphEdges.add(e);
            return e;
        }
        return null;
    }

    @Override
    public void removeSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
        if (source == null || target == null) {
            throw new SyntaxGraphException("Head or dependent node is missing.");
        }
        if (!target.isRoot()) {
            Iterator<Edge> ie = ((Node)target).getIncomingEdgeIterator();
            while (ie.hasNext()) {
                Edge e = ie.next();
                if (e.getSource() != source) continue;
                ie.remove();
                this.graphEdges.remove(e);
                this.edgePool.checkIn(e);
            }
        }
    }

    @Override
    public boolean hasLabeledDependency(int index) throws MaltChainedException {
        return this.getDependencyNode(index).hasHead() && this.getDependencyNode(index).getHeadEdge().isLabeled();
    }

    @Override
    public boolean isConnected() {
        return this.numberOfComponents == 1;
    }

    @Override
    public boolean isProjective() throws MaltChainedException {
        Iterator i$ = this.terminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            int i = (Integer)i$.next();
            if (((Token)this.terminalNodes.get(i)).isProjective()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isTree() {
        return this.isConnected() && this.isSingleHeaded();
    }

    @Override
    public boolean isSingleHeaded() {
        Iterator i$ = this.terminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            int i = (Integer)i$.next();
            if (((Token)this.terminalNodes.get(i)).hasAtMostOneHead()) continue;
            return false;
        }
        return true;
    }

    public boolean isSingleHeadedConstraint() {
        return this.singleHeadedConstraint;
    }

    public void setSingleHeadedConstraint(boolean singleHeadedConstraint) {
        this.singleHeadedConstraint = singleHeadedConstraint;
    }

    @Override
    public int nNonProjectiveEdges() throws MaltChainedException {
        int c = 0;
        Iterator i$ = this.terminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            int i = (Integer)i$.next();
            if (((Token)this.terminalNodes.get(i)).isProjective()) continue;
            ++c;
        }
        return c;
    }

    @Override
    public int nEdges() {
        return this.graphEdges.size();
    }

    @Override
    public SortedSet<Edge> getEdges() {
        return this.graphEdges;
    }

    @Override
    public SortedSet<Integer> getDependencyIndices() {
        TreeSet<Integer> indices = new TreeSet<Integer>(this.terminalNodes.keySet());
        indices.add(0);
        return indices;
    }

    protected DependencyNode link(DependencyNode x, DependencyNode y) throws MaltChainedException {
        if (x.getRank() <= y.getRank()) {
            x.setComponent(y);
            if (x.getRank() == y.getRank()) {
                y.setRank(y.getRank() + 1);
            }
            return y;
        }
        y.setComponent(x);
        return x;
    }

    @Override
    public void linkAllTreesToRoot() throws MaltChainedException {
        Iterator i$ = this.terminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            int i = (Integer)i$.next();
            if (((Token)this.terminalNodes.get(i)).hasHead()) continue;
            this.addDependencyEdge(this.root, (DependencyNode)this.terminalNodes.get(i));
        }
    }

    @Override
    public LabelSet getDefaultRootEdgeLabels() throws MaltChainedException {
        if (this.rootLabels == null) {
            return null;
        }
        return this.rootLabels.getDefaultRootLabels();
    }

    @Override
    public String getDefaultRootEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
        if (this.rootLabels == null) {
            return null;
        }
        return this.rootLabels.getDefaultRootLabelSymbol(table);
    }

    @Override
    public int getDefaultRootEdgeLabelCode(SymbolTable table) throws MaltChainedException {
        if (this.rootLabels == null) {
            return -1;
        }
        return this.rootLabels.getDefaultRootLabelCode(table);
    }

    @Override
    public void setDefaultRootEdgeLabel(SymbolTable table, String defaultRootSymbol) throws MaltChainedException {
        if (this.rootLabels == null) {
            this.rootLabels = new RootLabels();
        }
        this.rootLabels.setDefaultRootLabel(table, defaultRootSymbol);
    }

    @Override
    public void setDefaultRootEdgeLabels(String rootLabelOption, SortedMap<String, SymbolTable> edgeSymbolTables) throws MaltChainedException {
        if (this.rootLabels == null) {
            this.rootLabels = new RootLabels();
        }
        this.rootLabels.setRootLabels(rootLabelOption, edgeSymbolTables);
    }

    @Override
    public void clear() throws MaltChainedException {
        this.edgePool.checkInAll();
        this.graphEdges.clear();
        this.root.clear();
        super.clear();
        ++this.numberOfComponents;
    }

    @Override
    public DependencyNode getDependencyRoot() {
        return this.root;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator i$ = this.terminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            int index = (Integer)i$.next();
            sb.append(((Token)this.terminalNodes.get(index)).toString().trim());
            sb.append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }

}

