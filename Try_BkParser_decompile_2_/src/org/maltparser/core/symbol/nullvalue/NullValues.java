/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol.nullvalue;

import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolException;
import org.maltparser.core.symbol.SymbolTable;

public abstract class NullValues {
    protected HashMap<NullValueId, String> nullValue2SymbolMap;
    protected HashMap<NullValueId, Integer> nullValue2CodeMap;
    protected HashMap<String, Integer> symbol2CodeMap;
    protected SortedMap<Integer, String> code2SymbolMap;
    protected SymbolTable table;
    protected NullValueDegree nullValueEncoding;
    protected String nullValueStrategy;
    protected int nextCode;

    public NullValues(SymbolTable table) {
        this.setSymbolTable(table);
        this.nullValue2SymbolMap = new HashMap();
        this.nullValue2CodeMap = new HashMap();
        this.symbol2CodeMap = new HashMap();
        this.code2SymbolMap = new TreeMap<Integer, String>();
    }

    private void setSymbolTable(SymbolTable table) {
        this.table = table;
    }

    public SymbolTable getSymbolTable() {
        return this.table;
    }

    public String getNullValueStrategy() {
        return this.nullValueStrategy;
    }

    protected void setNullValueStrategy(String nullValueStrategy) {
        this.nullValueStrategy = nullValueStrategy;
    }

    public NullValueDegree getNullValueEncoding() {
        return this.nullValueEncoding;
    }

    public int getNextCode() {
        return this.nextCode;
    }

    protected void setNextCode(int nextCode) {
        this.nextCode = nextCode;
    }

    public boolean isNullValue(int code) {
        return code >= 0 && code < this.nextCode;
    }

    public boolean isNullValue(String symbol) {
        if (symbol == null || symbol.length() == 0 || symbol.charAt(0) != '#') {
            return false;
        }
        if (symbol.equals("#null#")) {
            return true;
        }
        if ((this.nullValueEncoding == NullValueDegree.ROOTNODE || this.nullValueEncoding == NullValueDegree.NOVALUE) && symbol.equals("#rootnode#")) {
            return true;
        }
        return this.nullValueEncoding == NullValueDegree.NOVALUE && symbol.equals("#novalue#");
    }

    public int nullvalueToCode(NullValueId nullValueIdentifier) throws MaltChainedException {
        if (!this.nullValue2CodeMap.containsKey((Object)nullValueIdentifier)) {
            throw new SymbolException("Illegal null-value identifier. ");
        }
        return this.nullValue2CodeMap.get((Object)nullValueIdentifier);
    }

    public String nullvalueToSymbol(NullValueId nullValueIdentifier) throws MaltChainedException {
        if (!this.nullValue2SymbolMap.containsKey((Object)nullValueIdentifier)) {
            throw new SymbolException("Illegal null-value identifier. ");
        }
        return this.nullValue2SymbolMap.get((Object)nullValueIdentifier);
    }

    public int symbolToCode(String symbol) {
        if (!this.symbol2CodeMap.containsKey(symbol)) {
            return -1;
        }
        return this.symbol2CodeMap.get(symbol);
    }

    public String codeToSymbol(int code) {
        if (!this.code2SymbolMap.containsKey(code)) {
            return null;
        }
        return (String)this.code2SymbolMap.get(code);
    }

    protected abstract void setNullValueEncoding(String var1);

    protected abstract void makeNullValues();

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
        NullValues nl = (NullValues)obj;
        if (!this.nullValueStrategy.equalsIgnoreCase(nl.getNullValueStrategy())) {
            return false;
        }
        if (this.nextCode != nl.getNextCode()) {
            return false;
        }
        if (!this.nullValue2SymbolMap.equals(nl.nullValue2SymbolMap)) {
            return false;
        }
        if (!this.nullValue2CodeMap.equals(nl.nullValue2CodeMap)) {
            return false;
        }
        if (!this.code2SymbolMap.equals(nl.code2SymbolMap)) {
            return false;
        }
        return this.symbol2CodeMap.equals(nl.symbol2CodeMap);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Null-values:\n");
        sb.append("  Strategy: " + this.nullValueStrategy);
        sb.append("  NO_NODE -> " + this.nullValue2CodeMap.get((Object)((Object)NullValueId.NO_NODE)) + " " + this.nullValue2SymbolMap.get((Object)((Object)NullValueId.NO_NODE)) + "\n");
        sb.append("  ROOT_NODE -> " + this.nullValue2CodeMap.get((Object)((Object)NullValueId.ROOT_NODE)) + " " + this.nullValue2SymbolMap.get((Object)((Object)NullValueId.ROOT_NODE)) + "\n");
        sb.append("  NO_VALUE -> " + this.nullValue2CodeMap.get((Object)((Object)NullValueId.NO_VALUE)) + " " + this.nullValue2SymbolMap.get((Object)((Object)NullValueId.NO_VALUE)) + "\n");
        return sb.toString();
    }

    public static enum NullValueId {
        NO_NODE,
        ROOT_NODE,
        NO_VALUE;
        
    }

    protected static enum NullValueDegree {
        NONE,
        ONE,
        ROOTNODE,
        NOVALUE;
        
    }

}

