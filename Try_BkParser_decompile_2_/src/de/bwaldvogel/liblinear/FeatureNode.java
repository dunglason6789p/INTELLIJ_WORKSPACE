/*
 * Decompiled with CFR 0.146.
 */
package de.bwaldvogel.liblinear;

public class FeatureNode {
    public final int index;
    public double value;

    public FeatureNode(int index, double value) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0");
        }
        this.index = index;
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
        FeatureNode other = (FeatureNode)obj;
        if (this.index != other.index) {
            return false;
        }
        return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
    }

    public String toString() {
        return "FeatureNode(idx=" + this.index + ", value=" + this.value + ")";
    }
}

