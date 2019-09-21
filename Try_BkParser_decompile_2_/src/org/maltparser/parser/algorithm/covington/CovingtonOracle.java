/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.covington;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.Oracle;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.algorithm.covington.CovingtonConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class CovingtonOracle
extends Oracle {
    public CovingtonOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
        super(manager, history);
        this.setGuideName("NonProjective");
    }

    @Override
    public GuideUserAction predict(DependencyStructure gold, ParserConfiguration config) throws MaltChainedException {
        CovingtonConfig covingtonConfig = (CovingtonConfig)config;
        DependencyNode leftTarget = covingtonConfig.getLeftTarget();
        int leftTargetIndex = leftTarget.getIndex();
        int rightTargetIndex = covingtonConfig.getRightTarget().getIndex();
        if (!leftTarget.isRoot() && gold.getTokenNode(leftTargetIndex).getHead().getIndex() == rightTargetIndex) {
            return this.updateActionContainers(4, gold.getTokenNode(leftTargetIndex).getHeadEdge().getLabelSet());
        }
        if (gold.getTokenNode(rightTargetIndex).getHead().getIndex() == leftTargetIndex) {
            return this.updateActionContainers(3, gold.getTokenNode(rightTargetIndex).getHeadEdge().getLabelSet());
        }
        if (covingtonConfig.isAllowShift() && (!gold.getTokenNode(rightTargetIndex).hasLeftDependent() || gold.getTokenNode(rightTargetIndex).getLeftmostDependent().getIndex() >= leftTargetIndex) && (gold.getTokenNode(rightTargetIndex).getHead().getIndex() >= leftTargetIndex || gold.getTokenNode(rightTargetIndex).getHead().isRoot() && covingtonConfig.getLeftstop() != 0)) {
            return this.updateActionContainers(1, null);
        }
        return this.updateActionContainers(2, null);
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
    }

    @Override
    public void terminate() throws MaltChainedException {
    }
}

