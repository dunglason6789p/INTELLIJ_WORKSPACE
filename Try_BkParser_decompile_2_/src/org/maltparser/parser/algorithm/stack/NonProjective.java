/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.stack;

import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.algorithm.stack.StackConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.transition.TransitionTable;

public class NonProjective
extends TransitionSystem {
    protected static final int SHIFT = 1;
    protected static final int SWAP = 2;
    protected static final int RIGHTARC = 3;
    protected static final int LEFTARC = 4;

    public NonProjective(PropagationManager propagationManager) throws MaltChainedException {
        super(propagationManager);
    }

    @Override
    public void apply(GuideUserAction currentAction, ParserConfiguration configuration) throws MaltChainedException {
        StackConfig config = (StackConfig)configuration;
        Stack<DependencyNode> stack = config.getStack();
        Stack<DependencyNode> input = config.getInput();
        currentAction.getAction(this.actionContainers);
        Edge e = null;
        DependencyNode head = null;
        DependencyNode dep = null;
        switch (this.transActionContainer.getActionCode()) {
            case 4: {
                head = stack.pop();
                dep = stack.pop();
                e = config.getDependencyStructure().addDependencyEdge(head.getIndex(), dep.getIndex());
                this.addEdgeLabels(e);
                stack.push(head);
                break;
            }
            case 3: {
                dep = stack.pop();
                e = config.getDependencyStructure().addDependencyEdge(stack.peek().getIndex(), dep.getIndex());
                this.addEdgeLabels(e);
                break;
            }
            case 2: {
                dep = stack.pop();
                input.push(stack.pop());
                stack.push(dep);
                config.lookaheadIncrement();
                break;
            }
            default: {
                if (input.isEmpty()) {
                    stack.pop();
                } else {
                    stack.push(input.pop());
                }
                config.lookaheadDecrement();
            }
        }
    }

    @Override
    public boolean permissible(GuideUserAction currentAction, ParserConfiguration configuration) throws MaltChainedException {
        currentAction.getAction(this.actionContainers);
        int trans = this.transActionContainer.getActionCode();
        if (!(trans != 4 && trans != 3 || this.isActionContainersLabeled())) {
            return false;
        }
        StackConfig config = (StackConfig)configuration;
        Stack<DependencyNode> stack = config.getStack();
        if ((trans == 4 || trans == 3 || trans == 2) && stack.size() < 2) {
            return false;
        }
        if ((trans == 4 || trans == 2) && ((DependencyNode)stack.get(stack.size() - 2)).isRoot()) {
            return false;
        }
        Stack<DependencyNode> input = config.getInput();
        if (trans == 1 && input.isEmpty()) {
            return false;
        }
        return trans != 2 || ((DependencyNode)stack.get(stack.size() - 2)).getIndex() <= ((DependencyNode)stack.get(stack.size() - 1)).getIndex();
    }

    @Override
    public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
        return null;
    }

    @Override
    protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
        ttable.addTransition(1, "SH", false, null);
        ttable.addTransition(2, "SW", false, null);
        ttable.addTransition(3, "RA", true, null);
        ttable.addTransition(4, "LA", true, null);
    }

    @Override
    protected void initWithDefaultTransitions(GuideUserHistory history) throws MaltChainedException {
        ComplexDecisionAction currentAction = new ComplexDecisionAction(history);
        this.transActionContainer.setAction(1);
        for (int i = 0; i < this.arcLabelActionContainers.length; ++i) {
            this.arcLabelActionContainers[i].setAction(-1);
        }
        currentAction.addAction(this.actionContainers);
    }

    @Override
    public String getName() {
        return "nonprojective";
    }

    @Override
    public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
        if (((StackConfig)configuration).getInput().isEmpty()) {
            return this.updateActionContainers(history, 3, ((StackConfig)configuration).getDependencyGraph().getDefaultRootEdgeLabels());
        }
        return this.updateActionContainers(history, 1, null);
    }
}

