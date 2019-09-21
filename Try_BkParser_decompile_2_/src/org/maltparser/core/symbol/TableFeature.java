/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.symbol;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.function.Modifiable;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.SymbolTableHandler;
import org.maltparser.core.symbol.nullvalue.NullValues;

public abstract class TableFeature
implements FeatureFunction,
Modifiable {
    protected final SingleFeatureValue featureValue;
    protected SymbolTable table;
    protected String tableName;
    protected SymbolTableHandler tableHandler;
    protected int type;

    public TableFeature(SymbolTableHandler tableHandler) throws MaltChainedException {
        this.tableHandler = tableHandler;
        this.featureValue = new SingleFeatureValue(this);
    }

    @Override
    public abstract void update() throws MaltChainedException;

    @Override
    public abstract void initialize(Object[] var1) throws MaltChainedException;

    @Override
    public abstract Class<?>[] getParameterTypes();

    @Override
    public String getSymbol(int value) throws MaltChainedException {
        return this.table.getSymbolCodeToString(value);
    }

    @Override
    public int getCode(String value) throws MaltChainedException {
        return this.table.getSymbolStringToCode(value);
    }

    @Override
    public SymbolTable getSymbolTable() {
        return this.table;
    }

    public void setSymbolTable(SymbolTable table) {
        this.table = table;
    }

    @Override
    public void setFeatureValue(int indexCode) throws MaltChainedException {
        if (this.table.getSymbolCodeToString(indexCode) == null) {
            this.featureValue.setIndexCode(indexCode);
            this.featureValue.setValue(1.0);
            this.featureValue.setSymbol(this.table.getNullValueSymbol(NullValues.NullValueId.NO_NODE));
            this.featureValue.setNullValue(true);
        } else {
            this.featureValue.setIndexCode(indexCode);
            this.featureValue.setValue(1.0);
            this.featureValue.setSymbol(this.table.getSymbolCodeToString(indexCode));
            this.featureValue.setNullValue(this.table.isNullValue(indexCode));
        }
    }

    @Override
    public void setFeatureValue(String symbol) throws MaltChainedException {
        if (this.table.getSymbolStringToCode(symbol) < 0) {
            this.featureValue.setIndexCode(this.table.getNullValueCode(NullValues.NullValueId.NO_NODE));
            this.featureValue.setValue(1.0);
            this.featureValue.setSymbol(symbol);
            this.featureValue.setNullValue(true);
        } else {
            this.featureValue.setIndexCode(this.table.getSymbolStringToCode(symbol));
            this.featureValue.setValue(1.0);
            this.featureValue.setSymbol(symbol);
            this.featureValue.setNullValue(this.table.isNullValue(symbol));
        }
    }

    @Override
    public FeatureValue getFeatureValue() {
        return this.featureValue;
    }

    public SymbolTableHandler getTableHandler() {
        return this.tableHandler;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TableFeature)) {
            return false;
        }
        return obj.toString().equals(this.toString());
    }

    public void setTableName(String name) {
        this.tableName = name;
    }

    public String getTableName() {
        return this.tableName;
    }

    @Override
    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String getMapIdentifier() {
        return this.getSymbolTable().getName();
    }

    public String toString() {
        return this.tableName;
    }
}

