/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.feature.function;

import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.feature.function.Function;
import org.maltparser.core.feature.value.AddressValue;

public abstract class AddressFunction
implements Function {
    protected final AddressValue address = new AddressValue(this);

    public abstract void update(Object[] var1) throws MaltChainedException;

    public AddressValue getAddressValue() {
        return this.address;
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
        return this.address.equals(((AddressFunction)obj).getAddressValue());
    }

    public String toString() {
        return this.address.toString();
    }
}

