/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.parser.history.HistoryNode;
import org.maltparser.parser.history.HistoryStructure;
import org.maltparser.parser.history.HistoryTreeNode;
import org.maltparser.parser.history.action.GuideUserAction;

public class HistoryTree
extends HistoryStructure {
    private final HistoryTreeNode root;
    protected final ObjectPoolList<HistoryNode> nodePool = new ObjectPoolList<HistoryNode>(){

        @Override
        protected HistoryNode create() throws MaltChainedException {
            return new HistoryTreeNode(null, null);
        }

        @Override
        public void resetObject(HistoryNode o) throws MaltChainedException {
            o.clear();
        }
    };

    public HistoryTree() {
        this.root = new HistoryTreeNode(null, null);
    }

    @Override
    public HistoryNode getNewHistoryNode(HistoryNode previousNode, GuideUserAction action) throws MaltChainedException {
        HistoryNode node = this.nodePool.checkOut();
        node.setAction(action);
        if (previousNode == null) {
            node.setPreviousNode(this.root);
        } else {
            node.setPreviousNode(previousNode);
        }
        return node;
    }

    @Override
    public void clear() throws MaltChainedException {
        this.nodePool.checkInAll();
        this.root.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.root.toString());
        return sb.toString();
    }

}

