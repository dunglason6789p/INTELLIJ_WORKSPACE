/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.node;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.SystemLogger;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.LabeledStructure;
import org.maltparser.core.syntaxgraph.SyntaxGraphException;
import org.maltparser.core.syntaxgraph.TokenStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.headrules.Direction;
import org.maltparser.core.syntaxgraph.headrules.HeadRules;
import org.maltparser.core.syntaxgraph.node.ComparableNode;
import org.maltparser.core.syntaxgraph.node.GraphNode;
import org.maltparser.core.syntaxgraph.node.Node;
import org.maltparser.core.syntaxgraph.node.NonTerminalNode;
import org.maltparser.core.syntaxgraph.node.PhraseStructureNode;
import org.maltparser.core.syntaxgraph.node.Token;
import org.maltparser.core.syntaxgraph.node.TokenNode;

public class NonTerminal
extends GraphNode
implements PhraseStructureNode,
NonTerminalNode {
    public static final int INDEX_OFFSET = 10000000;
    protected final SortedSet<PhraseStructureNode> children = new TreeSet<PhraseStructureNode>();
    protected PhraseStructureNode parent;
    protected int index = -1;

    @Override
    public void addIncomingEdge(Edge in) throws MaltChainedException {
        super.addIncomingEdge(in);
        if (in.getType() == 2 && in.getSource() instanceof PhraseStructureNode) {
            this.parent = (PhraseStructureNode)((Object)in.getSource());
        }
    }

    @Override
    public void removeIncomingEdge(Edge in) throws MaltChainedException {
        super.removeIncomingEdge(in);
        if (in.getType() == 2 && in.getSource() instanceof PhraseStructureNode && in.getSource() == this.parent) {
            this.parent = null;
        }
    }

    @Override
    public void addOutgoingEdge(Edge out) throws MaltChainedException {
        super.addOutgoingEdge(out);
        if (out.getType() == 2 && out.getTarget() instanceof PhraseStructureNode) {
            this.children.add((PhraseStructureNode)((Object)out.getTarget()));
        }
    }

    @Override
    public void removeOutgoingEdge(Edge out) throws MaltChainedException {
        super.removeOutgoingEdge(out);
        if (out.getType() == 2 && out.getTarget() instanceof PhraseStructureNode) {
            this.children.remove(out.getTarget());
        }
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
    public SortedSet<PhraseStructureNode> getChildren() {
        return new TreeSet<PhraseStructureNode>(this.children);
    }

    @Override
    public PhraseStructureNode getChild(int index) {
        if (index >= 0 && index < this.children.size()) {
            int i = 0;
            for (PhraseStructureNode node : this.children) {
                if (i == index) {
                    return node;
                }
                ++i;
            }
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
            if (i == n) {
                return node;
            }
            ++i;
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

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getCompareToIndex() {
        return this.index + 10000000;
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
    public boolean isContinuous() {
        int rcorner;
        int lcorner = this.getLeftmostProperDescendant().getIndex();
        if (lcorner == (rcorner = this.getRightmostProperDescendant().getIndex())) {
            return true;
        }
        TokenNode terminal = ((TokenStructure)this.getBelongsToGraph()).getTokenNode(lcorner);
        while (terminal.getIndex() != rcorner) {
            for (PhraseStructureNode tmp = terminal.getParent(); tmp != this; tmp = tmp.getParent()) {
                if (tmp != null) continue;
                return false;
            }
            terminal = terminal.getTokenNodeSuccessor();
        }
        return true;
    }

    @Override
    public boolean isContinuousExcludeTerminalsAttachToRoot() {
        int rcorner;
        int lcorner = this.getLeftmostProperDescendant().getIndex();
        if (lcorner == (rcorner = this.getRightmostProperDescendant().getIndex())) {
            return true;
        }
        TokenNode terminal = ((TokenStructure)this.getBelongsToGraph()).getTokenNode(lcorner);
        while (terminal.getIndex() != rcorner) {
            if (terminal.getParent() != null && terminal.getParent().isRoot()) {
                terminal = terminal.getTokenNodeSuccessor();
                continue;
            }
            for (PhraseStructureNode tmp = terminal.getParent(); tmp != this; tmp = tmp.getParent()) {
                if (tmp != null) continue;
                return false;
            }
            terminal = terminal.getTokenNodeSuccessor();
        }
        return true;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public ComparableNode getLeftmostProperDescendant() {
        NonTerminalNode node = this;
        PhraseStructureNode candidate = null;
        while (node != null) {
            candidate = node.getLeftChild();
            if (candidate == null || candidate instanceof TokenNode) {
                return candidate;
            }
            node = (NonTerminalNode)candidate;
        }
        return null;
    }

    @Override
    public ComparableNode getRightmostProperDescendant() {
        NonTerminalNode node = this;
        PhraseStructureNode candidate = null;
        while (node != null) {
            candidate = node.getRightChild();
            if (candidate == null || candidate instanceof TokenNode) {
                return candidate;
            }
            node = (NonTerminalNode)candidate;
        }
        return null;
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
    public void setIndex(int index) throws MaltChainedException {
        if (index <= 0) {
            throw new SyntaxGraphException("The index must be a positive index");
        }
        this.index = index;
    }

    @Override
    public void clear() throws MaltChainedException {
        super.clear();
        this.children.clear();
        this.parent = null;
        this.index = -1;
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
        if (this.getCompareToIndex() < o.getCompareToIndex()) {
            return -1;
        }
        if (this.getCompareToIndex() > o.getCompareToIndex()) {
            return 1;
        }
        return super.compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NonTerminal)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return 217 + super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getIndex());
        sb.append('\t');
        for (SymbolTable table : this.getLabelTypes()) {
            try {
                sb.append(this.getLabelSymbol(table));
            }
            catch (MaltChainedException e) {
                System.err.println("Print error : " + e.getMessageChain());
            }
            sb.append('\t');
        }
        return sb.toString();
    }
}

