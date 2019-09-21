/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

import java.io.Serializable;

public class svm_parameter
implements Cloneable,
Serializable {
    public static final int C_SVC = 0;
    public static final int NU_SVC = 1;
    public static final int ONE_CLASS = 2;
    public static final int EPSILON_SVR = 3;
    public static final int NU_SVR = 4;
    public static final int LINEAR = 0;
    public static final int POLY = 1;
    public static final int RBF = 2;
    public static final int SIGMOID = 3;
    public static final int PRECOMPUTED = 4;
    public int svm_type;
    public int kernel_type;
    public int degree;
    public double gamma;
    public double coef0;
    public double cache_size;
    public double eps;
    public double C;
    public int nr_weight;
    public int[] weight_label;
    public double[] weight;
    public double nu;
    public double p;
    public int shrinking;
    public int probability;

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

