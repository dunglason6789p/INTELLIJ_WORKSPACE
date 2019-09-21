/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.syntaxgraph.feature;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.FeatureException;
import org.maltparser.core.feature.function.FeatureFunction;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.function.Modifiable;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.feature.value.SingleFeatureValue;
import org.maltparser.core.io.dataformat.ColumnDescription;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.symbol.nullvalue.NullValues;

public abstract class ColumnFeature
implements FeatureFunction,
Modifiable {
    protected ColumnDescription column;
    protected SymbolTable symbolTable;
    protected final SingleFeatureValue featureValue = new SingleFeatureValue(this);

    @Override
    public abstract void update() throws MaltChainedException;

    @Override
    public abstract void initialize(Object[] var1) throws MaltChainedException;

    @Override
    public abstract Class<?>[] getParameterTypes();

    @Override
    public String getSymbol(int value) throws MaltChainedException {
        return this.symbolTable.getSymbolCodeToString(value);
    }

    @Override
    public int getCode(String value) throws MaltChainedException {
        return this.symbolTable.getSymbolStringToCode(value);
    }

    public ColumnDescription getColumn() {
        return this.column;
    }

    protected void setColumn(ColumnDescription column) {
        this.column = column;
    }

    @Override
    public SymbolTable getSymbolTable() {
        return this.symbolTable;
    }

    protected void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public void setFeatureValue(int indexCode) throws MaltChainedException {
        String symbol = this.symbolTable.getSymbolCodeToString(indexCode);
        if (symbol == null) {
            this.featureValue.update(indexCode, this.symbolTable.getNullValueSymbol(NullValues.NullValueId.NO_NODE), true, 1.0);
        } else {
            boolean nullValue = this.symbolTable.isNullValue(indexCode);
            if (this.column.getType() == 1 || nullValue) {
                this.featureValue.update(indexCode, symbol, nullValue, 1.0);
            } else {
                this.castFeatureValue(symbol);
            }
        }
    }

    @Override
    public void setFeatureValue(String symbol) throws MaltChainedException {
        int indexCode = this.symbolTable.getSymbolStringToCode(symbol);
        if (indexCode < 0) {
            this.featureValue.update(this.symbolTable.getNullValueCode(NullValues.NullValueId.NO_NODE), symbol, true, 1.0);
        } else {
            boolean nullValue = this.symbolTable.isNullValue(symbol);
            if (this.column.getType() == 1 || nullValue) {
                this.featureValue.update(indexCode, symbol, nullValue, 1.0);
            } else {
                this.castFeatureValue(symbol);
            }
        }
    }

    protected void castFeatureValue(String symbol) throws MaltChainedException {
        if (this.column.getType() == 2) {
            try {
                int dotIndex = symbol.indexOf(46);
                if (dotIndex == -1) {
                    this.featureValue.setValue(Integer.parseInt(symbol));
                    this.featureValue.setSymbol(symbol);
                } else {
                    this.featureValue.setValue(Integer.parseInt(symbol.substring(0, dotIndex)));
                    this.featureValue.setSymbol(symbol.substring(0, dotIndex));
                }
                this.featureValue.setNullValue(false);
                this.featureValue.setIndexCode(1);
            }
            catch (NumberFormatException e) {
                throw new FeatureException("Could not cast the feature value '" + symbol + "' to integer value.", e);
            }
        }
        if (this.column.getType() == 3) {
            int dotIndex = symbol.indexOf(46);
            if (symbol.equals("1") || symbol.equals("true") || symbol.equals("#true#") || dotIndex != -1 && symbol.substring(0, dotIndex).equals("1")) {
                this.featureValue.setValue(1.0);
                this.featureValue.setSymbol("true");
            } else if (symbol.equals("false") || symbol.equals("0") || dotIndex != -1 && symbol.substring(0, dotIndex).equals("0")) {
                this.featureValue.setValue(0.0);
                this.featureValue.setSymbol("false");
            } else {
                throw new FeatureException("Could not cast the feature value '" + symbol + "' to boolean value.");
            }
            this.featureValue.setNullValue(false);
            this.featureValue.setIndexCode(1);
        } else if (this.column.getType() == 4) {
            try {
                this.featureValue.setValue(Double.parseDouble(symbol));
                this.featureValue.setSymbol(symbol);
            }
            catch (NumberFormatException e) {
                throw new FeatureException("Could not cast the feature value '" + symbol + "' to real value.", e);
            }
            this.featureValue.setNullValue(false);
            this.featureValue.setIndexCode(1);
        }
    }

    @Override
    public FeatureValue getFeatureValue() {
        return this.featureValue;
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
        return obj.toString().equals(this.toString());
    }

    public String getColumnName() {
        return this.column.getName();
    }

    @Override
    public int getType() {
        return this.column.getType();
    }

    @Override
    public String getMapIdentifier() {
        return this.getSymbolTable().getName();
    }

    public String toString() {
        return this.column.getName();
    }
}

