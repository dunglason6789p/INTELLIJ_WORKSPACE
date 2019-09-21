/*
 * Decompiled with CFR 0.146.
 */
package third_party.org.chokkan.crfsuite;

import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.crfsuiteJNI;

public class Item {
    private long swigCPtr;
    protected boolean swigCMemOwn;

    public Item(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    public static long getCPtr(Item obj) {
        return obj == null ? 0L : obj.swigCPtr;
    }

    protected void finalize() {
        this.delete();
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0L) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                crfsuiteJNI.delete_Item(this.swigCPtr);
            }
            this.swigCPtr = 0L;
        }
    }

    public Item() {
        this(crfsuiteJNI.new_Item__SWIG_0(), true);
    }

    public Item(long n) {
        this(crfsuiteJNI.new_Item__SWIG_1(n), true);
    }

    public long size() {
        return crfsuiteJNI.Item_size(this.swigCPtr, this);
    }

    public long capacity() {
        return crfsuiteJNI.Item_capacity(this.swigCPtr, this);
    }

    public void reserve(long n) {
        crfsuiteJNI.Item_reserve(this.swigCPtr, this, n);
    }

    public boolean isEmpty() {
        return crfsuiteJNI.Item_isEmpty(this.swigCPtr, this);
    }

    public void clear() {
        crfsuiteJNI.Item_clear(this.swigCPtr, this);
    }

    public void add(Attribute x) {
        crfsuiteJNI.Item_add(this.swigCPtr, this, Attribute.getCPtr(x), x);
    }

    public Attribute get(int i) {
        return new Attribute(crfsuiteJNI.Item_get(this.swigCPtr, this, i), false);
    }

    public void set(int i, Attribute val) {
        crfsuiteJNI.Item_set(this.swigCPtr, this, i, Attribute.getCPtr(val), val);
    }
}

