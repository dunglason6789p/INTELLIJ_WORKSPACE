/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.HistoryNode;
import org.maltparser.parser.history.action.ActionDecision;
import org.maltparser.parser.history.action.GuideUserAction;

public class HistoryTreeNode
implements HistoryNode {
    private GuideUserAction action;
    private HistoryTreeNode parent;
    private int depth;
    private ArrayList<HistoryTreeNode> children;

    public HistoryTreeNode(HistoryNode previousNode, GuideUserAction action) {
        this.setPreviousNode(this.parent);
        this.setAction(action);
        this.children = new ArrayList();
    }

    @Override
    public GuideUserAction getAction() {
        return this.action;
    }

    @Override
    public void setAction(GuideUserAction action) {
        this.action = action;
    }

    @Override
    public HistoryNode getPreviousNode() {
        return this.parent;
    }

    @Override
    public void setPreviousNode(HistoryNode node) {
        if (node instanceof HistoryTreeNode) {
            this.parent = (HistoryTreeNode)node;
            this.parent.addChild(this);
            this.setDepth(this.parent.getDepth() + 1);
        }
    }

    public int getDepth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void addChild(HistoryTreeNode child) {
        this.children.add(child);
    }

    public void removeChild(HistoryTreeNode child) {
        this.children.remove(child);
    }

    public HistoryTreeNode getChild(ActionDecision childDecision) {
        for (HistoryTreeNode c : this.children) {
            if (!c.getAction().equals(childDecision)) continue;
            return c;
        }
        return null;
    }

    @Override
    public int getPosition() {
        return this.depth;
    }

    @Override
    public void clear() throws MaltChainedException {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.setAction(null);
        this.setPreviousNode(null);
        this.children.clear();
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        int i;
        StringBuilder sb = new StringBuilder();
        for (i = 0; i <= this.depth; ++i) {
            sb.append("  ");
        }
        sb.append(this.action);
        sb.append('\n');
        for (i = 0; i < this.children.size(); ++i) {
            sb.append(this.children.get(i));
        }
        return sb.toString();
    }
}

