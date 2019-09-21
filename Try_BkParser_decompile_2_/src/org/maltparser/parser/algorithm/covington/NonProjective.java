/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.covington;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.propagation.PropagationManager;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.TransitionSystem;
import org.maltparser.parser.algorithm.covington.CovingtonConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.transition.TransitionTable;

public class NonProjective
extends TransitionSystem {
    protected static final int SHIFT = 1;
    protected static final int NOARC = 2;
    protected static final int RIGHTARC = 3;
    protected static final int LEFTARC = 4;

    public NonProjective(PropagationManager propagationManager) throws MaltChainedException {
        super(propagationManager);
    }

    @Override
    public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        CovingtonConfig covingtonConfig = (CovingtonConfig)config;
        currentAction.getAction(this.actionContainers);
        Edge e = null;
        switch (this.transActionContainer.getActionCode()) {
            case 4: {
                e = covingtonConfig.getDependencyGraph().addDependencyEdge(covingtonConfig.getRightTarget().getIndex(), covingtonConfig.getLeftTarget().getIndex());
                this.addEdgeLabels(e);
                break;
            }
            case 3: {
                e = covingtonConfig.getDependencyGraph().addDependencyEdge(covingtonConfig.getLeftTarget().getIndex(), covingtonConfig.getRightTarget().getIndex());
                this.addEdgeLabels(e);
                break;
            }
        }
        this.update(covingtonConfig, this.transActionContainer.getActionCode());
    }

    private void update(CovingtonConfig covingtonConfig, int trans) {
        if (trans == 1) {
            covingtonConfig.setRight(covingtonConfig.getRight() + 1);
            covingtonConfig.setLeft(covingtonConfig.getRight() - 1);
        } else {
            DependencyNode rightNode = covingtonConfig.getRightTarget();
            int leftstop = covingtonConfig.getLeftstop();
            int left = covingtonConfig.getLeft();
            --left;
            DependencyNode leftNode = null;
            while (left >= leftstop) {
                leftNode = covingtonConfig.getInput().get(left);
                if (rightNode.findComponent().getIndex() != leftNode.findComponent().getIndex() && (!leftNode.hasHead() || !rightNode.hasHead())) break;
                --left;
            }
            if (left < leftstop) {
                covingtonConfig.setRight(covingtonConfig.getRight() + 1);
                covingtonConfig.setLeft(covingtonConfig.getRight() - 1);
            } else {
                covingtonConfig.setLeft(left);
            }
        }
    }

    @Override
    public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
        CovingtonConfig covingtonConfig = (CovingtonConfig)config;
        if (!covingtonConfig.isAllowRoot() && covingtonConfig.getLeftTarget().isRoot()) {
            return this.updateActionContainers(history, 2, null);
        }
        return null;
    }

    @Override
    protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
        ttable.addTransition(1, "SH", false, null);
        ttable.addTransition(2, "NA", false, null);
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
        return "covnonproj";
    }

    @Override
    public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
        CovingtonConfig covingtonConfig = (CovingtonConfig)config;
        DependencyNode leftTarget = covingtonConfig.getLeftTarget();
        DependencyNode rightTarget = covingtonConfig.getRightTarget();
        DependencyStructure dg = covingtonConfig.getDependencyGraph();
        currentAction.getAction(this.actionContainers);
        int trans = this.transActionContainer.getActionCode();
        if (trans == 1 && !covingtonConfig.isAllowShift()) {
            return false;
        }
        if (!(trans != 4 && trans != 3 || this.isActionContainersLabeled())) {
            return false;
        }
        if (trans == 4 && leftTarget.isRoot()) {
            return false;
        }
        if (trans == 4 && dg.hasLabeledDependency(leftTarget.getIndex())) {
            return false;
        }
        return trans != 3 || !dg.hasLabeledDependency(rightTarget.getIndex());
    }

    @Override
    public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
        return this.updateActionContainers(history, 2, null);
    }
}

