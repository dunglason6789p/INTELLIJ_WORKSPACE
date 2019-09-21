/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.value;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.FeatureValue;
import org.maltparser.core.helper.HashSet;

public class MultipleFeatureValue
extends FeatureValue {
    protected SortedMap<Integer, String> featureValues;

    public MultipleFeatureValue(Function function) {
        super(function);
        this.setFeatureValues(new TreeMap<Integer, String>());
    }

    @Override
    public void reset() {
        super.reset();
        this.featureValues.clear();
    }

    public void addFeatureValue(int code, String Symbol) {
        this.featureValues.put(code, Symbol);
    }

    protected void setFeatureValues(SortedMap<Integer, String> featureValues) {
        this.featureValues = featureValues;
    }

    public Set<Integer> getCodes() {
        return this.featureValues.keySet();
    }

    public int getFirstCode() {
        return this.featureValues.firstKey();
    }

    public Set<String> getSymbols() {
        return new HashSet<String>(this.featureValues.values());
    }

    public String getFirstSymbol() {
        return (String)this.featureValues.get(this.featureValues.firstKey());
    }

    @Override
    public boolean isMultiple() {
        return true;
    }

    @Override
    public int nFeatureValues() {
        return this.featureValues.size();
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
        MultipleFeatureValue v = (MultipleFeatureValue)obj;
        if (!this.featureValues.equals(v.featureValues)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append('{');
        for (Integer code : this.featureValues.keySet()) {
            sb.append('{');
            sb.append((String)this.featureValues.get(code));
            sb.append("->");
            sb.append(code);
            sb.append('}');
        }
        sb.append('}');
        return sb.toString();
    }
}

