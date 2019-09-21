/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.HistoryNode;
import org.maltparser.parser.history.action.GuideUserAction;

public class HistoryListNode
implements HistoryNode {
    private HistoryNode previousNode;
    private GuideUserAction action;
    private int position;

    public HistoryListNode(HistoryNode _previousNode, GuideUserAction _action) {
        this.previousNode = _previousNode;
        this.action = _action;
        this.position = this.previousNode != null ? this.previousNode.getPosition() + 1 : 1;
    }

    @Override
    public HistoryNode getPreviousNode() {
        return this.previousNode;
    }

    @Override
    public GuideUserAction getAction() {
        return this.action;
    }

    @Override
    public void setPreviousNode(HistoryNode node) {
        this.previousNode = node;
    }

    @Override
    public void setAction(GuideUserAction action) {
        this.action = action;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Override
    public void clear() throws MaltChainedException {
        this.previousNode = null;
        this.action = null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.action);
        return sb.toString();
    }
}

