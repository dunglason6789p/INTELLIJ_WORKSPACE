/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.value;

import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.FeatureValue;

public class SingleFeatureValue
extends FeatureValue {
    protected int indexCode;
    protected String symbol;
    protected double value;

    public SingleFeatureValue(Function function) {
        super(function);
        this.setIndexCode(0);
        this.setSymbol(null);
        this.setValue(0.0);
    }

    @Override
    public void reset() {
        super.reset();
        this.setIndexCode(0);
        this.setSymbol(null);
        this.setValue(0.0);
    }

    public void update(int indexCode, String symbol, boolean nullValue, double value) {
        this.indexCode = indexCode;
        this.symbol = symbol;
        this.nullValue = nullValue;
        this.value = value;
    }

    public int getIndexCode() {
        return this.indexCode;
    }

    public void setIndexCode(int code) {
        this.indexCode = code;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean isMultiple() {
        return false;
    }

    @Override
    public int nFeatureValues() {
        return 1;
    }

    public int hashCode() {
        int prime = 31;
        return 31 * (31 + this.indexCode) + (this.symbol == null ? 0 : this.symbol.hashCode());
    }

    @Override
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
        SingleFeatureValue other = (SingleFeatureValue)obj;
        if (this.indexCode != other.indexCode) {
            return false;
        }
        if (this.symbol == null ? other.symbol != null : !this.symbol.equals(other.symbol)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append('{');
        sb.append(this.symbol);
        sb.append("->");
        sb.append(this.indexCode);
        sb.append('}');
        return sb.toString();
    }
}

