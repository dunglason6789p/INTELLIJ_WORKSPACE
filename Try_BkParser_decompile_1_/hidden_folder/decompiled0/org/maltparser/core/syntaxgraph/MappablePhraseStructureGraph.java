/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.Element;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.RootLabels;
import org.maltparser.core.syntaxgraph.Sentence;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.ds2ps.LosslessMapping;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.edge.GraphEdge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.GraphNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.NonTerminal;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.Root;
import org.maltparser.core.syntaxgraph.node.Token;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class MappablePhraseStructureGraph
extends Sentence
implements DependencyStructure,
PhraseStructure {
    private final ObjectPoolList<Edge> edgePool;
    private final SortedSet<Edge> graphEdges;
    private Root root;
    private boolean singleHeadedConstraint;
    private final SortedMap<Integer, NonTerminal> nonTerminalNodes;
    private final ObjectPoolList<NonTerminal> nonTerminalPool;
    private LosslessMapping mapping;
    private RootLabels rootLabels;

    public MappablePhraseStructureGraph(SymbolTableHandler symbolTables) throws MaltChainedException {
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
        this.nonTerminalNodes = new TreeMap<Integer, NonTerminal>();
        this.nonTerminalPool = new ObjectPoolList<NonTerminal>(){

            @Override
            protected NonTerminal create() throws MaltChainedException {
                return new NonTerminal();
            }

            @Override
            public void resetObject(NonTerminal o) throws MaltChainedException {
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
        if (headIndex == 0) {
            head = this.root;
        } else if (headIndex > 0) {
            head = this.getOrAddTerminalNode(headIndex);
        }
        if (dependentIndex > 0) {
            dependent = this.getOrAddTerminalNode(dependentIndex);
        }
        return this.addDependencyEdge((DependencyNode)((Object)head), dependent);
    }

    public Edge addDependencyEdge(DependencyNode head, DependencyNode dependent) throws MaltChainedException {
        if (head == null || dependent == null || head.getBelongsToGraph() != this || dependent.getBelongsToGraph() != this) {
            throw new SyntaxGraphException("Head or dependent node is missing.");
        }
        if (!dependent.isRoot()) {
            DependencyNode dc;
            if (this.singleHeadedConstraint && dependent.hasHead()) {
                throw new SyntaxGraphException("The dependent already have a head. ");
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

    public Edge moveDependencyEdge(DependencyNode newHead, DependencyNode dependent) throws MaltChainedException {
        if (dependent == null || !dependent.hasHead() || newHead.getBelongsToGraph() != this || dependent.getBelongsToGraph() != this) {
            return null;
        }
        Edge headEdge = dependent.getHeadEdge();
        LabelSet labels = null;
        if (headEdge.isLabeled()) {
            labels = this.checkOutNewLabelSet();
            for (SymbolTable table : headEdge.getLabelTypes()) {
                labels.put(table, headEdge.getLabelCode(table));
            }
        }
        headEdge.clear();
        headEdge.setBelongsToGraph(this);
        headEdge.setEdge((Node)((Object)newHead), (Node)((Object)dependent), 1);
        if (labels != null) {
            headEdge.addLabel(labels);
            labels.clear();
            this.checkInLabelSet(labels);
        }
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
        if (head == null || dependent == null || head.getBelongsToGraph() != this || dependent.getBelongsToGraph() != this) {
            throw new SyntaxGraphException("Head or dependent node is missing.");
        }
        if (!dependent.isRoot()) {
            Iterator<Edge> ie = dependent.getIncomingEdgeIterator();
            while (ie.hasNext()) {
                Edge e = ie.next();
                if (e.getSource() != head) continue;
                ie.remove();
                this.graphEdges.remove(e);
                this.edgePool.checkIn(e);
            }
        } else {
            throw new SyntaxGraphException("Head node is not a root node or a terminal node.");
        }
    }

    @Override
    public Edge addSecondaryEdge(ComparableNode source, ComparableNode target) throws MaltChainedException {
        if (source == null || target == null || source.getBelongsToGraph() != this || target.getBelongsToGraph() != this) {
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
        if (source == null || target == null || source.getBelongsToGraph() != this || target.getBelongsToGraph() != this) {
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

    public void linkAllTerminalsToRoot() throws MaltChainedException {
        this.clear();
        Iterator i$ = this.terminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            int i = (Integer)i$.next();
            DependencyNode node = (DependencyNode)this.terminalNodes.get(i);
            this.addDependencyEdge(this.root, node);
        }
    }

    @Override
    public void linkAllTreesToRoot() throws MaltChainedException {
        Iterator i$ = this.terminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            int i = (Integer)i$.next();
            if (((Token)this.terminalNodes.get(i)).hasHead()) continue;
            Edge e = this.addDependencyEdge(this.root, (DependencyNode)this.terminalNodes.get(i));
            this.mapping.updatePhraseStructureGraph(this, e, false);
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
        this.root.setBelongsToGraph(this);
        this.nonTerminalPool.checkInAll();
        this.nonTerminalNodes.clear();
        if (this.mapping != null) {
            this.mapping.clear();
        }
        super.clear();
        ++this.numberOfComponents;
    }

    @Override
    public DependencyNode getDependencyRoot() {
        return this.root;
    }

    @Override
    public PhraseStructureNode addTerminalNode() throws MaltChainedException {
        return this.addTokenNode();
    }

    @Override
    public PhraseStructureNode addTerminalNode(int index) throws MaltChainedException {
        return this.addTokenNode(index);
    }

    @Override
    public PhraseStructureNode getTerminalNode(int index) {
        return this.getTokenNode(index);
    }

    @Override
    public int nTerminalNode() {
        return this.nTokenNode();
    }

    @Override
    public PhraseStructureNode addNonTerminalNode(int index) throws MaltChainedException {
        NonTerminal node = this.nonTerminalPool.checkOut();
        node.setIndex(index);
        node.setBelongsToGraph(this);
        this.nonTerminalNodes.put(index, node);
        return node;
    }

    @Override
    public PhraseStructureNode addNonTerminalNode() throws MaltChainedException {
        int index = this.getHighestNonTerminalIndex();
        if (index > 0) {
            return this.addNonTerminalNode(index + 1);
        }
        return this.addNonTerminalNode(1);
    }

    @Override
    public PhraseStructureNode getNonTerminalNode(int index) throws MaltChainedException {
        return (PhraseStructureNode)this.nonTerminalNodes.get(index);
    }

    @Override
    public int getHighestNonTerminalIndex() {
        try {
            return this.nonTerminalNodes.lastKey();
        }
        catch (NoSuchElementException e) {
            return 0;
        }
    }

    @Override
    public Set<Integer> getNonTerminalIndices() {
        return new TreeSet<Integer>(this.nonTerminalNodes.keySet());
    }

    @Override
    public boolean hasNonTerminals() {
        return !this.nonTerminalNodes.isEmpty();
    }

    @Override
    public int nNonTerminals() {
        return this.nonTerminalNodes.size();
    }

    @Override
    public PhraseStructureNode getPhraseStructureRoot() {
        return this.root;
    }

    @Override
    public Edge addPhraseStructureEdge(PhraseStructureNode parent, PhraseStructureNode child) throws MaltChainedException {
        if (parent == null || child == null) {
            throw new MaltChainedException("Parent or child node is missing in sentence " + this.getSentenceID());
        }
        if (parent.getBelongsToGraph() != this || child.getBelongsToGraph() != this) {
            throw new MaltChainedException("Parent or child node is not a member of the graph in sentence " + this.getSentenceID());
        }
        if (parent == child) {
            throw new MaltChainedException("It is not allowed to add a phrase structure edge connecting the same node in sentence " + this.getSentenceID());
        }
        if (parent instanceof NonTerminalNode && !child.isRoot()) {
            Edge e = this.edgePool.checkOut();
            e.setBelongsToGraph(this);
            e.setEdge((Node)((Object)parent), (Node)((Object)child), 2);
            this.graphEdges.add(e);
            return e;
        }
        throw new MaltChainedException("Parent or child node is not of correct node type.");
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Edge && this.mapping != null) {
            try {
                this.mapping.update(this, (Edge)((Object)o), arg);
            }
            catch (MaltChainedException ex) {
                if (SystemLogger.logger().isDebugEnabled()) {
                    SystemLogger.logger().debug("", ex);
                } else {
                    SystemLogger.logger().error(ex.getMessageChain());
                }
                System.exit(1);
            }
        }
    }

    public LosslessMapping getMapping() {
        return this.mapping;
    }

    public void setMapping(LosslessMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void addLabel(Element element, String labelFunction, String label) throws MaltChainedException {
        super.addLabel(element, labelFunction, label);
    }

    @Override
    public void removePhraseStructureEdge(PhraseStructureNode parent, PhraseStructureNode child) throws MaltChainedException {
        if (parent == null || child == null) {
            throw new MaltChainedException("Parent or child node is missing.");
        }
        if (parent instanceof NonTerminalNode && !child.isRoot()) {
            for (Edge e : this.graphEdges) {
                if (e.getSource() != parent || e.getTarget() != child) continue;
                e.clear();
                this.graphEdges.remove(e);
                if (!(e instanceof GraphEdge)) continue;
                this.edgePool.checkIn(e);
            }
        } else {
            throw new SyntaxGraphException("Head node is not a root node or a terminal node.");
        }
    }

    @Override
    public boolean isContinuous() {
        for (int index : this.nonTerminalNodes.keySet()) {
            NonTerminalNode node = (NonTerminalNode)this.nonTerminalNodes.get(index);
            if (node.isContinuous()) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isContinuousExcludeTerminalsAttachToRoot() {
        for (int index : this.nonTerminalNodes.keySet()) {
            NonTerminalNode node = (NonTerminalNode)this.nonTerminalNodes.get(index);
            if (node.isContinuousExcludeTerminalsAttachToRoot()) continue;
            return false;
        }
        return true;
    }

    public String toStringTerminalNode(TokenNode node) {
        StringBuilder sb = new StringBuilder();
        TokenNode depnode = node;
        sb.append(node.toString().trim());
        if (depnode.hasHead()) {
            sb.append('\t');
            try {
                sb.append(depnode.getHead().getIndex());
                sb.append('\t');
                sb.append(depnode.getHeadEdge().toString());
            }
            catch (MaltChainedException e) {
                System.err.println(e);
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    public String toStringNonTerminalNode(NonTerminalNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(node.toString().trim());
        sb.append('\n');
        Iterator<Edge> ie = ((Node)((Object)node)).getOutgoingEdgeIterator();
        while (ie.hasNext()) {
            Edge e = ie.next();
            if (e.getTarget() instanceof TokenNode) {
                sb.append("   T");
                sb.append(e.getTarget().getIndex());
            }
            if (e.getTarget() instanceof NonTerminalNode) {
                sb.append("   N");
                sb.append(e.getTarget().getIndex());
            }
            sb.append('\t');
            sb.append(e.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        int index;
        StringBuilder sb = new StringBuilder();
        Iterator<Object> i$ = this.terminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            index = (Integer)i$.next();
            sb.append(this.toStringTerminalNode((TokenNode)this.terminalNodes.get(index)));
        }
        sb.append('\n');
        sb.append(this.toStringNonTerminalNode((NonTerminalNode)this.getPhraseStructureRoot()));
        i$ = this.nonTerminalNodes.keySet().iterator();
        while (i$.hasNext()) {
            index = (Integer)i$.next();
            sb.append(this.toStringNonTerminalNode((NonTerminalNode)this.nonTerminalNodes.get(index)));
        }
        return sb.toString();
    }

}

