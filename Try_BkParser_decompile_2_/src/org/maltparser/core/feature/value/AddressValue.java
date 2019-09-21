/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.value;

import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.FunctionValue;

public class AddressValue
extends FunctionValue {
    private Object address;

    public AddressValue(Function function) {
        super(function);
        this.setAddress(null);
    }

    @Override
    public void reset() {
        this.setAddress(null);
    }

    public Class<?> getAddressClass() {
        if (this.address != null) {
            return this.address.getClass();
        }
        return null;
    }

    public Object getAddress() {
        return this.address;
    }

    public void setAddress(Object address) {
        this.address = address;
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
        AddressValue other = (AddressValue)obj;
        if (this.address == null ? other.address != null : !this.address.equals(other.address)) {
            return false;
        }
        return super.equals(obj);
    }

    public int hashCode() {
        return 31 + (this.address == null ? 0 : this.address.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(this.address.toString());
        return sb.toString();
    }
}

