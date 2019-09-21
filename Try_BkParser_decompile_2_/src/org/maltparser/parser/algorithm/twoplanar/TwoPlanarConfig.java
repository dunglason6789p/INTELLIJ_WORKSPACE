/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.twoplanar;

import java.util.SortedSet;
import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParsingException;
import org.maltparser.parser.history.HistoryNode;

public class TwoPlanarConfig
extends ParserConfiguration {
    public static final int NORMAL = 1;
    public static final int RELAXED = 2;
    public final boolean SINGLE_HEAD = true;
    public boolean noCoveredRoots = false;
    public boolean acyclicity = true;
    public boolean reduceAfterSwitch = false;
    private final Stack<DependencyNode> firstStack = new Stack();
    private final Stack<DependencyNode> secondStack = new Stack();
    public static final boolean FIRST_STACK = false;
    public static final boolean SECOND_STACK = true;
    private boolean activeStack = false;
    private Stack<DependencyNode> input = new Stack();
    private DependencyStructure dependencyGraph;
    private int rootHandling;
    private int lastAction;

    public TwoPlanarConfig(String noCoveredRoots, String acyclicity, String reduceAfterSwitch, String rootHandling) throws MaltChainedException {
        this.setRootHandling(rootHandling);
        this.setNoCoveredRoots(Boolean.valueOf(noCoveredRoots));
        this.setAcyclicity(Boolean.valueOf(acyclicity));
        this.setReduceAfterSwitch(Boolean.valueOf(reduceAfterSwitch));
    }

    public void switchStacks() {
        this.activeStack = !this.activeStack;
    }

    public boolean reduceAfterSwitch() {
        return this.reduceAfterSwitch;
    }

    public void setReduceAfterSwitch(boolean ras) {
        this.reduceAfterSwitch = ras;
    }

    public void setLastAction(int action) {
        this.lastAction = action;
    }

    public int getLastAction() {
        return this.lastAction;
    }

    public boolean getStackActivityState() {
        return this.activeStack;
    }

    private Stack<DependencyNode> getFirstStack() {
        return this.firstStack;
    }

    private Stack<DependencyNode> getSecondStack() {
        return this.secondStack;
    }

    public Stack<DependencyNode> getActiveStack() {
        if (!this.activeStack) {
            return this.getFirstStack();
        }
        return this.getSecondStack();
    }

    public Stack<DependencyNode> getInactiveStack() {
        if (!this.activeStack) {
            return this.getSecondStack();
        }
        return this.getFirstStack();
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

    private DependencyNode getStackNode(Stack<DependencyNode> stack, int index) throws MaltChainedException {
        if (index < 0) {
            throw new ParsingException("Stack index must be non-negative in feature specification. ");
        }
        if (stack.size() - index > 0) {
            return (DependencyNode)stack.get(stack.size() - 1 - index);
        }
        return null;
    }

    public DependencyNode getActiveStackNode(int index) throws MaltChainedException {
        return this.getStackNode(this.getActiveStack(), index);
    }

    public DependencyNode getInactiveStackNode(int index) throws MaltChainedException {
        return this.getStackNode(this.getInactiveStack(), index);
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
            TwoPlanarConfig planarConfig = (TwoPlanarConfig)parserConfiguration;
            this.activeStack = planarConfig.activeStack;
            Stack<DependencyNode> sourceActiveStack = planarConfig.getActiveStack();
            Stack<DependencyNode> sourceInactiveStack = planarConfig.getInactiveStack();
            Stack<DependencyNode> sourceInput = planarConfig.getInput();
            this.setDependencyGraph(planarConfig.getDependencyGraph());
            int n = sourceActiveStack.size();
            for (i = 0; i < n; ++i) {
                this.getActiveStack().add(this.dependencyGraph.getDependencyNode(((DependencyNode)sourceActiveStack.get(i)).getIndex()));
            }
            n = sourceInactiveStack.size();
            for (i = 0; i < n; ++i) {
                this.getInactiveStack().add(this.dependencyGraph.getDependencyNode(((DependencyNode)sourceInactiveStack.get(i)).getIndex()));
            }
            n = sourceInput.size();
            for (i = 0; i < n; ++i) {
                this.input.add(this.dependencyGraph.getDependencyNode(((DependencyNode)sourceInput.get(i)).getIndex()));
            }
        } else {
            this.getActiveStack().push(this.dependencyGraph.getDependencyRoot());
            this.getInactiveStack().push(this.dependencyGraph.getDependencyRoot());
            for (int i = this.dependencyGraph.getHighestTokenIndex(); i > 0; --i) {
                DependencyNode node = this.dependencyGraph.getDependencyNode(i);
                if (node == null) continue;
                this.input.push(node);
            }
        }
    }

    @Override
    public void initialize() throws MaltChainedException {
        this.getActiveStack().push(this.dependencyGraph.getDependencyRoot());
        this.getInactiveStack().push(this.dependencyGraph.getDependencyRoot());
        for (int i = this.dependencyGraph.getHighestTokenIndex(); i > 0; --i) {
            DependencyNode node = this.dependencyGraph.getDependencyNode(i);
            if (node == null) continue;
            this.input.push(node);
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

    public boolean requiresSingleHead() {
        return true;
    }

    public boolean requiresNoCoveredRoots() {
        return this.noCoveredRoots;
    }

    public boolean requiresAcyclicity() {
        return this.acyclicity;
    }

    public void setNoCoveredRoots(boolean value) {
        this.noCoveredRoots = value;
    }

    public void setAcyclicity(boolean value) {
        this.acyclicity = value;
    }

    @Override
    public void clear() throws MaltChainedException {
        this.getActiveStack().clear();
        this.getInactiveStack().clear();
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
        TwoPlanarConfig that = (TwoPlanarConfig)obj;
        if (this.getActiveStack().size() != that.getActiveStack().size()) {
            return false;
        }
        if (this.getInactiveStack().size() != that.getInactiveStack().size()) {
            return false;
        }
        if (this.input.size() != that.getInput().size()) {
            return false;
        }
        if (this.dependencyGraph.nEdges() != that.getDependencyGraph().nEdges()) {
            return false;
        }
        for (i = 0; i < this.getActiveStack().size(); ++i) {
            if (((DependencyNode)this.getActiveStack().get(i)).getIndex() == ((DependencyNode)that.getActiveStack().get(i)).getIndex()) continue;
            return false;
        }
        for (i = 0; i < this.getInactiveStack().size(); ++i) {
            if (((DependencyNode)this.getInactiveStack().get(i)).getIndex() == ((DependencyNode)that.getInactiveStack().get(i)).getIndex()) continue;
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
        sb.append(this.getActiveStack().size());
        sb.append(", ");
        sb.append(this.getInactiveStack().size());
        sb.append(", ");
        sb.append(this.input.size());
        sb.append(", ");
        sb.append(this.dependencyGraph.nEdges());
        return sb.toString();
    }
}

