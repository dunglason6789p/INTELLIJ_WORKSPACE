/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.stack;

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
import org.maltparser.parser.algorithm.stack.StackConfig;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;

public class ProjectiveOracle
extends Oracle {
    public ProjectiveOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
        super(manager, history);
        this.setGuideName("projective");
    }

    @Override
    public GuideUserAction predict(DependencyStructure gold, ParserConfiguration configuration) throws MaltChainedException {
        StackConfig config = (StackConfig)configuration;
        Stack<DependencyNode> stack = config.getStack();
        if (stack.size() < 2) {
            return this.updateActionContainers(1, null);
        }
        DependencyNode left = (DependencyNode)stack.get(stack.size() - 2);
        int leftIndex = left.getIndex();
        int rightIndex = ((DependencyNode)stack.get(stack.size() - 1)).getIndex();
        if (!left.isRoot() && gold.getTokenNode(leftIndex).getHead().getIndex() == rightIndex) {
            return this.updateActionContainers(3, gold.getTokenNode(leftIndex).getHeadEdge().getLabelSet());
        }
        if (gold.getTokenNode(rightIndex).getHead().getIndex() == leftIndex && this.checkRightDependent(gold, config.getDependencyGraph(), rightIndex)) {
            return this.updateActionContainers(2, gold.getTokenNode(rightIndex).getHeadEdge().getLabelSet());
        }
        return this.updateActionContainers(1, null);
    }

    private boolean checkRightDependent(DependencyStructure gold, DependencyStructure parseDependencyGraph, int index) throws MaltChainedException {
        if (gold.getTokenNode(index).getRightmostDependent() == null) {
            return true;
        }
        return parseDependencyGraph.getTokenNode(index).getRightmostDependent() != null && gold.getTokenNode(index).getRightmostDependent().getIndex() == parseDependencyGraph.getTokenNode(index).getRightmostDependent().getIndex();
    }

    @Override
    public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
    }

    @Override
    public void terminate() throws MaltChainedException {
    }
}

