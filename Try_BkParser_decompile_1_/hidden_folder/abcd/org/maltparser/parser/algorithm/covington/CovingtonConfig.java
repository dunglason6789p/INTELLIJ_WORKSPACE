/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.algorithm.covington;

import java.util.ArrayList;
import java.util.SortedSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.parser.ParserConfiguration;
import org.maltparser.parser.ParsingException;
import org.maltparser.parser.history.HistoryNode;

public class CovingtonConfig
extends ParserConfiguration {
    private final ArrayList<DependencyNode> input = new ArrayList();
    private int right;
    private int left;
    private int leftstop;
    private int rightstop;
    private DependencyStructure dependencyGraph;
    private final boolean allowRoot;
    private final boolean allowShift;

    public CovingtonConfig(boolean cr, boolean cs) throws MaltChainedException {
        this.allowRoot = cr;
        this.allowShift = cs;
    }

    public DependencyStructure getDependencyStructure() {
        return this.dependencyGraph;
    }

    public ArrayList<DependencyNode> getInput() {
        return this.input;
    }

    @Override
    public boolean isTerminalState() {
        return this.right > this.rightstop;
    }

    public int getRight() {
        return this.right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getLeft() {
        return this.left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getLeftstop() {
        return this.leftstop;
    }

    public int getRightstop() {
        return this.rightstop;
    }

    public boolean isAllowRoot() {
        return this.allowRoot;
    }

    public boolean isAllowShift() {
        return this.allowShift;
    }

    public DependencyNode getLeftNode(int index) throws MaltChainedException {
        if (index < 0) {
            throw new ParsingException("Left index must be non-negative in feature specification. ");
        }
        if (this.left - index >= 0) {
            return this.input.get(this.left - index);
        }
        return null;
    }

    public DependencyNode getRightNode(int index) throws MaltChainedException {
        if (index < 0) {
            throw new ParsingException("Right index must be non-negative in feature specification. ");
        }
        if (this.right + index < this.input.size()) {
            return this.input.get(this.right + index);
        }
        return null;
    }

    public DependencyNode getLeftContextNode(int index) throws MaltChainedException {
        if (index < 0) {
            throw new ParsingException("LeftContext index must be non-negative in feature specification. ");
        }
        int tmpindex = 0;
        for (int i = this.left + 1; i < this.right; ++i) {
            if (this.input.get(i).hasAncestorInside(this.left, this.right)) continue;
            if (tmpindex == index) {
                return this.input.get(i);
            }
            ++tmpindex;
        }
        return null;
    }

    public DependencyNode getRightContextNode(int index) throws MaltChainedException {
        if (index < 0) {
            throw new ParsingException("RightContext index must be non-negative in feature specification. ");
        }
        int tmpindex = 0;
        for (int i = this.right - 1; i > this.left; --i) {
            if (this.input.get(i).hasAncestorInside(this.left, this.right)) continue;
            if (tmpindex == index) {
                return this.input.get(i);
            }
            ++tmpindex;
        }
        return null;
    }

    public DependencyNode getLeftTarget() {
        return this.input.get(this.left);
    }

    public DependencyNode getRightTarget() {
        return this.input.get(this.right);
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
            CovingtonConfig covingtonConfig = (CovingtonConfig)parserConfiguration;
            ArrayList<DependencyNode> sourceInput = covingtonConfig.getInput();
            this.setDependencyGraph(covingtonConfig.getDependencyGraph());
            int n = sourceInput.size();
            for (int i = 0; i < n; ++i) {
                this.input.add(this.dependencyGraph.getDependencyNode(sourceInput.get(i).getIndex()));
            }
            this.left = covingtonConfig.getLeft();
            this.right = covingtonConfig.getRight();
            this.rightstop = covingtonConfig.getRightstop();
            this.leftstop = covingtonConfig.getLeftstop();
        } else {
            int n = this.dependencyGraph.getHighestTokenIndex();
            for (int i = 0; i <= n; ++i) {
                DependencyNode node = this.dependencyGraph.getDependencyNode(i);
                if (node == null) continue;
                this.input.add(node);
            }
            this.leftstop = this.allowRoot ? 0 : 1;
            this.rightstop = this.dependencyGraph.getHighestTokenIndex();
            this.left = this.leftstop;
            this.right = this.left + 1;
        }
    }

    @Override
    public void initialize() throws MaltChainedException {
        int n = this.dependencyGraph.getHighestTokenIndex();
        for (int i = 0; i <= n; ++i) {
            DependencyNode node = this.dependencyGraph.getDependencyNode(i);
            if (node == null) continue;
            this.input.add(node);
        }
        this.leftstop = this.allowRoot ? 0 : 1;
        this.rightstop = this.dependencyGraph.getHighestTokenIndex();
        this.left = this.leftstop;
        this.right = this.left + 1;
    }

    @Override
    public void clear() throws MaltChainedException {
        this.input.clear();
        this.historyNode = null;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        CovingtonConfig that = (CovingtonConfig)obj;
        if (this.input.size() != that.getInput().size()) {
            return false;
        }
        if (this.dependencyGraph.nEdges() != that.getDependencyGraph().nEdges()) {
            return false;
        }
        for (int i = 0; i < this.input.size(); ++i) {
            if (this.input.get(i).getIndex() == that.getInput().get(i).getIndex()) continue;
            return false;
        }
        return this.dependencyGraph.getEdges().equals(that.getDependencyGraph().getEdges());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.input.size());
        sb.append(", ");
        sb.append(this.dependencyGraph.nEdges());
        return sb.toString();
    }
}

