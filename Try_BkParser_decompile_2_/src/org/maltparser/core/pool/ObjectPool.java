/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.pool;

import org.maltparser.core.exception.MaltChainedException;

public abstract class ObjectPool<T> {
    protected int keepThreshold;

    public ObjectPool() {
        this(Integer.MAX_VALUE);
    }

    public ObjectPool(int keepThreshold) {
        this.setKeepThreshold(keepThreshold);
    }

    public int getKeepThreshold() {
        return this.keepThreshold;
    }

    public void setKeepThreshold(int keepThreshold) {
        this.keepThreshold = keepThreshold;
    }

    protected abstract T create() throws MaltChainedException;

    public abstract void resetObject(T var1) throws MaltChainedException;

    public abstract T checkOut() throws MaltChainedException;

    public abstract void checkIn(T var1) throws MaltChainedException;

    public abstract void checkInAll() throws MaltChainedException;
}

