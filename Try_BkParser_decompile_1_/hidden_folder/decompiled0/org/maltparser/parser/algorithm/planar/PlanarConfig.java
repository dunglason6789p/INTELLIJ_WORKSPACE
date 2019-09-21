/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.planar;

import java.util.SortedSet;
import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParsingException;
import org.maltparser.parser.history.HistoryNode;

public class PlanarConfig
extends ParserConfiguration {
    public static final int NO_CONNECTEDNESS = 1;
    public static final int REDUCE_ONLY = 2;
    public static final int FULL_CONNECTEDNESS = 3;
    public static final int NORMAL = 1;
    public static final int RELAXED = 2;
    public final boolean SINGLE_HEAD = true;
    public boolean noCoveredRoots = false;
    public boolean acyclicity = true;
    public int connectedness = 1;
    private final Stack<DependencyNode> stack = new Stack();
    private final Stack<DependencyNode> input = new Stack();
    private DependencyStructure dependencyGraph;
    private int rootHandling;

    public PlanarConfig(String noCoveredRoots, String acyclicity, String connectedness, String rootHandling) throws MaltChainedException {
        this.setRootHandling(rootHandling);
        this.setNoCoveredRoots(Boolean.valueOf(noCoveredRoots));
        this.setAcyclicity(Boolean.valueOf(acyclicity));
        this.setConnectedness(connectedness);
    }

    public Stack<DependencyNode> getStack() {
        return this.stack;
    }

    public Stack<DependencyNode> getInput() {
        return this.input;
    }

    public DependencyStructure getDependencyStructure() {
        return this.dependencyGraph;
    }

    @Override
    public boolean isTerminalState() {
        return this.input.isEmpty();
    }

    public DependencyNode getStackNode(int index) throws MaltChainedException {
        if (index < 0) {
            throw new ParsingException("Stack index must be non-negative in feature specification. ");
        }
        if (this.stack.size() - index > 0) {
            return (DependencyNode)this.stack.get(this.stack.size() - 1 - index);
        }
        return null;
    }

    public DependencyNode getInputNode(int index) throws MaltChainedException {
        if (index < 0) {
            throw new ParsingException("Input index must be non-negative in feature specification. ");
        }
        if (this.input.size() - index > 0) {
            return (DependencyNode)this.input.get(this.input.size() - 1 - index);
        }
        return null;
    }

    @Override
    public void setDependencyGraph(DependencyStructure source) throws MaltChainedException {
        this.dependencyGraph = source;
    }

    @Override
    public DependencyStructure getDependencyGraph() {
        return this.dependencyGraph;
    }

    public void initialize(ParserConfiguration parserConfiguration) throws MaltChainedException {
        if (parserConfiguration != null) {
            int i;
            PlanarConfig planarConfig = (PlanarConfig)parserConfiguration;
            Stack<DependencyNode> sourceStack = planarConfig.getStack();
            Stack<DependencyNode> sourceInput = planarConfig.getInput();
            this.setDependencyGraph(planarConfig.getDependencyGraph());
            int n = sourceStack.size();
            for (i = 0; i < n; ++i) {
                this.stack.add(this.dependencyGraph.getDependencyNode(((DependencyNode)sourceStack.get(i)).getIndex()));
            }
            n = sourceInput.size();
            for (i = 0; i < n; ++i) {
                this.input.add(this.dependencyGraph.getDependencyNode(((DependencyNode)sourceInput.get(i)).getIndex()));
            }
        } else {
            this.stack.push(this.dependencyGraph.getDependencyRoot());
            for (int i = this.dependencyGraph.getHighestTokenIndex(); i > 0; --i) {
                DependencyNode node = this.dependencyGraph.getDependencyNode(i);
                if (node == null || node.hasHead()) continue;
                this.input.push(node);
            }
        }
    }

    @Override
    public void initialize() throws MaltChainedException {
        this.stack.push(this.dependencyGraph.getDependencyRoot());
        for (int i = this.dependencyGraph.getHighestTokenIndex(); i > 0; --i) {
            DependencyNode node = this.dependencyGraph.getDependencyNode(i);
            if (node == null || node.hasHead()) continue;
            this.input.push(node);
        }
    }

    public boolean requiresSingleHead() {
        return true;
    }

    public boolean requiresNoCoveredRoots() {
        return this.noCoveredRoots;
    }

    public boolean requiresAcyclicity() {
        return this.acyclicity;
    }

    public boolean requiresConnectednessCheckOnReduce() {
        return this.connectedness != 1;
    }

    public boolean requiresConnectednessCheckOnShift() {
        return this.connectedness == 3;
    }

    public void setNoCoveredRoots(boolean value) {
        this.noCoveredRoots = value;
    }

    public void setAcyclicity(boolean value) {
        this.acyclicity = value;
    }

    protected void setConnectedness(String conn) throws MaltChainedException {
        if (conn.equalsIgnoreCase("none")) {
            this.connectedness = 1;
        } else if (conn.equalsIgnoreCase("reduceonly")) {
            this.connectedness = 2;
        } else if (conn.equalsIgnoreCase("full")) {
            this.connectedness = 3;
        } else {
            throw new ParsingException("The connectedness constraint option '" + conn + "' is unknown");
        }
    }

    public int getRootHandling() {
        return this.rootHandling;
    }

    protected void setRootHandling(String rh) throws MaltChainedException {
        if (rh.equalsIgnoreCase("relaxed")) {
            this.rootHandling = 2;
        } else if (rh.equalsIgnoreCase("normal")) {
            this.rootHandling = 1;
        } else {
            throw new ParsingException("The root handling '" + rh + "' is unknown");
        }
    }

    @Override
    public void clear() throws MaltChainedException {
        this.stack.clear();
        this.input.clear();
        this.historyNode = null;
    }

    public boolean equals(Object obj) {
        int i;
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        PlanarConfig that = (PlanarConfig)obj;
        if (this.stack.size() != that.getStack().size()) {
            return false;
        }
        if (this.input.size() != that.getInput().size()) {
            return false;
        }
        if (this.dependencyGraph.nEdges() != that.getDependencyGraph().nEdges()) {
            return false;
        }
        for (i = 0; i < this.stack.size(); ++i) {
            if (((DependencyNode)this.stack.get(i)).getIndex() == ((DependencyNode)that.getStack().get(i)).getIndex()) continue;
            return false;
        }
        for (i = 0; i < this.input.size(); ++i) {
            if (((DependencyNode)this.input.get(i)).getIndex() == ((DependencyNode)that.getInput().get(i)).getIndex()) continue;
            return false;
        }
        return this.dependencyGraph.getEdges().equals(that.getDependencyGraph().getEdges());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.stack.size());
        sb.append(", ");
        sb.append(this.input.size());
        sb.append(", ");
        sb.append(this.dependencyGraph.nEdges());
        return sb.toString();
    }
}

