/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.headrules;

import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.headrules.PrioSet;

public class PrioSetMember {
    protected PrioSet prioSet;
    protected SymbolTable table;
    protected ColumnDescription column;
    protected int symbolCode;
    protected RelationToPrevMember relationToPrevMember;

    public PrioSetMember(PrioSet prioSet, SymbolTable table, ColumnDescription column, int symbolCode, RelationToPrevMember relationToPrevMember) {
        this.setPrioSet(prioSet);
        this.setTable(table);
        this.setColumn(column);
        this.setSymbolCode(symbolCode);
        this.setRelationToPrevMember(relationToPrevMember);
    }

    public PrioSetMember(PrioSet prioSet, SymbolTable table, ColumnDescription column, String symbolString, RelationToPrevMember relationToPrevMember) throws MaltChainedException {
        this.setPrioSet(prioSet);
        this.setTable(table);
        this.setColumn(column);
        if (table != null) {
            this.setSymbolCode(table.getSymbolStringToCode(symbolString));
        } else {
            this.setSymbolCode(-1);
        }
        this.setRelationToPrevMember(relationToPrevMember);
    }

    public PrioSet getPrioSet() {
        return this.prioSet;
    }

    public void setPrioSet(PrioSet prioSet) {
        this.prioSet = prioSet;
    }

    public ColumnDescription getColumn() {
        return this.column;
    }

    public void setColumn(ColumnDescription column) {
        this.column = column;
    }

    public SymbolTable getTable() {
        return this.table;
    }

    public void setTable(SymbolTable table) {
        this.table = table;
    }

    public int getSymbolCode() {
        return this.symbolCode;
    }

    public String getSymbolString() throws MaltChainedException {
        if (this.table != null && this.symbolCode >= 0) {
            return this.table.getSymbolCodeToString(this.symbolCode);
        }
        return null;
    }

    public void setSymbolCode(int symbolCode) {
        this.symbolCode = symbolCode;
    }

    public RelationToPrevMember getRelationToPrevMember() {
        return this.relationToPrevMember;
    }

    public void setRelationToPrevMember(RelationToPrevMember relationToPrevMember) {
        this.relationToPrevMember = relationToPrevMember;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.symbolCode;
        result = 31 * result + (this.relationToPrevMember == null ? 0 : this.relationToPrevMember.hashCode());
        return result;
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
        PrioSetMember other = (PrioSetMember)obj;
        if (this.symbolCode != other.symbolCode) {
            return false;
        }
        return !(this.relationToPrevMember == null ? other.relationToPrevMember != null : !this.relationToPrevMember.equals((Object)other.relationToPrevMember));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.table.getName());
        sb.append(':');
        try {
            sb.append(this.getSymbolString());
        }
        catch (MaltChainedException e) {
            if (this.prioSet.getLogger().isDebugEnabled()) {
                this.prioSet.getLogger().debug("", e);
            }
            this.prioSet.getLogger().error(e.getMessageChain());
        }
        return sb.toString();
    }

    protected static enum RelationToPrevMember {
        START,
        DISJUNCTION,
        CONJUNCTION;
        
    }

}

