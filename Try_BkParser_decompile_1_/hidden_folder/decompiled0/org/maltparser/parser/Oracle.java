/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.syntaxgraph.LabelSet;
import org.maltparser.parser.DependencyParserConfig;
import org.maltparser.parser.ParsingException;
import org.maltparser.parser.guide.OracleGuide;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;

public abstract class Oracle
implements OracleGuide {
    private final DependencyParserConfig manager;
    private final GuideUserHistory history;
    private String name;
    protected final ActionContainer[] actionContainers;
    protected ActionContainer transActionContainer;
    protected final ActionContainer[] arcLabelActionContainers;

    public Oracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
        this.manager = manager;
        this.history = history;
        this.actionContainers = history.getActionContainerArray();
        if (this.actionContainers.length < 1) {
            throw new ParsingException("Problem when initialize the history (sequence of actions). There are no action containers. ");
        }
        int nLabels = 0;
        for (int i = 0; i < this.actionContainers.length; ++i) {
            if (!this.actionContainers[i].getTableContainerName().startsWith("A.")) continue;
            ++nLabels;
        }
        int j = 0;
        this.arcLabelActionContainers = new ActionContainer[nLabels];
        for (int i = 0; i < this.actionContainers.length; ++i) {
            if (this.actionContainers[i].getTableContainerName().equals("T.TRANS")) {
                this.transActionContainer = this.actionContainers[i];
                continue;
            }
            if (!this.actionContainers[i].getTableContainerName().startsWith("A.")) continue;
            this.arcLabelActionContainers[j++] = this.actionContainers[i];
        }
    }

    public GuideUserHistory getHistory() {
        return this.history;
    }

    @Override
    public DependencyParserConfig getConfiguration() {
        return this.manager;
    }

    @Override
    public String getGuideName() {
        return this.name;
    }

    @Override
    public void setGuideName(String guideName) {
        this.name = guideName;
    }

    protected GuideUserAction updateActionContainers(int transition, LabelSet arcLabels) throws MaltChainedException {
        int i;
        this.transActionContainer.setAction(transition);
        if (arcLabels == null) {
            for (i = 0; i < this.arcLabelActionContainers.length; ++i) {
                this.arcLabelActionContainers[i].setAction(-1);
            }
        } else {
            for (i = 0; i < this.arcLabelActionContainers.length; ++i) {
                this.arcLabelActionContainers[i].setAction(((Integer)arcLabels.get(this.arcLabelActionContainers[i].getTable())).shortValue());
            }
        }
        GuideUserAction oracleAction = this.history.getEmptyGuideUserAction();
        oracleAction.addAction(this.actionContainers);
        return oracleAction;
    }
}

