/*
 * Decompiled with CFR 0.146.
 */
package org.maltparser.ml.lib;

public class MaltFeatureNode
implements Comparable<MaltFeatureNode> {
    int index;
    double value;

    public MaltFeatureNode() {
        this.index = -1;
        this.value = 0.0;
    }

    public MaltFeatureNode(int index, double value) {
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

    public void clear() {
        this.index = -1;
        this.value = 0.0;
    }

    public int hashCode() {
        int prime = 31;
        long temp = Double.doubleToLongBits(this.value);
        return 31 * (31 + this.index) + (int)(temp ^ temp >>> 32);
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
        MaltFeatureNode other = (MaltFeatureNode)obj;
        if (this.index != other.index) {
            return false;
        }
        return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
    }

    @Override
    public int compareTo(MaltFeatureNode aThat) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("MaltFeatureNode [index=");
        sb.append(this.index);
        sb.append(", value=");
        sb.append(this.value);
        sb.append("]");
        return sb.toString();
    }
}

