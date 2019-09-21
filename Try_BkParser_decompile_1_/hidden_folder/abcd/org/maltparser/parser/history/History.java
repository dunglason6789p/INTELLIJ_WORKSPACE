/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history;

import java.util.ArrayList;
import java.util.List;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.pool.ObjectPoolList;
import org.maltparser.core.symbol.Table;
import org.maltparser.core.symbol.TableHandler;
import org.maltparser.parser.history.GuideUserHistory;
import org.maltparser.parser.history.action.ActionDecision;
import org.maltparser.parser.history.action.ComplexDecisionAction;
import org.maltparser.parser.history.action.GuideDecision;
import org.maltparser.parser.history.action.GuideUserAction;
import org.maltparser.parser.history.container.ActionContainer;
import org.maltparser.parser.history.container.CombinedTableContainer;
import org.maltparser.parser.history.container.TableContainer;

public class History
implements GuideUserHistory {
    private final ObjectPoolList<ComplexDecisionAction> actionPool;
    private final int kBestSize;
    private final String separator;
    private final String decisionSettings;
    private final ArrayList<TableContainer> decisionTables;
    private final ArrayList<TableContainer> actionTables;
    private final HashMap<String, TableHandler> tableHandlers;

    public History(String _decisionSettings, String _separator, HashMap<String, TableHandler> _tableHandlers, int _kBestSize) throws MaltChainedException {
        this.tableHandlers = _tableHandlers;
        this.separator = _separator == null || _separator.length() < 1 ? "~" : _separator;
        this.kBestSize = _kBestSize;
        this.decisionTables = new ArrayList();
        this.actionTables = new ArrayList();
        this.decisionSettings = _decisionSettings;
        this.initDecisionSettings();
        this.actionPool = new ObjectPoolList<ComplexDecisionAction>(){

            @Override
            protected ComplexDecisionAction create() throws MaltChainedException {
                return new ComplexDecisionAction(History.this.getThis());
            }

            @Override
            public void resetObject(ComplexDecisionAction o) throws MaltChainedException {
                o.clear();
            }
        };
        this.clear();
    }

    private History getThis() {
        return this;
    }

    @Override
    public GuideUserAction getEmptyGuideUserAction() throws MaltChainedException {
        return (GuideUserAction)((Object)this.getEmptyActionObject());
    }

    @Override
    public ArrayList<ActionContainer> getActionContainers() {
        ArrayList<ActionContainer> actionContainers = new ArrayList<ActionContainer>();
        for (int i = 0; i < this.actionTables.size(); ++i) {
            actionContainers.add(new ActionContainer(this.actionTables.get(i)));
        }
        return actionContainers;
    }

    @Override
    public ActionContainer[] getActionContainerArray() {
        ActionContainer[] actionContainers = new ActionContainer[this.actionTables.size()];
        for (int i = 0; i < this.actionTables.size(); ++i) {
            actionContainers[i] = new ActionContainer(this.actionTables.get(i));
        }
        return actionContainers;
    }

    @Override
    public void clear() throws MaltChainedException {
        this.actionPool.checkInAll();
    }

    public GuideDecision getEmptyGuideDecision() throws MaltChainedException {
        return (GuideDecision)this.getEmptyActionObject();
    }

    @Override
    public int getNumberOfDecisions() {
        return this.decisionTables.size();
    }

    public TableHandler getTableHandler(String name) {
        return this.tableHandlers.get(name);
    }

    @Override
    public int getKBestSize() {
        return this.kBestSize;
    }

    public int getNumberOfActions() {
        return this.actionTables.size();
    }

    @Override
    public ArrayList<TableContainer> getDecisionTables() {
        return this.decisionTables;
    }

    @Override
    public ArrayList<TableContainer> getActionTables() {
        return this.actionTables;
    }

    public HashMap<String, TableHandler> getTableHandlers() {
        return this.tableHandlers;
    }

    public String getSeparator() {
        return this.separator;
    }

    public String getDecisionSettings() {
        return this.decisionSettings;
    }

    private ActionDecision getEmptyActionObject() throws MaltChainedException {
        return this.actionPool.checkOut();
    }

    private void initDecisionSettings() throws MaltChainedException {
        int start = 0;
        int k = 0;
        char prevDecisionSeparator = ' ';
        TableContainer tmp = null;
        StringBuilder sbTableHandler = new StringBuilder();
        StringBuilder sbTable = new StringBuilder();
        int state = 0;
        for (int i = 0; i < this.decisionSettings.length(); ++i) {
            switch (this.decisionSettings.charAt(i)) {
                case '.': {
                    state = 1;
                    break;
                }
                case '+': {
                    tmp = new TableContainer(this.tableHandlers.get(sbTableHandler.toString()).getSymbolTable(sbTable.toString()), sbTableHandler.toString() + "." + sbTable.toString(), '+');
                    this.actionTables.add(tmp);
                    ++k;
                    sbTableHandler.setLength(0);
                    sbTable.setLength(0);
                    state = 0;
                    break;
                }
                case '#': {
                    state = 2;
                    break;
                }
                case ';': {
                    state = 2;
                    break;
                }
                case ',': {
                    state = 2;
                    break;
                }
                default: {
                    if (state == 0) {
                        sbTableHandler.append(this.decisionSettings.charAt(i));
                        break;
                    }
                    if (state != 1) break;
                    sbTable.append(this.decisionSettings.charAt(i));
                }
            }
            if (state != 2 && i != this.decisionSettings.length() - 1) continue;
            char decisionSeparator = this.decisionSettings.charAt(i);
            if (i == this.decisionSettings.length() - 1) {
                decisionSeparator = prevDecisionSeparator;
            }
            tmp = new TableContainer(this.tableHandlers.get(sbTableHandler.toString()).getSymbolTable(sbTable.toString()), sbTableHandler.toString() + "." + sbTable.toString(), decisionSeparator);
            this.actionTables.add(tmp);
            if (++k - start > 1) {
                this.decisionTables.add(new CombinedTableContainer(this.getTableHandler("A"), this.separator, this.actionTables.subList(start, k), decisionSeparator));
            } else {
                this.decisionTables.add(tmp);
            }
            sbTableHandler.setLength(0);
            sbTable.setLength(0);
            state = 0;
            start = k;
            prevDecisionSeparator = decisionSeparator;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

}
