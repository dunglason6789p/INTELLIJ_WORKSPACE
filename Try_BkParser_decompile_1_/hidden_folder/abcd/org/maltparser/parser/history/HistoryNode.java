/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideUserAction;

public interface HistoryNode {
    public HistoryNode getPreviousNode();

    public GuideUserAction getAction();

    public void setAction(GuideUserAction var1);

    public void setPreviousNode(HistoryNode var1);

    public int getPosition();

    public void clear() throws MaltChainedException;
}

