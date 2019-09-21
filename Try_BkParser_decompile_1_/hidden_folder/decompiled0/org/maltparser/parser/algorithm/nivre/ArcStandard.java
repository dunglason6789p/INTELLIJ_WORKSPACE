/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.nivre;

import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.algorithm.nivre.NivreConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.transition.TransitionTable;

public class ArcStandard
extends TransitionSystem {
    protected static final int SHIFT = 1;
    protected static final int RIGHTARC = 2;
    protected static final int LEFTARC = 3;

    public ArcStandard(PropagationManager propagationManager) throws MaltChainedException {
        super(propagationManager);
    }

    @Override
    public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        NivreConfig nivreConfig = (NivreConfig)config;
        Stack<DependencyNode> stack = nivreConfig.getStack();
        Stack<DependencyNode> input = nivreConfig.getInput();
        currentAction.getAction(this.actionContainers);
        Edge e = null;
        switch (this.transActionContainer.getActionCode()) {
            case 3: {
                e = nivreConfig.getDependencyStructure().addDependencyEdge(input.peek().getIndex(), stack.peek().getIndex());
                this.addEdgeLabels(e);
                stack.pop();
                break;
            }
            case 2: {
                e = nivreConfig.getDependencyStructure().addDependencyEdge(stack.peek().getIndex(), input.peek().getIndex());
                this.addEdgeLabels(e);
                input.pop();
                if (stack.peek().isRoot()) break;
                input.push(stack.pop());
                break;
            }
            default: {
                stack.push(input.pop());
            }
        }
    }

    @Override
    public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
        NivreConfig nivreConfig = (NivreConfig)config;
        if (!nivreConfig.isAllowRoot() && nivreConfig.getStack().peek().isRoot()) {
            return this.updateActionContainers(history, 1, null);
        }
        return null;
    }

    @Override
    protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
        ttable.addTransition(1, "SH", false, null);
        ttable.addTransition(2, "RA", true, null);
        ttable.addTransition(3, "LA", true, null);
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
        return "nivrestandard";
    }

    @Override
    public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        currentAction.getAction(this.actionContainers);
        int trans = this.transActionContainer.getActionCode();
        if (!(trans != 3 && trans != 2 || this.isActionContainersLabeled())) {
            return false;
        }
        DependencyNode stackTop = ((NivreConfig)config).getStack().peek();
        if (!((NivreConfig)config).isAllowRoot() && stackTop.isRoot() && trans != 1) {
            return false;
        }
        return trans != 3 || !stackTop.isRoot();
    }

    @Override
    public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
        return this.updateActionContainers(history, 1, null);
    }
}

