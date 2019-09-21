/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.pool;

import java.util.ArrayList;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.pool.ObjectPool;

public abstract class ObjectPoolList<T>
extends ObjectPool<T> {
    private final ArrayList<T> objectList = new ArrayList();
    private int currentSize;

    public ObjectPoolList() {
        this(Integer.MAX_VALUE);
    }

    public ObjectPoolList(int keepThreshold) {
        super(keepThreshold);
    }

    @Override
    protected abstract T create() throws MaltChainedException;

    @Override
    public abstract void resetObject(T var1) throws MaltChainedException;

    @Override
    public synchronized T checkOut() throws MaltChainedException {
        T t = null;
        if (this.currentSize >= this.objectList.size()) {
            t = this.create();
            this.objectList.add(t);
            ++this.currentSize;
        } else {
            t = this.objectList.get(this.currentSize);
            ++this.currentSize;
        }
        return t;
    }

    @Override
    public synchronized void checkIn(T o) throws MaltChainedException {
        this.resetObject(o);
    }

    @Override
    public synchronized void checkInAll() throws MaltChainedException {
        for (int i = this.currentSize - 1; i >= 0 && i < this.objectList.size(); --i) {
            this.resetObject(this.objectList.get(i));
            if (this.currentSize < this.keepThreshold) continue;
            this.objectList.remove(i);
        }
        this.currentSize = 0;
    }

    public int getCurrentSize() {
        return this.currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    public int size() {
        return this.objectList.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.currentSize; ++i) {
            sb.append(this.objectList.get(i));
            sb.append(", ");
        }
        return sb.toString();
    }
}

