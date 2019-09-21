/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.history.container.TableContainer;

public interface GuideUserHistory {
    public GuideUserAction getEmptyGuideUserAction() throws MaltChainedException;

    public ArrayList<ActionContainer> getActionContainers();

    public ActionContainer[] getActionContainerArray();

    public int getNumberOfDecisions();

    public void clear() throws MaltChainedException;

    public int getKBestSize();

    public ArrayList<TableContainer> getDecisionTables();

    public ArrayList<TableContainer> getActionTables();
}

