/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.PhraseStructure;
import org.maltparser.core.syntaxgraph.Sentence;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.edge.GraphEdge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.NonTerminal;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.Root;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class PhraseStructureGraph
extends Sentence
implements PhraseStructure {
    protected final ObjectPoolList<Edge> edgePool;
    protected final SortedSet<Edge> graphEdges;
    protected final SortedMap<Integer, NonTerminal> nonTerminalNodes;
    protected final ObjectPoolList<NonTerminal> nonTerminalPool;
    protected final Root root = new Root();

    public PhraseStructureGraph(SymbolTableHandler symbolTables) throws MaltChainedException {
        super(symbolTables);
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
            throw new MaltChainedException("Parent or child node is missing.");
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
    public int nEdges() {
        return this.graphEdges.size();
    }

    public SortedSet<Edge> getEdges() {
        return this.graphEdges;
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

    @Override
    public void clear() throws MaltChainedException {
        this.edgePool.checkInAll();
        this.graphEdges.clear();
        this.root.clear();
        this.root.setBelongsToGraph(this);
        this.nonTerminalPool.checkInAll();
        this.nonTerminalNodes.clear();
        super.clear();
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

