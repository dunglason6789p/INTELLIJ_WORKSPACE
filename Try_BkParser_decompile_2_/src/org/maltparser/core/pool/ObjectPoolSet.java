/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.core.pool;

import java.util.Iterator;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.helper.HashSet;
import org.maltparser.core.pool.ObjectPool;

public abstract class ObjectPoolSet<T>
extends ObjectPool<T> {
    private final HashSet<T> available = new HashSet();
    private final HashSet<T> inuse = new HashSet();

    public ObjectPoolSet() {
        this(Integer.MAX_VALUE);
    }

    public ObjectPoolSet(int keepThreshold) {
        super(keepThreshold);
    }

    @Override
    protected abstract T create() throws MaltChainedException;

    @Override
    public abstract void resetObject(T var1) throws MaltChainedException;

    @Override
    public synchronized T checkOut() throws MaltChainedException {
        if (this.available.isEmpty()) {
            T t = this.create();
            this.inuse.add(t);
            return t;
        }
        Iterator<T> i$ = this.available.iterator();
        if (i$.hasNext()) {
            T t = i$.next();
            this.inuse.add(t);
            this.available.remove(t);
            return t;
        }
        return null;
    }

    @Override
    public synchronized void checkIn(T t) throws MaltChainedException {
        this.resetObject(t);
        this.inuse.remove(t);
        if (this.available.size() < this.keepThreshold) {
            this.available.add(t);
        }
    }

    @Override
    public synchronized void checkInAll() throws MaltChainedException {
        for (T t : this.inuse) {
            this.resetObject(t);
            if (this.available.size() >= this.keepThreshold) continue;
            this.available.add(t);
        }
        this.inuse.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T t : this.inuse) {
            sb.append(t);
            sb.append(", ");
        }
        return sb.toString();
    }
}

