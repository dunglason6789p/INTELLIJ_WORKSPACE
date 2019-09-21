/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import vn.edu.vnu.uet.liblinear.Linear;
import vn.edu.vnu.uet.liblinear.SolverType;

public final class Model
implements Serializable {
    private static final long serialVersionUID = -6456047576741854834L;
    double bias;
    int[] label;
    int nr_class;
    int nr_feature;
    SolverType solverType;
    double[] w;

    public int getNrClass() {
        return this.nr_class;
    }

    public int getNrFeature() {
        return this.nr_feature;
    }

    public int[] getLabels() {
        return Linear.copyOf(this.label, this.nr_class);
    }

    public double[] getFeatureWeights() {
        return Linear.copyOf(this.w, this.w.length);
    }

    public boolean isProbabilityModel() {
        return this.solverType.isLogisticRegressionSolver();
    }

    public double getBias() {
        return this.bias;
    }

    private double get_w_value(int idx, int label_idx) {
        if (idx < 0 || idx > this.nr_feature) {
            return 0.0;
        }
        if (this.solverType.isSupportVectorRegression()) {
            return this.w[idx];
        }
        if (label_idx < 0 || label_idx >= this.nr_class) {
            return 0.0;
        }
        if (this.nr_class == 2 && this.solverType != SolverType.MCSVM_CS) {
            if (label_idx == 0) {
                return this.w[idx];
            }
            return -this.w[idx];
        }
        return this.w[idx * this.nr_class + label_idx];
    }

    public double getDecfunCoef(int featIdx, int labelIdx) {
        if (featIdx > this.nr_feature) {
            return 0.0;
        }
        return this.get_w_value(featIdx - 1, labelIdx);
    }

    public double getDecfunBias(int labelIdx) {
        int biasIdx = this.nr_feature;
        if (this.bias <= 0.0) {
            return 0.0;
        }
        return this.bias * this.get_w_value(biasIdx, labelIdx);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Model");
        sb.append(" bias=").append(this.bias);
        sb.append(" nr_class=").append(this.nr_class);
        sb.append(" nr_feature=").append(this.nr_feature);
        sb.append(" solverType=").append((Object)this.solverType);
        return sb.toString();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        long temp = Double.doubleToLongBits(this.bias);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        result = 31 * result + Arrays.hashCode(this.label);
        result = 31 * result + this.nr_class;
        result = 31 * result + this.nr_feature;
        result = 31 * result + (this.solverType == null ? 0 : this.solverType.hashCode());
        result = 31 * result + Arrays.hashCode(this.w);
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
        Model other = (Model)obj;
        if (Double.doubleToLongBits(this.bias) != Double.doubleToLongBits(other.bias)) {
            return false;
        }
        if (!Arrays.equals(this.label, other.label)) {
            return false;
        }
        if (this.nr_class != other.nr_class) {
            return false;
        }
        if (this.nr_feature != other.nr_feature) {
            return false;
        }
        if (this.solverType == null ? other.solverType != null : !this.solverType.equals((Object)other.solverType)) {
            return false;
        }
        return Model.equals(this.w, other.w);
    }

    protected static boolean equals(double[] a, double[] a2) {
        if (a == a2) {
            return true;
        }
        if (a == null || a2 == null) {
            return false;
        }
        int length = a.length;
        if (a2.length != length) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (a[i] == a2[i]) continue;
            return false;
        }
        return true;
    }

    public void save(File file) throws IOException {
        Linear.saveModel(file, this);
    }

    public void save(Writer writer) throws IOException {
        Linear.saveModel(writer, this);
    }

    public static Model load(File file) throws IOException {
        return Linear.loadModel(file);
    }

    public static Model load(Reader inputReader) throws IOException {
        return Linear.loadModel(inputReader);
    }
}

