/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol.nullvalue;

import java.util.SortedMap;
import org.maltparser.core.helper.HashMap;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.nullvalue.NullValues;

public class InputNullValues
extends NullValues {
    public InputNullValues(String nullValueStrategy, SymbolTable table) {
        super(table);
        this.setNullValueEncoding(nullValueStrategy);
        this.makeNullValues();
    }

    @Override
    protected void makeNullValues() {
        if (this.nullValueEncoding == NullValues.NullValueDegree.NONE || this.nullValueEncoding == NullValues.NullValueDegree.ONE) {
            this.nullValue2SymbolMap.put(NullValues.NullValueId.NO_NODE, "#null#");
            this.nullValue2SymbolMap.put(NullValues.NullValueId.ROOT_NODE, "#null#");
            this.nullValue2CodeMap.put(NullValues.NullValueId.NO_NODE, 0);
            this.nullValue2CodeMap.put(NullValues.NullValueId.ROOT_NODE, 0);
            this.symbol2CodeMap.put("#null#", 0);
            this.code2SymbolMap.put(0, "#null#");
            this.setNextCode(1);
        } else if (this.nullValueEncoding == NullValues.NullValueDegree.ROOTNODE) {
            this.nullValue2SymbolMap.put(NullValues.NullValueId.NO_NODE, "#null#");
            this.nullValue2SymbolMap.put(NullValues.NullValueId.ROOT_NODE, "#rootnode#");
            this.nullValue2CodeMap.put(NullValues.NullValueId.NO_NODE, 0);
            this.nullValue2CodeMap.put(NullValues.NullValueId.ROOT_NODE, 1);
            this.symbol2CodeMap.put("#null#", 0);
            this.symbol2CodeMap.put("#rootnode#", 1);
            this.code2SymbolMap.put(0, "#null#");
            this.code2SymbolMap.put(1, "#rootnode#");
            this.setNextCode(2);
        }
    }

    @Override
    protected void setNullValueEncoding(String nullValueStrategy) {
        this.setNullValueStrategy(nullValueStrategy);
        this.nullValueEncoding = nullValueStrategy.equalsIgnoreCase("none") ? NullValues.NullValueDegree.NONE : (nullValueStrategy.equalsIgnoreCase("rootnode") ? NullValues.NullValueDegree.ROOTNODE : (nullValueStrategy.equalsIgnoreCase("novalue") ? NullValues.NullValueDegree.ROOTNODE : NullValues.NullValueDegree.ONE));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }
}

