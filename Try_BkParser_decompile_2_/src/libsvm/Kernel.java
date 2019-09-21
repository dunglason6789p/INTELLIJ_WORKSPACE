/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

import libsvm.QMatrix;
import libsvm.svm_node;
import libsvm.svm_parameter;

abstract class Kernel
extends QMatrix {
    private svm_node[][] x;
    private final double[] x_square;
    private final int kernel_type;
    private final int degree;
    private final double gamma;
    private final double coef0;

    abstract float[] get_Q(int var1, int var2);

    abstract double[] get_QD();

    void swap_index(int i, int j) {
        svm_node[] _ = this.x[i];
        this.x[i] = this.x[j];
        this.x[j] = _;
        if (this.x_square != null) {
            double _2 = this.x_square[i];
            this.x_square[i] = this.x_square[j];
            this.x_square[j] = _2;
        }
    }

    private static double powi(double base, int times) {
        double tmp = base;
        double ret = 1.0;
        for (int t = times; t > 0; t /= 2) {
            if (t % 2 == 1) {
                ret *= tmp;
            }
            tmp *= tmp;
        }
        return ret;
    }

    double kernel_function(int i, int j) {
        switch (this.kernel_type) {
            case 0: {
                return Kernel.dot(this.x[i], this.x[j]);
            }
            case 1: {
                return Kernel.powi(this.gamma * Kernel.dot(this.x[i], this.x[j]) + this.coef0, this.degree);
            }
            case 2: {
                return Math.exp(-this.gamma * (this.x_square[i] + this.x_square[j] - 2.0 * Kernel.dot(this.x[i], this.x[j])));
            }
            case 3: {
                return Math.tanh(this.gamma * Kernel.dot(this.x[i], this.x[j]) + this.coef0);
            }
            case 4: {
                return this.x[i][(int)this.x[j][0].value].value;
            }
        }
        return 0.0;
    }

    Kernel(int l, svm_node[][] x_, svm_parameter param) {
        this.kernel_type = param.kernel_type;
        this.degree = param.degree;
        this.gamma = param.gamma;
        this.coef0 = param.coef0;
        this.x = (svm_node[][])x_.clone();
        if (this.kernel_type == 2) {
            this.x_square = new double[l];
            for (int i = 0; i < l; ++i) {
                this.x_square[i] = Kernel.dot(this.x[i], this.x[i]);
            }
        } else {
            this.x_square = null;
        }
    }

    static double dot(svm_node[] x, svm_node[] y) {
        double sum = 0.0;
        int xlen = x.length;
        int ylen = y.length;
        int i = 0;
        int j = 0;
        while (i < xlen && j < ylen) {
            if (x[i].index == y[j].index) {
                sum += x[i++].value * y[j++].value;
                continue;
            }
            if (x[i].index > y[j].index) {
                ++j;
                continue;
            }
            ++i;
        }
        return sum;
    }

    static double k_function(svm_node[] x, svm_node[] y, svm_parameter param) {
        switch (param.kernel_type) {
            case 0: {
                return Kernel.dot(x, y);
            }
            case 1: {
                return Kernel.powi(param.gamma * Kernel.dot(x, y) + param.coef0, param.degree);
            }
            case 2: {
                double sum = 0.0;
                int xlen = x.length;
                int ylen = y.length;
                int i = 0;
                int j = 0;
                while (i < xlen && j < ylen) {
                    if (x[i].index == y[j].index) {
                        double d = x[i++].value - y[j++].value;
                        sum += d * d;
                        continue;
                    }
                    if (x[i].index > y[j].index) {
                        sum += y[j].value * y[j].value;
                        ++j;
                        continue;
                    }
                    sum += x[i].value * x[i].value;
                    ++i;
                }
                while (i < xlen) {
                    sum += x[i].value * x[i].value;
                    ++i;
                }
                while (j < ylen) {
                    sum += y[j].value * y[j].value;
                    ++j;
                }
                return Math.exp(-param.gamma * sum);
            }
            case 3: {
                return Math.tanh(param.gamma * Kernel.dot(x, y) + param.coef0);
            }
            case 4: {
                return x[(int)y[0].value].value;
            }
        }
        return 0.0;
    }
}

