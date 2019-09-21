/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.action;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.HistoryException;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.action.MultipleDecision;
import org.maltparser.parser.history.action.SimpleDecisionAction;
import org.maltparser.parser.history.action.SingleDecision;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.history.container.CombinedTableContainer;
import org.maltparser.parser.history.container.TableContainer;
import org.maltparser.parser.history.kbest.KBestList;
import org.maltparser.parser.history.kbest.ScoredKBestList;

public class ComplexDecisionAction
implements GuideUserAction,
MultipleDecision {
    private final GuideUserHistory history;
    private final ArrayList<SimpleDecisionAction> decisions;

    public ComplexDecisionAction(GuideUserHistory history) throws MaltChainedException {
        this.history = history;
        this.decisions = new ArrayList(history.getDecisionTables().size());
        int n = history.getDecisionTables().size();
        for (int i = 0; i < n; ++i) {
            this.decisions.add(new SimpleDecisionAction(history.getKBestSize(), history.getDecisionTables().get(i)));
        }
    }

    @Override
    public void addAction(ArrayList<ActionContainer> actionContainers) throws MaltChainedException {
        if (actionContainers == null || actionContainers.size() != this.history.getActionTables().size()) {
            throw new HistoryException("The action containers does not exist or is not of the same size as the action table. ");
        }
        int j = 0;
        int n = this.history.getDecisionTables().size();
        for (int i = 0; i < n; ++i) {
            if (this.history.getDecisionTables().get(i) instanceof CombinedTableContainer) {
                CombinedTableContainer tableContainer = (CombinedTableContainer)this.history.getDecisionTables().get(i);
                int nContainers = tableContainer.getNumberContainers();
                this.decisions.get(i).addDecision(tableContainer.getCombinedCode(actionContainers.subList(j, j + nContainers)));
                j += nContainers;
                continue;
            }
            this.decisions.get(i).addDecision(actionContainers.get(j).getActionCode());
            ++j;
        }
    }

    @Override
    public void getAction(ArrayList<ActionContainer> actionContainers) throws MaltChainedException {
        if (actionContainers == null || actionContainers.size() != this.history.getActionTables().size()) {
            throw new HistoryException("The action containers does not exist or is not of the same size as the action table. ");
        }
        int j = 0;
        int n = this.history.getDecisionTables().size();
        for (int i = 0; i < n; ++i) {
            if (this.history.getDecisionTables().get(i) instanceof CombinedTableContainer) {
                CombinedTableContainer tableContainer = (CombinedTableContainer)this.history.getDecisionTables().get(i);
                int nContainers = tableContainer.getNumberContainers();
                tableContainer.setActionContainer(actionContainers.subList(j, j + nContainers), this.decisions.get(i).getDecisionCode());
                j += nContainers;
                continue;
            }
            actionContainers.get(j).setAction(this.decisions.get(i).getDecisionCode());
            ++j;
        }
    }

    @Override
    public void addAction(ActionContainer[] actionContainers) throws MaltChainedException {
        if (actionContainers == null || actionContainers.length != this.history.getActionTables().size()) {
            throw new HistoryException("The action containers does not exist or is not of the same size as the action table. ");
        }
        int j = 0;
        int n = this.history.getDecisionTables().size();
        for (int i = 0; i < n; ++i) {
            if (this.history.getDecisionTables().get(i) instanceof CombinedTableContainer) {
                CombinedTableContainer tableContainer = (CombinedTableContainer)this.history.getDecisionTables().get(i);
                int nContainers = tableContainer.getNumberContainers();
                this.decisions.get(i).addDecision(tableContainer.getCombinedCode(actionContainers, j));
                j += nContainers;
                continue;
            }
            this.decisions.get(i).addDecision(actionContainers[j].getActionCode());
            ++j;
        }
    }

    @Override
    public void getAction(ActionContainer[] actionContainers) throws MaltChainedException {
        if (actionContainers == null || actionContainers.length != this.history.getActionTables().size()) {
            throw new HistoryException("The action containers does not exist or is not of the same size as the action table. ");
        }
        int j = 0;
        int n = this.history.getDecisionTables().size();
        for (int i = 0; i < n; ++i) {
            if (this.history.getDecisionTables().get(i) instanceof CombinedTableContainer) {
                CombinedTableContainer tableContainer = (CombinedTableContainer)this.history.getDecisionTables().get(i);
                int nContainers = tableContainer.getNumberContainers();
                tableContainer.setActionContainer(actionContainers, j, this.decisions.get(i).getDecisionCode());
                j += nContainers;
                continue;
            }
            actionContainers[j].setAction(this.decisions.get(i).getDecisionCode());
            ++j;
        }
    }

    public void getKBestLists(ArrayList<ScoredKBestList> kbestListContainers) throws MaltChainedException {
        kbestListContainers.clear();
        int n = this.decisions.size();
        for (int i = 0; i < n; ++i) {
            kbestListContainers.add((ScoredKBestList)this.decisions.get(i).getKBestList());
        }
    }

    public void getKBestLists(ScoredKBestList[] kbestListContainers) throws MaltChainedException {
        int n = this.decisions.size();
        for (int i = 0; i < n; ++i) {
            kbestListContainers[0] = (ScoredKBestList)this.decisions.get(i).getKBestList();
        }
    }

    @Override
    public int numberOfActions() {
        return this.history.getActionTables().size();
    }

    @Override
    public void clear() {
        int n = this.decisions.size();
        for (int i = 0; i < n; ++i) {
            this.decisions.get(i).clear();
        }
    }

    @Override
    public SingleDecision getSingleDecision(int decisionIndex) throws MaltChainedException {
        return this.decisions.get(decisionIndex);
    }

    @Override
    public int numberOfDecisions() {
        return this.history.getDecisionTables().size();
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
        ComplexDecisionAction other = (ComplexDecisionAction)obj;
        if (this.decisions == null) {
            if (other.decisions != null) {
                return false;
            }
        } else {
            if (this.decisions.size() != other.decisions.size()) {
                return false;
            }
            for (int i = 0; i < this.decisions.size(); ++i) {
                try {
                    if (this.decisions.get(i).getDecisionCode() == other.decisions.get(i).getDecisionCode()) continue;
                    return false;
                }
                catch (MaltChainedException e) {
                    System.err.println("Error in equals. ");
                }
            }
        }
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int n = this.decisions.size();
        for (int i = 0; i < n; ++i) {
            sb.append(this.decisions.get(i));
            sb.append(';');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}

