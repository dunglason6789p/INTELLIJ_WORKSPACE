/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.planar;

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
import org.maltparser.parser.algorithm.planar.PlanarConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class PlanarArcEagerOracle
extends Oracle {
    public PlanarArcEagerOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
        super(manager, history);
        this.setGuideName("Planar");
    }

    @Override
    public GuideUserAction predict(DependencyStructure gold, ParserConfiguration config) throws MaltChainedException {
        PlanarConfig planarConfig = (PlanarConfig)config;
        DependencyStructure dg = planarConfig.getDependencyGraph();
        DependencyNode stackPeek = planarConfig.getStack().peek();
        int stackPeekIndex = stackPeek.getIndex();
        int inputPeekIndex = planarConfig.getInput().peek().getIndex();
        if (!stackPeek.isRoot() && gold.getTokenNode(stackPeekIndex).getHead().getIndex() == inputPeekIndex && !this.checkIfArcExists(dg, inputPeekIndex, stackPeekIndex)) {
            return this.updateActionContainers(4, gold.getTokenNode(stackPeekIndex).getHeadEdge().getLabelSet());
        }
        if (gold.getTokenNode(inputPeekIndex).getHead().getIndex() == stackPeekIndex && !this.checkIfArcExists(dg, stackPeekIndex, inputPeekIndex)) {
            return this.updateActionContainers(3, gold.getTokenNode(inputPeekIndex).getHeadEdge().getLabelSet());
        }
        if (gold.getTokenNode(inputPeekIndex).hasLeftDependent() && gold.getTokenNode(inputPeekIndex).getLeftmostDependent().getIndex() < stackPeekIndex) {
            return this.updateActionContainers(2, null);
        }
        if (!(gold.getTokenNode(inputPeekIndex).getHead().getIndex() >= stackPeekIndex || gold.getTokenNode(inputPeekIndex).getHead().isRoot() && planarConfig.getRootHandling() != 1)) {
            return this.updateActionContainers(2, null);
        }
        return this.updateActionContainers(1, null);
    }

    private boolean checkIfArcExists(DependencyStructure dg, int index1, int index2) throws MaltChainedException {
        return dg.getTokenNode(index2).hasHead() && dg.getTokenNode(index2).getHead().getIndex() == index1;
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
    }

    @Override
    public void terminate() throws MaltChainedException {
    }
}

