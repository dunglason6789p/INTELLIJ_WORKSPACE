/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.planar;

import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.algorithm.planar.PlanarConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.transition.TransitionTable;

public class Planar
extends TransitionSystem {
    protected static final int SHIFT = 1;
    protected static final int REDUCE = 2;
    protected static final int RIGHTARC = 3;
    protected static final int LEFTARC = 4;

    public Planar(PropagationManager propagationManager) throws MaltChainedException {
        super(propagationManager);
    }

    @Override
    public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        PlanarConfig planarConfig = (PlanarConfig)config;
        Stack<DependencyNode> stack = planarConfig.getStack();
        Stack<DependencyNode> input = planarConfig.getInput();
        currentAction.getAction(this.actionContainers);
        Edge e = null;
        switch (this.transActionContainer.getActionCode()) {
            case 4: {
                e = planarConfig.getDependencyStructure().addDependencyEdge(input.peek().getIndex(), stack.peek().getIndex());
                this.addEdgeLabels(e);
                break;
            }
            case 3: {
                e = planarConfig.getDependencyStructure().addDependencyEdge(stack.peek().getIndex(), input.peek().getIndex());
                this.addEdgeLabels(e);
                break;
            }
            case 2: {
                stack.pop();
                break;
            }
            default: {
                stack.push(input.pop());
            }
        }
    }

    @Override
    public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
        PlanarConfig planarConfig = (PlanarConfig)config;
        if (planarConfig.getRootHandling() != 1 && planarConfig.getStack().peek().isRoot()) {
            return this.updateActionContainers(history, 1, null);
        }
        return null;
    }

    @Override
    protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
        ttable.addTransition(1, "SH", false, null);
        ttable.addTransition(2, "RE", false, null);
        ttable.addTransition(3, "RA", true, null);
        ttable.addTransition(4, "LA", true, null);
    }

    @Override
    protected void initWithDefaultTransitions(GuideUserHistory history) throws MaltChainedException {
        ComplexDecisionAction currentAction = new ComplexDecisionAction(history);
        this.transActionContainer.setAction(1);
        this.transActionContainer.setAction(2);
        for (int i = 0; i < this.arcLabelActionContainers.length; ++i) {
            this.arcLabelActionContainers[i].setAction(-1);
        }
        currentAction.addAction(this.actionContainers);
    }

    @Override
    public String getName() {
        return "planar arc-eager";
    }

    @Override
    public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        currentAction.getAction(this.actionContainers);
        int trans = this.transActionContainer.getActionCode();
        PlanarConfig planarConfig = (PlanarConfig)config;
        DependencyNode stackPeek = planarConfig.getStack().peek();
        DependencyNode inputPeek = planarConfig.getInput().peek();
        DependencyStructure dg = planarConfig.getDependencyGraph();
        boolean singleHeadConstraint = planarConfig.requiresSingleHead();
        boolean noCoveredRootsConstraint = planarConfig.requiresNoCoveredRoots();
        boolean acyclicityConstraint = planarConfig.requiresAcyclicity();
        boolean connectednessConstraintOnReduce = planarConfig.requiresConnectednessCheckOnReduce();
        boolean connectednessConstraintOnShift = planarConfig.requiresConnectednessCheckOnShift();
        if (!(trans != 4 && trans != 3 || this.isActionContainersLabeled())) {
            return false;
        }
        if (trans == 4) {
            if (stackPeek.isRoot()) {
                return false;
            }
            if (stackPeek.hasHead() && singleHeadConstraint) {
                return false;
            }
            if (stackPeek.hasHead() && dg.getTokenNode(stackPeek.getIndex()).getHead().getIndex() == inputPeek.getIndex()) {
                return false;
            }
            if (acyclicityConstraint && stackPeek.findComponent().getIndex() == inputPeek.findComponent().getIndex()) {
                return false;
            }
        }
        if (trans == 3) {
            if (inputPeek.hasHead() && singleHeadConstraint) {
                return false;
            }
            if (inputPeek.hasHead() && dg.getTokenNode(inputPeek.getIndex()).getHead().getIndex() == stackPeek.getIndex()) {
                return false;
            }
            if (acyclicityConstraint && stackPeek.findComponent().getIndex() == inputPeek.findComponent().getIndex()) {
                return false;
            }
        }
        if (trans == 2) {
            if (stackPeek.isRoot()) {
                return false;
            }
            if (!stackPeek.hasHead() && noCoveredRootsConstraint) {
                return false;
            }
            if (connectednessConstraintOnReduce) {
                boolean path1;
                DependencyNode stackPrev;
                boolean bl = path1 = stackPeek.findComponent().getIndex() == inputPeek.findComponent().getIndex();
                boolean path2 = planarConfig.getStack().size() < 2 ? false : (stackPrev = (DependencyNode)planarConfig.getStack().get(planarConfig.getStack().size() - 2)).findComponent().getIndex() == stackPeek.findComponent().getIndex();
                return path1 || path2;
            }
        }
        if (trans == 1 && connectednessConstraintOnShift && planarConfig.getInput().size() == 1) {
            boolean path = planarConfig.getDependencyGraph().getTokenNode(1).findComponent().getIndex() == inputPeek.findComponent().getIndex();
            return path;
        }
        return true;
    }

    @Override
    public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
        return this.updateActionContainers(history, 1, null);
    }
}

