/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.container;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;
import org.maltparser.parser.history.container.TableContainer;

public class ActionContainer {
    private final Table table;
    private final String name;
    private int actionCode;
    private String actionSymbol;

    public ActionContainer(TableContainer tableContainer) {
        this.table = tableContainer.getTable();
        this.name = tableContainer.getTableContainerName();
        this.clear();
    }

    public void clear() {
        this.actionCode = -1;
        this.actionSymbol = null;
    }

    public String getActionSymbol() {
        return this.actionSymbol;
    }

    public int getActionCode() {
        return this.actionCode;
    }

    public String setAction(int code) throws MaltChainedException {
        if (this.actionCode != code) {
            if (code < 0) {
                this.clear();
            } else {
                this.actionSymbol = this.table.getSymbolCodeToString(code);
                if (this.actionSymbol == null) {
                    this.clear();
                } else {
                    this.actionCode = code;
                }
            }
        }
        return this.actionSymbol;
    }

    public int setAction(String symbol) throws MaltChainedException {
        if (symbol == null) {
            this.clear();
        } else {
            this.actionCode = this.table.getSymbolStringToCode(symbol);
            if (this.actionCode == -1) {
                this.clear();
            } else {
                this.actionSymbol = symbol;
            }
        }
        return this.actionCode;
    }

    public Table getTable() {
        return this.table;
    }

    public String getTableName() {
        if (this.table == null) {
            return null;
        }
        return this.table.getName();
    }

    public String getTableContainerName() {
        return this.name;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append(" -> ");
        sb.append(this.actionSymbol);
        sb.append(" = ");
        sb.append(this.actionCode);
        return sb.toString();
    }
}

