/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.headrules.Direction;
import org.maltparser.core.syntaxgraph.headrules.HeadRules;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.GraphNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.NonTerminal;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.Token;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class Root
extends GraphNode
implements DependencyNode,
PhraseStructureNode,
NonTerminalNode {
    protected final SortedSet<DependencyNode> leftDependents = new TreeSet<DependencyNode>();
    protected final SortedSet<DependencyNode> rightDependents = new TreeSet<DependencyNode>();
    protected final SortedSet<PhraseStructureNode> children = new TreeSet<PhraseStructureNode>();
    protected DependencyNode component;
    protected int rank;

    public Root() throws MaltChainedException {
        this.clear();
    }

    @Override
    public void addIncomingEdge(Edge in) throws MaltChainedException {
        throw new SyntaxGraphException("It is not allowed for a root node to have an incoming edge");
    }

    @Override
    public void removeIncomingEdge(Edge in) {
    }

    @Override
    public void addOutgoingEdge(Edge out) throws MaltChainedException {
        super.addOutgoingEdge(out);
        if (out.getTarget() != null) {
            if (out.getType() == 1 && out.getTarget() instanceof DependencyNode) {
                Node dependent = out.getTarget();
                if (this.compareTo(dependent) > 0) {
                    this.leftDependents.add((DependencyNode)((Object)dependent));
                } else if (this.compareTo(dependent) < 0) {
                    this.rightDependents.add((DependencyNode)((Object)dependent));
                }
            } else if (out.getType() == 2 && out.getTarget() instanceof PhraseStructureNode) {
                this.children.add((PhraseStructureNode)((Object)out.getTarget()));
            }
        }
    }

    @Override
    public void removeOutgoingEdge(Edge out) throws MaltChainedException {
        super.removeOutgoingEdge(out);
        if (out.getTarget() != null) {
            if (out.getType() == 1 && out.getTarget() instanceof DependencyNode) {
                Node dependent = out.getTarget();
                if (this.compareTo(dependent) > 0) {
                    this.leftDependents.remove((DependencyNode)((Object)dependent));
                } else if (this.compareTo(dependent) < 0) {
                    this.rightDependents.remove((DependencyNode)((Object)dependent));
                }
            } else if (out.getType() == 2 && out.getTarget() instanceof PhraseStructureNode) {
                this.children.remove((PhraseStructureNode)((Object)out.getTarget()));
            }
        }
    }

    @Override
    public DependencyNode getPredecessor() {
        return null;
    }

    @Override
    public DependencyNode getSuccessor() {
        return null;
    }

    @Override
    public DependencyNode getAncestor() throws MaltChainedException {
        return this;
    }

    @Override
    public DependencyNode getProperAncestor() throws MaltChainedException {
        return null;
    }

    @Override
    public int getRank() {
        return this.rank;
    }

    @Override
    public void setRank(int r) {
        this.rank = r;
    }

    @Override
    public DependencyNode findComponent() {
        return this.findComponent(this);
    }

    private DependencyNode findComponent(DependencyNode x) {
        if (x != x.getComponent()) {
            x.setComponent(this.findComponent(x.getComponent()));
        }
        return x.getComponent();
    }

    @Override
    public DependencyNode getComponent() {
        return this.component;
    }

    @Override
    public void setComponent(DependencyNode x) {
        this.component = x;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isContinuousExcludeTerminalsAttachToRoot() {
        return true;
    }

    @Override
    public PhraseStructureNode getParent() {
        return null;
    }

    @Override
    public Edge getParentEdge() throws MaltChainedException {
        return null;
    }

    @Override
    public String getParentEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
        return null;
    }

    @Override
    public int getParentEdgeLabelCode(SymbolTable table) throws MaltChainedException {
        return -1;
    }

    @Override
    public boolean hasParentEdgeLabel(SymbolTable table) throws MaltChainedException {
        return false;
    }

    @Override
    public SortedSet<PhraseStructureNode> getChildren() {
        return new TreeSet<PhraseStructureNode>(this.children);
    }

    @Override
    public PhraseStructureNode getChild(int index) {
        if (index >= 0 && index < this.children.size()) {
            return this.children.toArray(new PhraseStructureNode[this.children.size()])[index];
        }
        return null;
    }

    @Override
    public PhraseStructureNode getLeftChild() {
        Iterator i$ = this.children.iterator();
        if (i$.hasNext()) {
            PhraseStructureNode node = (PhraseStructureNode)i$.next();
            return node;
        }
        return null;
    }

    @Override
    public PhraseStructureNode getRightChild() {
        int n = this.children.size();
        int i = 1;
        for (PhraseStructureNode node : this.children) {
            if (i != n) continue;
            return node;
        }
        return null;
    }

    @Override
    public int nChildren() {
        return this.children.size();
    }

    @Override
    public boolean hasNonTerminalChildren() {
        for (PhraseStructureNode node : this.children) {
            if (!(node instanceof NonTerminal)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasTerminalChildren() {
        for (PhraseStructureNode node : this.children) {
            if (!(node instanceof Token)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        int max = -1;
        for (PhraseStructureNode node : this.children) {
            if (node instanceof Token) {
                if (max >= 0) continue;
                max = 0;
                continue;
            }
            int nodeheight = ((NonTerminalNode)node).getHeight();
            if (max >= nodeheight) continue;
            max = nodeheight;
        }
        return max + 1;
    }

    @Override
    public TokenNode getLexicalHead(HeadRules headRules) throws MaltChainedException {
        return this.identifyHead(headRules);
    }

    @Override
    public PhraseStructureNode getHeadChild(HeadRules headRules) throws MaltChainedException {
        return this.identifyHeadChild(headRules);
    }

    @Override
    public TokenNode getLexicalHead() throws MaltChainedException {
        return this.identifyHead(null);
    }

    @Override
    public PhraseStructureNode getHeadChild() throws MaltChainedException {
        return this.identifyHeadChild(null);
    }

    private PhraseStructureNode identifyHeadChild(HeadRules headRules) throws MaltChainedException {
        PhraseStructureNode headChild;
        PhraseStructureNode phraseStructureNode = headChild = headRules == null ? null : headRules.getHeadChild(this);
        if (headChild == null) {
            Direction direction;
            Direction direction2 = direction = headRules == null ? Direction.LEFT : headRules.getDefaultDirection(this);
            if (direction == Direction.LEFT) {
                headChild = this.leftmostTerminalChild();
                if (headChild == null) {
                    headChild = this.leftmostNonTerminalChild();
                }
            } else {
                headChild = this.rightmostTerminalChild();
                if (headChild == null) {
                    headChild = this.rightmostNonTerminalChild();
                }
            }
        }
        return headChild;
    }

    @Override
    public TokenNode identifyHead(HeadRules headRules) throws MaltChainedException {
        PhraseStructureNode headChild = this.identifyHeadChild(headRules);
        TokenNode lexicalHead = null;
        if (headChild instanceof NonTerminalNode) {
            lexicalHead = ((NonTerminalNode)headChild).identifyHead(headRules);
        } else if (headChild instanceof TokenNode) {
            lexicalHead = (TokenNode)headChild;
        }
        for (PhraseStructureNode node : this.children) {
            if (node == headChild || !(node instanceof NonTerminalNode)) continue;
            ((NonTerminalNode)node).identifyHead(headRules);
        }
        return lexicalHead;
    }

    private PhraseStructureNode leftmostTerminalChild() {
        for (PhraseStructureNode node : this.children) {
            if (!(node instanceof TokenNode)) continue;
            return node;
        }
        return null;
    }

    private PhraseStructureNode leftmostNonTerminalChild() {
        for (PhraseStructureNode node : this.children) {
            if (!(node instanceof NonTerminalNode)) continue;
            return node;
        }
        return null;
    }

    private PhraseStructureNode rightmostTerminalChild() {
        try {
            if (this.children.last() instanceof TokenNode) {
                return this.children.last();
            }
        }
        catch (NoSuchElementException e) {
            // empty catch block
        }
        PhraseStructureNode candidate = null;
        for (PhraseStructureNode node : this.children) {
            if (!(node instanceof TokenNode)) continue;
            candidate = node;
        }
        return candidate;
    }

    private PhraseStructureNode rightmostNonTerminalChild() {
        try {
            if (this.children.last() instanceof NonTerminalNode) {
                return this.children.last();
            }
        }
        catch (NoSuchElementException e) {
            // empty catch block
        }
        PhraseStructureNode candidate = null;
        for (PhraseStructureNode node : this.children) {
            if (!(node instanceof NonTerminalNode)) continue;
            candidate = node;
        }
        return candidate;
    }

    @Override
    public boolean hasAtMostOneHead() {
        return true;
    }

    @Override
    public boolean hasAncestorInside(int left, int right) throws MaltChainedException {
        return false;
    }

    @Override
    public boolean hasHead() {
        return false;
    }

    @Override
    public DependencyNode getHead() throws MaltChainedException {
        return null;
    }

    @Override
    public Edge getHeadEdge() throws MaltChainedException {
        return null;
    }

    @Override
    public void addHeadEdgeLabel(SymbolTable table, String symbol) throws MaltChainedException {
    }

    @Override
    public void addHeadEdgeLabel(SymbolTable table, int code) throws MaltChainedException {
    }

    @Override
    public void addHeadEdgeLabel(LabelSet labelSet) throws MaltChainedException {
    }

    @Override
    public int getHeadEdgeLabelCode(SymbolTable table) throws MaltChainedException {
        return 0;
    }

    @Override
    public LabelSet getHeadEdgeLabelSet() throws MaltChainedException {
        return null;
    }

    @Override
    public String getHeadEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
        return null;
    }

    @Override
    public Set<SymbolTable> getHeadEdgeLabelTypes() throws MaltChainedException {
        return null;
    }

    @Override
    public boolean hasHeadEdgeLabel(SymbolTable table) throws MaltChainedException {
        return false;
    }

    @Override
    public boolean isHeadEdgeLabeled() throws MaltChainedException {
        return false;
    }

    @Override
    public int nHeadEdgeLabels() throws MaltChainedException {
        return 0;
    }

    @Override
    public Set<Edge> getHeadEdges() throws MaltChainedException {
        return null;
    }

    @Override
    public Set<DependencyNode> getHeads() throws MaltChainedException {
        return null;
    }

    @Override
    public boolean hasDependent() {
        return this.hasLeftDependent() || this.hasRightDependent();
    }

    @Override
    public boolean hasLeftDependent() {
        return !this.leftDependents.isEmpty();
    }

    @Override
    public DependencyNode getLeftDependent(int index) {
        if (0 <= index && index < this.leftDependents.size()) {
            int i = 0;
            for (DependencyNode node : this.leftDependents) {
                if (i == index) {
                    return node;
                }
                ++i;
            }
        }
        return null;
    }

    @Override
    public int getLeftDependentCount() {
        return this.leftDependents.size();
    }

    @Override
    public SortedSet<DependencyNode> getLeftDependents() {
        return this.leftDependents;
    }

    @Override
    public DependencyNode getLeftSibling() throws MaltChainedException {
        return null;
    }

    @Override
    public DependencyNode getSameSideLeftSibling() throws MaltChainedException {
        return null;
    }

    @Override
    public DependencyNode getClosestLeftDependent() {
        try {
            return this.leftDependents.last();
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public DependencyNode getLeftmostDependent() {
        Iterator i$ = this.leftDependents.iterator();
        if (i$.hasNext()) {
            DependencyNode dep = (DependencyNode)i$.next();
            return dep;
        }
        return null;
    }

    @Override
    public DependencyNode getRightDependent(int index) {
        int size = this.rightDependents.size();
        if (index < size) {
            return this.rightDependents.toArray(new DependencyNode[size])[size - 1 - index];
        }
        return null;
    }

    @Override
    public int getRightDependentCount() {
        return this.rightDependents.size();
    }

    @Override
    public SortedSet<DependencyNode> getRightDependents() {
        return this.rightDependents;
    }

    @Override
    public DependencyNode getRightSibling() throws MaltChainedException {
        return null;
    }

    @Override
    public DependencyNode getSameSideRightSibling() throws MaltChainedException {
        return null;
    }

    @Override
    public DependencyNode getClosestRightDependent() {
        Iterator i$ = this.rightDependents.iterator();
        if (i$.hasNext()) {
            DependencyNode dep = (DependencyNode)i$.next();
            return dep;
        }
        return null;
    }

    @Override
    public DependencyNode getRightmostDependent() {
        int n = this.rightDependents.size();
        int i = 1;
        for (DependencyNode node : this.rightDependents) {
            if (i == n) {
                return node;
            }
            ++i;
        }
        return null;
    }

    @Override
    public boolean hasRightDependent() {
        return !this.rightDependents.isEmpty();
    }

    protected void getDependencyDominationSet(SortedSet<DependencyNode> dominationSet) {
        if (this.leftDependents.size() > 0 || this.rightDependents.size() > 0) {
            dominationSet.addAll(this.leftDependents);
            dominationSet.addAll(this.rightDependents);
            for (DependencyNode node : this.leftDependents) {
                ((Token)node).getDependencyDominationSet(dominationSet);
            }
            for (DependencyNode node : this.rightDependents) {
                ((Token)node).getDependencyDominationSet(dominationSet);
            }
        }
    }

    @Override
    public boolean isProjective() throws MaltChainedException {
        return true;
    }

    @Override
    public int getDependencyNodeDepth() throws MaltChainedException {
        return 0;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public int getCompareToIndex() {
        return 0;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public ComparableNode getLeftmostProperDescendant() throws MaltChainedException {
        NonTerminalNode node = this;
        PhraseStructureNode candidate = null;
        while (node != null && (candidate = node.getLeftChild()) != null && !(candidate instanceof TokenNode)) {
            node = (NonTerminalNode)candidate;
        }
        if (candidate == null && candidate instanceof NonTerminalNode) {
            candidate = null;
            DependencyNode dep = null;
            Iterator i$ = ((TokenStructure)this.getBelongsToGraph()).getTokenIndices().iterator();
            while (i$.hasNext()) {
                int index = (Integer)i$.next();
                for (dep = ((TokenStructure)this.getBelongsToGraph()).getTokenNode((int)index); dep != null; dep = dep.getHead()) {
                    if (dep != this) continue;
                    return dep;
                }
            }
        }
        return candidate;
    }

    @Override
    public ComparableNode getRightmostProperDescendant() throws MaltChainedException {
        NonTerminalNode node = this;
        PhraseStructureNode candidate = null;
        while (node != null && (candidate = node.getRightChild()) != null && !(candidate instanceof TokenNode)) {
            node = (NonTerminalNode)candidate;
        }
        if (candidate == null && candidate instanceof NonTerminalNode) {
            candidate = null;
            DependencyNode dep = null;
            for (int i = ((TokenStructure)this.getBelongsToGraph()).getHighestTokenIndex(); i > 0; --i) {
                for (dep = ((TokenStructure)this.getBelongsToGraph()).getTokenNode((int)i); dep != null; dep = dep.getHead()) {
                    if (dep != this) continue;
                    return dep;
                }
            }
        }
        return candidate;
    }

    @Override
    public ComparableNode getLeftmostDescendant() throws MaltChainedException {
        return this.getLeftmostProperDescendant();
    }

    @Override
    public ComparableNode getRightmostDescendant() throws MaltChainedException {
        return this.getRightmostProperDescendant();
    }

    @Override
    public List<DependencyNode> getListOfDependents() {
        ArrayList<DependencyNode> dependentList = new ArrayList<DependencyNode>();
        for (DependencyNode node : this.leftDependents) {
            dependentList.add(node);
        }
        for (DependencyNode node : this.rightDependents) {
            dependentList.add(node);
        }
        return dependentList;
    }

    @Override
    public List<DependencyNode> getListOfLeftDependents() {
        ArrayList<DependencyNode> leftDependentList = new ArrayList<DependencyNode>();
        for (DependencyNode node : this.leftDependents) {
            leftDependentList.add(node);
        }
        return leftDependentList;
    }

    @Override
    public List<DependencyNode> getListOfRightDependents() {
        ArrayList<DependencyNode> rightDependentList = new ArrayList<DependencyNode>();
        for (DependencyNode node : this.rightDependents) {
            rightDependentList.add(node);
        }
        return rightDependentList;
    }

    @Override
    public void setIndex(int index) throws MaltChainedException {
    }

    @Override
    public void clear() throws MaltChainedException {
        super.clear();
        this.component = this;
        this.rank = 0;
        this.leftDependents.clear();
        this.rightDependents.clear();
        this.children.clear();
    }

    @Override
    public int compareTo(ComparableNode o) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == o) {
            return 0;
        }
        try {
            int thatRCorner;
            int thisLCorner = this.getLeftmostProperDescendantIndex();
            int thatLCorner = o instanceof TokenNode ? o.getCompareToIndex() : o.getLeftmostProperDescendantIndex();
            int thisRCorner = this.getRightmostProperDescendantIndex();
            int n = thatRCorner = o instanceof TokenNode ? o.getCompareToIndex() : o.getRightmostProperDescendantIndex();
            if (thisLCorner != -1 && thatLCorner != -1 && thisRCorner != -1 && thatRCorner != -1) {
                if (thisLCorner < thatLCorner && thisRCorner < thatRCorner) {
                    return -1;
                }
                if (thisLCorner > thatLCorner && thisRCorner > thatRCorner) {
                    return 1;
                }
                if (thisLCorner > thatLCorner && thisRCorner < thatRCorner) {
                    return -1;
                }
                if (thisLCorner < thatLCorner && thisRCorner > thatRCorner) {
                    return 1;
                }
            } else {
                if (thisLCorner != -1 && thatLCorner != -1) {
                    if (thisLCorner < thatLCorner) {
                        return -1;
                    }
                    if (thisLCorner > thatLCorner) {
                        return 1;
                    }
                }
                if (thisRCorner != -1 && thatRCorner != -1) {
                    if (thisRCorner < thatRCorner) {
                        return -1;
                    }
                    if (thisRCorner > thatRCorner) {
                        return 1;
                    }
                }
            }
        }
        catch (MaltChainedException e) {
            if (SystemLogger.logger().isDebugEnabled()) {
                SystemLogger.logger().debug("", e);
            } else {
                SystemLogger.logger().error(e.getMessageChain());
            }
            System.exit(1);
        }
        if (0 < o.getCompareToIndex()) {
            return -1;
        }
        if (0 > o.getCompareToIndex()) {
            return 1;
        }
        return super.compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return 217 + super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        return sb.toString();
    }
}

