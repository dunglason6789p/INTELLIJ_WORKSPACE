/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.stack;

import java.util.SortedSet;
import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParsingException;
import org.maltparser.parser.history.HistoryNode;

public class StackConfig
extends ParserConfiguration {
    private final Stack<DependencyNode> stack = new Stack();
    private final Stack<DependencyNode> input = new Stack();
    private DependencyStructure dependencyGraph;
    private int lookahead;

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
        return this.input.isEmpty() && this.stack.size() == 1;
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

    private DependencyNode getBufferNode(int index) throws MaltChainedException {
        if (index < 0) {
            throw new ParsingException("Input index must be non-negative in feature specification. ");
        }
        if (this.input.size() - index > 0) {
            return (DependencyNode)this.input.get(this.input.size() - 1 - index);
        }
        return null;
    }

    public DependencyNode getLookaheadNode(int index) throws MaltChainedException {
        return this.getBufferNode(this.lookahead + index);
    }

    public DependencyNode getInputNode(int index) throws MaltChainedException {
        if (index < this.lookahead) {
            return this.getBufferNode(index);
        }
        return null;
    }

    @Override
    public void setDependencyGraph(DependencyStructure source) throws MaltChainedException {
        this.dependencyGraph = source;
    }

    public void lookaheadIncrement() {
        ++this.lookahead;
    }

    public void lookaheadDecrement() {
        if (this.lookahead > 0) {
            --this.lookahead;
        }
    }

    @Override
    public DependencyStructure getDependencyGraph() {
        return this.dependencyGraph;
    }

    public void initialize(ParserConfiguration parserConfiguration) throws MaltChainedException {
        if (parserConfiguration != null) {
            int i;
            StackConfig config = (StackConfig)parserConfiguration;
            Stack<DependencyNode> sourceStack = config.getStack();
            Stack<DependencyNode> sourceInput = config.getInput();
            this.setDependencyGraph(config.getDependencyGraph());
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
        StackConfig that = (StackConfig)obj;
        if (this.lookahead != that.lookahead) {
            return false;
        }
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

    @Override
    public void clear() throws MaltChainedException {
        this.stack.clear();
        this.input.clear();
        this.historyNode = null;
        this.lookahead = 0;
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

