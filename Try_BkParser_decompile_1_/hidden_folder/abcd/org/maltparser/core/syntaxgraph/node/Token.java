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
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.DependencyNode;
import org.maltparser.core.syntaxgraph.node.GraphNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class Token
extends GraphNode
implements TokenNode,
DependencyNode,
PhraseStructureNode {
    protected TokenNode predecessor = null;
    protected TokenNode successor = null;
    protected DependencyNode component;
    protected int rank;
    protected int index;
    protected PhraseStructureNode parent = null;
    protected final SortedSet<DependencyNode> heads = new TreeSet<DependencyNode>();
    protected final SortedSet<DependencyNode> leftDependents = new TreeSet<DependencyNode>();
    protected final SortedSet<DependencyNode> rightDependents = new TreeSet<DependencyNode>();

    public Token() throws MaltChainedException {
        this.clear();
    }

    @Override
    public void setPredecessor(TokenNode predecessor) {
        this.predecessor = predecessor;
    }

    @Override
    public void setSuccessor(TokenNode successor) {
        this.successor = successor;
    }

    @Override
    public TokenNode getTokenNodePredecessor() {
        return this.predecessor;
    }

    @Override
    public TokenNode getTokenNodeSuccessor() {
        return this.successor;
    }

    @Override
    public DependencyNode getPredecessor() {
        return this.predecessor;
    }

    @Override
    public DependencyNode getSuccessor() {
        return this.successor;
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
    public void addIncomingEdge(Edge in) throws MaltChainedException {
        super.addIncomingEdge(in);
        if (in.getSource() != null) {
            if (in.getType() == 1 && in.getSource() instanceof DependencyNode) {
                this.heads.add((DependencyNode)((Object)in.getSource()));
            } else if (in.getType() == 2 && in.getSource() instanceof PhraseStructureNode) {
                this.parent = (PhraseStructureNode)((Object)in.getSource());
            }
        }
    }

    @Override
    public void removeIncomingEdge(Edge in) throws MaltChainedException {
        super.removeIncomingEdge(in);
        if (in.getSource() != null) {
            if (in.getType() == 1 && in.getSource() instanceof DependencyNode) {
                this.heads.remove((DependencyNode)((Object)in.getSource()));
            } else if (in.getType() == 2 && in.getSource() instanceof PhraseStructureNode && in.getSource() == this.parent) {
                this.parent = null;
            }
        }
    }

    @Override
    public void addOutgoingEdge(Edge out) throws MaltChainedException {
        super.addOutgoingEdge(out);
        if (out.getType() == 1 && out.getTarget() instanceof DependencyNode) {
            DependencyNode dependent = (DependencyNode)((Object)out.getTarget());
            if (this.compareTo(dependent) > 0) {
                this.leftDependents.add(dependent);
            } else if (this.compareTo(dependent) < 0) {
                this.rightDependents.add(dependent);
            }
        }
    }

    @Override
    public void removeOutgoingEdge(Edge out) throws MaltChainedException {
        super.removeOutgoingEdge(out);
        if (out.getType() == 1 && out.getTarget() instanceof DependencyNode) {
            DependencyNode dependent = (DependencyNode)((Object)out.getTarget());
            if (this.compareTo(dependent) > 0) {
                this.leftDependents.remove(dependent);
            } else if (this.compareTo(dependent) < 0) {
                this.rightDependents.remove(dependent);
            }
        }
    }

    @Override
    public void setIndex(int index) throws MaltChainedException {
        if (index <= 0) {
            throw new SyntaxGraphException("A terminal node must have a positive integer value and not index " + index + ". ");
        }
        this.index = index;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getCompareToIndex() {
        return this.index;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public DependencyNode getAncestor() throws MaltChainedException {
        if (!this.hasHead()) {
            return this;
        }
        DependencyNode tmp = this;
        while (tmp.hasHead()) {
            tmp = tmp.getHead();
        }
        return tmp;
    }

    @Override
    public DependencyNode getProperAncestor() throws MaltChainedException {
        if (!this.hasHead()) {
            return null;
        }
        DependencyNode tmp = this;
        while (tmp.hasHead()) {
            tmp = tmp.getHead();
        }
        return tmp;
    }

    @Override
    public ComparableNode getLeftmostProperDescendant() throws MaltChainedException {
        ComparableNode candidate = null;
        ComparableNode tmp = null;
        for (DependencyNode ldep : this.leftDependents) {
            if (candidate == null) {
                candidate = ldep;
            } else if (ldep.getIndex() < candidate.getIndex()) {
                candidate = ldep;
            }
            if ((tmp = ((Token)ldep).getLeftmostProperDescendant()) == null) continue;
            if (candidate == null) {
                candidate = tmp;
            } else if (tmp.getIndex() < candidate.getIndex()) {
                candidate = tmp;
            }
            if (candidate.getIndex() != 1) continue;
            return candidate;
        }
        for (DependencyNode rdep : this.rightDependents) {
            if (candidate == null) {
                candidate = rdep;
            } else if (rdep.getIndex() < candidate.getIndex()) {
                candidate = rdep;
            }
            if ((tmp = ((Token)rdep).getLeftmostProperDescendant()) == null) continue;
            if (candidate == null) {
                candidate = tmp;
            } else if (tmp.getIndex() < candidate.getIndex()) {
                candidate = tmp;
            }
            if (candidate.getIndex() != 1) continue;
            return candidate;
        }
        return candidate;
    }

    @Override
    public ComparableNode getRightmostProperDescendant() throws MaltChainedException {
        ComparableNode candidate = null;
        ComparableNode tmp = null;
        for (DependencyNode ldep : this.leftDependents) {
            if (candidate == null) {
                candidate = ldep;
            } else if (ldep.getIndex() > candidate.getIndex()) {
                candidate = ldep;
            }
            if ((tmp = ((Token)ldep).getRightmostProperDescendant()) == null) continue;
            if (candidate == null) {
                candidate = tmp;
                continue;
            }
            if (tmp.getIndex() <= candidate.getIndex()) continue;
            candidate = tmp;
        }
        for (DependencyNode rdep : this.rightDependents) {
            if (candidate == null) {
                candidate = rdep;
            } else if (rdep.getIndex() > candidate.getIndex()) {
                candidate = rdep;
            }
            if ((tmp = ((Token)rdep).getRightmostProperDescendant()) == null) continue;
            if (candidate == null) {
                candidate = tmp;
                continue;
            }
            if (tmp.getIndex() <= candidate.getIndex()) continue;
            candidate = tmp;
        }
        return candidate;
    }

    @Override
    public ComparableNode getLeftmostDescendant() throws MaltChainedException {
        ComparableNode candidate = this;
        ComparableNode tmp = null;
        for (DependencyNode ldep : this.leftDependents) {
            if (candidate == null) {
                candidate = ldep;
            } else if (ldep.getIndex() < candidate.getIndex()) {
                candidate = ldep;
            }
            if ((tmp = ((Token)ldep).getLeftmostDescendant()) == null) continue;
            if (candidate == null) {
                candidate = tmp;
            } else if (tmp.getIndex() < candidate.getIndex()) {
                candidate = tmp;
            }
            if (candidate.getIndex() != 1) continue;
            return candidate;
        }
        for (DependencyNode rdep : this.rightDependents) {
            if (candidate == null) {
                candidate = rdep;
            } else if (rdep.getIndex() < candidate.getIndex()) {
                candidate = rdep;
            }
            if ((tmp = ((Token)rdep).getLeftmostDescendant()) == null) continue;
            if (candidate == null) {
                candidate = tmp;
            } else if (tmp.getIndex() < candidate.getIndex()) {
                candidate = tmp;
            }
            if (candidate.getIndex() != 1) continue;
            return candidate;
        }
        return candidate;
    }

    @Override
    public ComparableNode getRightmostDescendant() throws MaltChainedException {
        ComparableNode candidate = this;
        ComparableNode tmp = null;
        for (DependencyNode ldep : this.leftDependents) {
            if (candidate == null) {
                candidate = ldep;
            } else if (ldep.getIndex() > candidate.getIndex()) {
                candidate = ldep;
            }
            if ((tmp = ((Token)ldep).getRightmostDescendant()) == null) continue;
            if (candidate == null) {
                candidate = tmp;
                continue;
            }
            if (tmp.getIndex() <= candidate.getIndex()) continue;
            candidate = tmp;
        }
        for (DependencyNode rdep : this.rightDependents) {
            if (candidate == null) {
                candidate = rdep;
            } else if (rdep.getIndex() > candidate.getIndex()) {
                candidate = rdep;
            }
            if ((tmp = ((Token)rdep).getRightmostDescendant()) == null) continue;
            if (candidate == null) {
                candidate = tmp;
                continue;
            }
            if (tmp.getIndex() <= candidate.getIndex()) continue;
            candidate = tmp;
        }
        return candidate;
    }

    @Override
    public PhraseStructureNode getParent() {
        return this.parent;
    }

    @Override
    public Edge getParentEdge() throws MaltChainedException {
        for (Edge e : this.incomingEdges) {
            if (e.getSource() != this.parent || e.getType() != 2) continue;
            return e;
        }
        return null;
    }

    @Override
    public String getParentEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
        for (Edge e : this.incomingEdges) {
            if (e.getSource() != this.parent || e.getType() != 2) continue;
            return e.getLabelSymbol(table);
        }
        return null;
    }

    @Override
    public int getParentEdgeLabelCode(SymbolTable table) throws MaltChainedException {
        for (Edge e : this.incomingEdges) {
            if (e.getSource() != this.parent || e.getType() != 2) continue;
            return e.getLabelCode(table);
        }
        return -1;
    }

    @Override
    public boolean hasParentEdgeLabel(SymbolTable table) throws MaltChainedException {
        for (Edge e : this.incomingEdges) {
            if (e.getSource() != this.parent || e.getType() != 2) continue;
            return e.hasLabel(table);
        }
        return false;
    }

    @Override
    public boolean hasAtMostOneHead() {
        return this.heads.size() <= 1;
    }

    @Override
    public boolean hasAncestorInside(int left, int right) throws MaltChainedException {
        DependencyNode tmp = this;
        return tmp.getHead() != null && (tmp = tmp.getHead()).getIndex() >= left && tmp.getIndex() <= right;
    }

    @Override
    public Set<Edge> getHeadEdges() throws MaltChainedException {
        return this.incomingEdges;
    }

    @Override
    public Set<DependencyNode> getHeads() throws MaltChainedException {
        return this.heads;
    }

    @Override
    public boolean hasHead() {
        return this.heads.size() != 0;
    }

    @Override
    public DependencyNode getHead() throws MaltChainedException {
        Iterator i$;
        if (this.heads.size() == 0) {
            return null;
        }
        if (this.heads.size() == 1 && (i$ = this.heads.iterator()).hasNext()) {
            DependencyNode head = (DependencyNode)i$.next();
            return head;
        }
        if (this.heads.size() > 1) {
            throw new SyntaxGraphException("The dependency node is multi-headed and it is ambigious to return a single-head dependency node. ");
        }
        return null;
    }

    @Override
    public Edge getHeadEdge() throws MaltChainedException {
        if (this.heads.size() == 0) {
            return null;
        }
        if (this.incomingEdges.size() == 1 && this.incomingEdges.first() instanceof DependencyNode) {
            return (Edge)this.incomingEdges.first();
        }
        if (this.heads.size() == 1) {
            for (Edge e : this.incomingEdges) {
                if (e.getSource() != this.heads.first()) continue;
                return e;
            }
        }
        return null;
    }

    @Override
    public void addHeadEdgeLabel(SymbolTable table, String symbol) throws MaltChainedException {
        if (this.hasHead()) {
            this.getHeadEdge().addLabel(table, symbol);
        }
    }

    @Override
    public void addHeadEdgeLabel(SymbolTable table, int code) throws MaltChainedException {
        if (this.hasHead()) {
            this.getHeadEdge().addLabel(table, code);
        }
    }

    @Override
    public void addHeadEdgeLabel(LabelSet labelSet) throws MaltChainedException {
        if (this.hasHead()) {
            this.getHeadEdge().addLabel(labelSet);
        }
    }

    @Override
    public boolean hasHeadEdgeLabel(SymbolTable table) throws MaltChainedException {
        if (!this.hasHead()) {
            return false;
        }
        return this.getHeadEdge().hasLabel(table);
    }

    @Override
    public String getHeadEdgeLabelSymbol(SymbolTable table) throws MaltChainedException {
        return this.getHeadEdge().getLabelSymbol(table);
    }

    @Override
    public int getHeadEdgeLabelCode(SymbolTable table) throws MaltChainedException {
        if (!this.hasHead()) {
            return 0;
        }
        return this.getHeadEdge().getLabelCode(table);
    }

    @Override
    public boolean isHeadEdgeLabeled() throws MaltChainedException {
        if (!this.hasHead()) {
            return false;
        }
        return this.getHeadEdge().isLabeled();
    }

    @Override
    public int nHeadEdgeLabels() throws MaltChainedException {
        if (!this.hasHead()) {
            return 0;
        }
        return this.getHeadEdge().nLabels();
    }

    @Override
    public Set<SymbolTable> getHeadEdgeLabelTypes() throws MaltChainedException {
        return this.getHeadEdge().getLabelTypes();
    }

    @Override
    public LabelSet getHeadEdgeLabelSet() throws MaltChainedException {
        return this.getHeadEdge().getLabelSet();
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
        if (this.getHead() == null) {
            return null;
        }
        DependencyNode candidate = null;
        for (DependencyNode node : this.getHead().getLeftDependents()) {
            if (node == this) {
                return candidate;
            }
            candidate = node;
        }
        for (DependencyNode node : this.getHead().getRightDependents()) {
            if (node == this) {
                return candidate;
            }
            candidate = node;
        }
        return null;
    }

    @Override
    public DependencyNode getSameSideLeftSibling() throws MaltChainedException {
        if (this.getHead() == null) {
            return null;
        }
        if (this.getIndex() < this.getHead().getIndex()) {
            try {
                return this.getHead().getLeftDependents().headSet(this).last();
            }
            catch (NoSuchElementException e) {
                return null;
            }
        }
        if (this.getIndex() > this.getHead().getIndex()) {
            try {
                return this.getHead().getRightDependents().headSet(this).last();
            }
            catch (NoSuchElementException e) {
                return null;
            }
        }
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
        if (this.getHead() == null) {
            return null;
        }
        for (DependencyNode node : this.getHead().getLeftDependents()) {
            if (node.getIndex() <= this.getIndex()) continue;
            return node;
        }
        for (DependencyNode node : this.getHead().getRightDependents()) {
            if (node.getIndex() <= this.getIndex()) continue;
            return node;
        }
        return null;
    }

    @Override
    public DependencyNode getSameSideRightSibling() throws MaltChainedException {
        if (this.getHead() == null) {
            return null;
        }
        if (this.getIndex() < this.getHead().getIndex()) {
            SortedSet<DependencyNode> tailSet = this.getHead().getLeftDependents().tailSet(this);
            if (tailSet.size() <= 1) {
                return null;
            }
            return tailSet.toArray(new DependencyNode[tailSet.size()])[1];
        }
        if (this.getIndex() > this.getHead().getIndex()) {
            SortedSet<DependencyNode> tailSet = this.getHead().getRightDependents().tailSet(this);
            if (tailSet.size() <= 1) {
                return null;
            }
            return tailSet.toArray(new DependencyNode[tailSet.size()])[1];
        }
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
    public boolean hasRightDependent() {
        return !this.rightDependents.isEmpty();
    }

    @Override
    public boolean isProjective() throws MaltChainedException {
        if (this.hasHead() && !this.getHead().isRoot()) {
            DependencyNode head = this.getHead();
            if (this.getHead().getIndex() < this.getIndex()) {
                DependencyNode terminals = head;
                DependencyNode tmp = null;
                block0 : do {
                    if (terminals == null || terminals.getSuccessor() == null) {
                        return false;
                    }
                    if (terminals.getSuccessor() != this) {
                        tmp = terminals = terminals.getSuccessor();
                        do {
                            if (tmp == this || tmp == head) continue block0;
                            if (!tmp.hasHead()) {
                                return false;
                            }
                            tmp = tmp.getHead();
                        } while (true);
                    }
                    break;
                } while (true);
            } else {
                DependencyNode terminals = this;
                DependencyNode tmp = null;
                block2 : do {
                    if (terminals == null || terminals.getSuccessor() == null) {
                        return false;
                    }
                    if (terminals.getSuccessor() == head) break;
                    terminals = terminals.getSuccessor();
                    tmp = terminals;
                    do {
                        if (tmp == this || tmp == head) continue block2;
                        if (!tmp.hasHead()) {
                            return false;
                        }
                        tmp = tmp.getHead();
                    } while (true);
                    break;
                } while (true);
            }
        }
        return true;
    }

    @Override
    public int getDependencyNodeDepth() throws MaltChainedException {
        DependencyNode tmp = this;
        int depth = 0;
        while (tmp.hasHead()) {
            ++depth;
            tmp = tmp.getHead();
        }
        return depth;
    }

    @Override
    public void clear() throws MaltChainedException {
        super.clear();
        this.predecessor = null;
        this.successor = null;
        this.component = this;
        this.rank = 0;
        this.parent = null;
        this.heads.clear();
        this.leftDependents.clear();
        this.rightDependents.clear();
    }

    @Override
    public int compareTo(ComparableNode that) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == that) {
            return 0;
        }
        if (that instanceof TokenNode) {
            if (this.index < that.getCompareToIndex()) {
                return -1;
            }
            if (this.index > that.getCompareToIndex()) {
                return 1;
            }
            return super.compareTo(that);
        }
        if (that instanceof NonTerminalNode) {
            try {
                int thisLCorner = this.index;
                int thatLCorner = that.getLeftmostProperDescendantIndex();
                int thisRCorner = this.index;
                int thatRCorner = that.getRightmostProperDescendantIndex();
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
        }
        if (this.index < that.getCompareToIndex()) {
            return -1;
        }
        if (this.index > that.getCompareToIndex()) {
            return 1;
        }
        return super.compareTo(that);
    }

    @Override
    public boolean equals(Object obj) {
        Token v = (Token)obj;
        if (this.predecessor != v.predecessor || this.successor != v.successor) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == this.predecessor ? 0 : this.predecessor.hashCode());
        hash = 31 * hash + (null == this.successor ? 0 : this.successor.hashCode());
        return 31 * hash + super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        return sb.toString();
    }
}

