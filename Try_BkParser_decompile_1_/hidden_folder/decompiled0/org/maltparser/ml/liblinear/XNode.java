/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.liblinear;

public class XNode
implements Comparable<XNode> {
    private int index;
    private double value;

    public XNode(int index, double value) {
        this.setIndex(index);
        this.setValue(value);
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.index;
        long temp = Double.doubleToLongBits(this.value);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
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
        XNode other = (XNode)obj;
        if (this.index != other.index) {
            return false;
        }
        return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
    }

    @Override
    public int compareTo(XNode aThat) {
        int BEFORE = -1;
        boolean EQUAL = false;
        boolean AFTER = true;
        if (this == aThat) {
            return 0;
        }
        if (this.index < aThat.index) {
            return -1;
        }
        if (this.index > aThat.index) {
            return 1;
        }
        if (this.value < aThat.value) {
            return -1;
        }
        return this.value > aThat.value;
    }

    public String toString() {
        return "XNode [index=" + this.index + ", value=" + this.value + "]";
    }
}

