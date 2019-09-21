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

public class ArcEager
extends TransitionSystem {
    protected static final int SHIFT = 1;
    protected static final int REDUCE = 2;
    protected static final int RIGHTARC = 3;
    protected static final int LEFTARC = 4;
    protected static final int UNSHIFT = 5;

    public ArcEager(PropagationManager propagationManager) throws MaltChainedException {
        super(propagationManager);
    }

    @Override
    public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        NivreConfig nivreConfig = (NivreConfig)config;
        Stack<DependencyNode> stack = nivreConfig.getStack();
        Stack<DependencyNode> input = nivreConfig.getInput();
        currentAction.getAction(this.actionContainers);
        Edge e = null;
        if (!nivreConfig.isEnforceTree()) {
            switch (this.transActionContainer.getActionCode()) {
                case 4: {
                    e = nivreConfig.getDependencyStructure().addDependencyEdge(input.peek().getIndex(), stack.peek().getIndex());
                    this.addEdgeLabels(e);
                    stack.pop();
                    break;
                }
                case 3: {
                    e = nivreConfig.getDependencyStructure().addDependencyEdge(stack.peek().getIndex(), input.peek().getIndex());
                    this.addEdgeLabels(e);
                    stack.push(input.pop());
                    break;
                }
                case 2: {
                    stack.pop();
                    break;
                }
                default: {
                    stack.push(input.pop());
                    break;
                }
            }
        } else {
            switch (this.transActionContainer.getActionCode()) {
                case 4: {
                    e = nivreConfig.getDependencyStructure().addDependencyEdge(input.peek().getIndex(), stack.peek().getIndex());
                    this.addEdgeLabels(e);
                    stack.pop();
                    break;
                }
                case 3: {
                    e = nivreConfig.getDependencyStructure().addDependencyEdge(stack.peek().getIndex(), input.peek().getIndex());
                    this.addEdgeLabels(e);
                    stack.push(input.pop());
                    if (!input.isEmpty() || nivreConfig.isEnd()) break;
                    nivreConfig.setEnd(true);
                    break;
                }
                case 2: {
                    stack.pop();
                    break;
                }
                case 5: {
                    input.push(stack.pop());
                    break;
                }
                default: {
                    stack.push(input.pop());
                    if (!input.isEmpty() || nivreConfig.isEnd()) break;
                    nivreConfig.setEnd(true);
                }
            }
        }
    }

    @Override
    public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
        NivreConfig nivreConfig = (NivreConfig)config;
        if (!nivreConfig.isEnforceTree()) {
            if (!nivreConfig.isAllowRoot() && nivreConfig.getStack().peek().isRoot()) {
                return this.updateActionContainers(history, 1, null);
            }
        } else {
            if (!nivreConfig.isAllowRoot() && nivreConfig.getStack().peek().isRoot() && !nivreConfig.isEnd()) {
                return this.updateActionContainers(history, 1, null);
            }
            if (nivreConfig.getInput().isEmpty() && nivreConfig.getStack().peek().hasHead()) {
                return this.updateActionContainers(history, 2, null);
            }
            if (nivreConfig.getInput().isEmpty() && !nivreConfig.getStack().peek().hasHead()) {
                return this.updateActionContainers(history, 5, null);
            }
        }
        return null;
    }

    @Override
    protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
        ttable.addTransition(1, "SH", false, null);
        ttable.addTransition(2, "RE", false, null);
        ttable.addTransition(3, "RA", true, null);
        ttable.addTransition(4, "LA", true, null);
        ttable.addTransition(5, "USH", false, null);
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
        return "nivreeager";
    }

    @Override
    public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        currentAction.getAction(this.actionContainers);
        int trans = this.transActionContainer.getActionCode();
        NivreConfig nivreConfig = (NivreConfig)config;
        DependencyNode stackPeek = nivreConfig.getStack().peek();
        if (!(trans != 4 && trans != 3 || this.isActionContainersLabeled())) {
            return false;
        }
        if ((trans == 4 || trans == 2) && stackPeek.isRoot()) {
            return false;
        }
        if (trans == 4 && stackPeek.hasHead()) {
            return false;
        }
        if (trans == 2 && !stackPeek.hasHead() && !nivreConfig.isAllowReduce()) {
            return false;
        }
        return trans != 1 || !nivreConfig.isEnforceTree() || !nivreConfig.isEnd();
    }

    @Override
    public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
        return this.updateActionContainers(history, 1, null);
    }
}

