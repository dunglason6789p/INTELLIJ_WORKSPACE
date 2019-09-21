/*
 * Decompiled with CFR 0.146.
 */
package libsvm;

import libsvm.Cache;
import libsvm.Kernel;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

class SVC_Q
extends Kernel {
    private final byte[] y;
    private final Cache cache;
    private final double[] QD;

    SVC_Q(svm_problem prob, svm_parameter param, byte[] y_) {
        super(prob.l, prob.x, param);
        this.y = (byte[])y_.clone();
        this.cache = new Cache(prob.l, (long)(param.cache_size * 1048576.0));
        this.QD = new double[prob.l];
        for (int i = 0; i < prob.l; ++i) {
            this.QD[i] = this.kernel_function(i, i);
        }
    }

    float[] get_Q(int i, int len) {
        float[][] data = new float[1][];
        int start = this.cache.get_data(i, data, len);
        if (start < len) {
            for (int j = start; j < len; ++j) {
                data[0][j] = (float)((double)(this.y[i] * this.y[j]) * this.kernel_function(i, j));
            }
        }
        return data[0];
    }

    double[] get_QD() {
        return this.QD;
    }

    void swap_index(int i, int j) {
        this.cache.swap_index(i, j);
        super.swap_index(i, j);
        byte _ = this.y[i];
        this.y[i] = this.y[j];
        this.y[j] = _;
        double _2 = this.QD[i];
        this.QD[i] = this.QD[j];
        this.QD[j] = _2;
    }
}

