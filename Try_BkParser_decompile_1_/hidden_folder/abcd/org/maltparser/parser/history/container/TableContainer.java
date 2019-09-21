/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.parser.history.container;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.Table;
import org.maltparser.parser.history.container.DecisionPropertyTable;

public class TableContainer {
    protected int cachedCode;
    protected final StringBuilder cachedSymbol;
    protected Table table;
    protected String name;
    private final RelationToNextDecision relationToNextDecision;

    public TableContainer(Table _table, String _name, char _decisionSeparator) {
        this.table = _table;
        this.name = _name;
        switch (_decisionSeparator) {
            case '+': {
                this.relationToNextDecision = RelationToNextDecision.COMBINED;
                break;
            }
            case ',': {
                this.relationToNextDecision = RelationToNextDecision.SEQUANTIAL;
                break;
            }
            case ';': {
                this.relationToNextDecision = RelationToNextDecision.BRANCHED;
                break;
            }
            case '#': {
                this.relationToNextDecision = RelationToNextDecision.BRANCHED;
                break;
            }
            case '?': {
                this.relationToNextDecision = RelationToNextDecision.SWITCHED;
                break;
            }
            default: {
                this.relationToNextDecision = RelationToNextDecision.NONE;
            }
        }
        this.cachedSymbol = new StringBuilder();
        this.cachedCode = -1;
    }

    public void clearCache() {
        this.cachedCode = -1;
        this.cachedSymbol.setLength(0);
    }

    public String getSymbol(int code) throws MaltChainedException {
        if (code < 0 && !this.containCode(code)) {
            this.clearCache();
            return null;
        }
        if (this.cachedCode != code) {
            this.clearCache();
            this.cachedCode = code;
            this.cachedSymbol.append(this.table.getSymbolCodeToString(this.cachedCode));
        }
        return this.cachedSymbol.toString();
    }

    public int getCode(String symbol) throws MaltChainedException {
        if (this.cachedSymbol == null || !this.cachedSymbol.equals(symbol)) {
            this.clearCache();
            this.cachedSymbol.append(symbol);
            this.cachedCode = this.table.getSymbolStringToCode(symbol);
        }
        return this.cachedCode;
    }

    public boolean containCode(int code) throws MaltChainedException {
        if (this.cachedCode != code) {
            this.clearCache();
            this.cachedSymbol.append(this.table.getSymbolCodeToString(code));
            if (this.cachedSymbol == null) {
                return false;
            }
            this.cachedCode = code;
        }
        return true;
    }

    public boolean containSymbol(String symbol) throws MaltChainedException {
        if (this.cachedSymbol == null || !this.cachedSymbol.equals(symbol)) {
            this.clearCache();
            this.cachedCode = this.table.getSymbolStringToCode(symbol);
            if (this.cachedCode < 0) {
                return false;
            }
            this.cachedSymbol.append(symbol);
        }
        return true;
    }

    public boolean continueWithNextDecision(int code) throws MaltChainedException {
        if (this.table instanceof DecisionPropertyTable) {
            return ((DecisionPropertyTable)((Object)this.table)).continueWithNextDecision(code);
        }
        return true;
    }

    public boolean continueWithNextDecision(String symbol) throws MaltChainedException {
        if (this.table instanceof DecisionPropertyTable) {
            return ((DecisionPropertyTable)((Object)this.table)).continueWithNextDecision(symbol);
        }
        return true;
    }

    public Table getTable() {
        return this.table;
    }

    public String getTableName() {
        return this.table != null ? this.table.getName() : null;
    }

    public String getTableContainerName() {
        return this.name;
    }

    public RelationToNextDecision getRelationToNextDecision() {
        return this.relationToNextDecision;
    }

    protected void setTable(Table table) {
        this.table = table;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public int size() {
        return this.table.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append(" -> ");
        sb.append(this.cachedSymbol);
        sb.append(" = ");
        sb.append(this.cachedCode);
        return sb.toString();
    }

    public static enum RelationToNextDecision {
        COMBINED,
        SEQUANTIAL,
        BRANCHED,
        SWITCHED,
        NONE;
        
    }

}

