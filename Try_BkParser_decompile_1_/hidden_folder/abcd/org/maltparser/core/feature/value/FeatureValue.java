/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.value;

import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.FunctionValue;

public abstract class FeatureValue
extends FunctionValue {
    protected boolean nullValue;

    public FeatureValue(Function function) {
        super(function);
        this.setNullValue(true);
    }

    @Override
    public void reset() {
        this.setNullValue(true);
    }

    public boolean isNullValue() {
        return this.nullValue;
    }

    public void setNullValue(boolean nullValue) {
        this.nullValue = nullValue;
    }

    public abstract boolean isMultiple();

    public abstract int nFeatureValues();

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
        return super.equals(obj);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("[null=");
        sb.append(this.nullValue);
        sb.append("]");
        return sb.toString();
    }
}

