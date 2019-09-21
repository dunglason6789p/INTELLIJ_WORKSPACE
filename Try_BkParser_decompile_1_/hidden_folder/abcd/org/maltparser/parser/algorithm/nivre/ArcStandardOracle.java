/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.nivre;

import java.util.Stack;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.Oracle;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.algorithm.nivre.NivreConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class ArcStandardOracle
extends Oracle {
    public ArcStandardOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
        super(manager, history);
        this.setGuideName("ArcStandard");
    }

    @Override
    public GuideUserAction predict(DependencyStructure gold, ParserConfiguration config) throws MaltChainedException {
        NivreConfig nivreConfig = (NivreConfig)config;
        DependencyNode stackPeek = nivreConfig.getStack().peek();
        int stackPeekIndex = stackPeek.getIndex();
        int inputPeekIndex = nivreConfig.getInput().peek().getIndex();
        if (!nivreConfig.isAllowRoot() && stackPeek.isRoot()) {
            return this.updateActionContainers(1, null);
        }
        if (!stackPeek.isRoot() && gold.getTokenNode(stackPeekIndex).getHead().getIndex() == inputPeekIndex) {
            return this.updateActionContainers(3, gold.getTokenNode(stackPeekIndex).getHeadEdge().getLabelSet());
        }
        if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() == stackPeekIndex && this.checkRightDependent(gold, nivreConfig.getDependencyGraph(), inputPeekIndex)) {
            return this.updateActionContainers(2, gold.getTokenNode(inputPeekIndex).getHeadEdge().getLabelSet());
        }
        return this.updateActionContainers(1, null);
    }

    private boolean checkRightDependent(DependencyStructure gold, DependencyStructure parseDependencyGraph, int inputPeekIndex) throws MaltChainedException {
        if (gold.getTokenNode(inputPeekIndex).getRightmostDependent() == null) {
            return true;
        }
        return parseDependencyGraph.getTokenNode(inputPeekIndex).getRightmostDependent() != null && gold.getTokenNode(inputPeekIndex).getRightmostDependent().getIndex() == parseDependencyGraph.getTokenNode(inputPeekIndex).getRightmostDependent().getIndex();
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
    }

    @Override
    public void terminate() throws MaltChainedException {
    }
}

